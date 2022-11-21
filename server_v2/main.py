import socket, threading
import json
import sqlite3
from json.decoder import JSONDecodeError
from sqlite3 import Error

from server_v2.annotation_managers.annotation_manager import AnnotationManager
from server_v2.overview_managers.overview_manager import OverviewManager

class ClientThread(threading.Thread):
    def __init__(self,clientAddress,clientsocket,db_file, aliases, aliases_lower, redirects, abstracts):
        threading.Thread.__init__(self)
        self.csocket = clientsocket
        self.idx = 0
        self.db_file = db_file
        self.previous_id = -1
        self.clientAddress = clientAddress
        self.annotation_manager = AnnotationManager()
        self.overview_manager = OverviewManager()
        self.aliases = aliases
        self.redirects = redirects
        self.aliases_lower = aliases_lower
        self.abstracts = abstracts
        print ("New connection added: ", self.clientAddress)
    def run(self):
        print ("Connection from : ", self.clientAddress)
        self.create_connection()
        errors = 0
        loops = 0
        while True:
            loops += 1
            if loops > 10:
                break
            print('waiting for data')
            data = self.csocket.recv(2048)
            msg = data.decode()
            print ("from client", msg)

            messages = msg.split('\n')
            print("client sent " + str(len(messages)) + " messages.")

            try:
                for msg in messages:
                    print("message:")
                    print(msg)
                    if len(msg.strip()) > 0:
                        print("length > 0")
                        self.process_message(msg)
                        loops = 0
            except JSONDecodeError as e:

                if errors == 0:
                    print("got json decode error, try again.")
                    print(e)
                    message = {"mode": -2}
                    print(message)
                    message = json.dumps(message) + '\n'
                    self.csocket.send(bytes(message, 'UTF-8'))
                    errors += 1
                else:
                    break


        print ("Client at ", self.clientAddress , " disconnected...")

    def process_message(self, msg):
        d = json.loads(msg)
        uid = d['uid']
        task = d['task']
        errors = 0
        if 'uid' in d and d['uid'] == "abcdefghijklmnopqrstuvwxyz" :
            message = {"mode": -3, "message": "Please update the app and note your UID before you annotate more. Please email that one to Michael!"}
            print(message)
            message = json.dumps(message) + '\n'
            self.csocket.send(bytes(message, 'UTF-8'))
            return
        if d['mode'] == 0:
            return
        elif d['mode'] == 1:
            dataset_name = d['datasetName']
            dataset_source = d['datasetSource']
            print("client asks for sample.")
            message = self.annotation_manager.get_sample_from_database(self.conn, dataset_name, dataset_source, uid, task)
            self.csocket.send(bytes(message, 'UTF-8'))
        elif d['mode'] == 2:
            dataset_name = d['datasetName']
            dataset_source = d['datasetSource']
            print('Client response: ' + str(d['response']))
            self.annotation_manager.write_to_database(d, self.conn, dataset_name, dataset_source, uid, task)
        elif d['mode'] == 4:
            dataset_name = d['datasetName']
            dataset_source = d['datasetSource']
            print('client asks for the previous sample.')
            self.annotation_manager.reset_previous_sample(self.conn, dataset_name, dataset_source, uid, task)
        elif d['mode'] == 5:
            print('client asks for available datasets')
            message = self.overview_manager.get_datasets(self.conn, uid, task)
            self.csocket.send(bytes(message, 'UTF-8'))
        elif d['mode'] == 6:
            print('client asks for candidates for task EL')
            message = self.annotation_manager.get_candidates(d, self.aliases, self.aliases_lower, self.redirects, self.abstracts)
            self.csocket.send(bytes(message, 'UTF-8'))


    def create_connection(self):
        """ create a database connection to a SQLite database """
        self.conn = None
        try:
            self.conn = sqlite3.connect(self.db_file)
            print(sqlite3.version)
        except Error as e:
            print(e)

def main():
    config = json.load(open('../config/config.json'))


    print("Loading EL candidate data")

    aliases = json.load(open(config['aliases']))
    aliases_lower = json.load(open(config['aliases_lower']))
    redirects = json.load(open(config['redirects']))

    abstracts = json.load(open(config['abstracts_short']))

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((config['ip'], config['port']))

    print("Server started")
    print("Waiting for client request..")
    while True:
        server.listen(1)
        clientsock, clientAddress = server.accept()
        newthread = ClientThread(clientAddress, clientsock, config['database'], aliases, aliases_lower, redirects, abstracts)
        newthread.start()

if __name__ == "__main__":
    main()

