import socket, threading
import json
import sqlite3
from json.decoder import JSONDecodeError
from sqlite3 import Error

from modules.annotation_manager import AnnotationManager
from modules.relation_manager import RelationManager

class ClientThread(threading.Thread):
    def __init__(self,clientAddress,clientsocket,db_file):
        threading.Thread.__init__(self)
        self.csocket = clientsocket
        self.idx = 0
        self.db_file = db_file
        self.previous_id = -1
        self.clientAddress = clientAddress
        self.annotation_manager = AnnotationManager()
        self.relation_manager = RelationManager()
        print ("New connection added: ", self.clientAddress)
    def run(self):
        print ("Connection from : ", self.clientAddress)
        self.create_connection()
        errors = 0
        loops = 0
        while True:
            loops += 1
            if loops > 5:
                #TODO: This is a hack that works, but has to be replaced soon. The problem is that sometimes messages from the client contain a newline, if multiple messages are sent at once.
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
        errors = 0

        # This can be used to message a specific device, e.g. in case they need to update the app.
        if 'uid' in d and d['uid'] == "<SPECIFIC UID>" :
            message = {"mode": -3, "message": "<MESSAGE TO CLIENT SHOWN AS A TOAST>"}
            print(message)
            message = json.dumps(message) + '\n'
            self.csocket.send(bytes(message, 'UTF-8'))
            return

        if d['mode'] == 0:
            return
        elif d['mode'] == 1:
            relation_name = d['relation']
            uid = d['uid']
            print("client asks for sample.")
            message = self.annotation_manager.get_sample_from_database(self.conn, relation_name, uid)
            self.csocket.send(bytes(message, 'UTF-8'))
        elif d['mode'] == 2:
            print('Client response: ' + str(d['response']))
            self.annotation_manager.write_to_database(d, self.conn)
        elif d['mode'] == 4:
            relation_name = d['relation']
            uid = d['uid']
            print('client asks for the previous sample.')
            self.annotation_manager.reset_previous_sample(self.conn, relation_name, uid)
        elif d['mode'] == 5:
            print('client asks for available relations')
            uid = d['uid']
            message = self.relation_manager.get_relations(self.conn, uid)
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
    config = json.load(open('config/config.json'))

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((config['ip'], config['port']))

    print("Server started")
    print("Waiting for client request..")
    while True:
        server.listen(1)
        clientsock, clientAddress = server.accept()
        newthread = ClientThread(clientAddress, clientsock, config['database'])
        newthread.start()

if __name__ == "__main__":
    main()



