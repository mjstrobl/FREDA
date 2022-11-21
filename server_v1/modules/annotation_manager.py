import json
import time

class AnnotationManager:

    def __init__(self):
        self.previous_article = ""
        self.previous_line = -1
        #self.previous_response = -3
        self.current_relation = ""
        self.current_annotators = {}
        self.previous_annotators = {}
    def write_to_database(self, dict, conn):
        self.previous_article = dict['article']
        self.previous_line = dict['line']
        self.previous_response = dict['response']
        self.previous_annotators = self.current_annotators

        cur = conn.cursor()

        if "annotator_1" not in self.current_annotators:
            sql = "UPDATE " + dict['relation'] + " SET annotator_1 = ?, data_1 = ?, response_1 = ? WHERE article = ? AND line = ?;"
        elif "annotator_2" not in self.current_annotators:
            sql = "UPDATE " + dict['relation'] + " SET annotator_2 = ?, data_2 = ?, response_2 = ? WHERE article = ? AND line = ?;"
        elif "annotator_3" not in self.current_annotators:
            sql = "UPDATE " + dict['relation'] + " SET annotator_3 = ?, data_3 = ?, response_3 = ? WHERE article = ? AND line = ?;"
        else:
            print("we already had 3 annotators here, this is a problem!!!")
            return

        cur.execute(sql,(dict['uid'], json.dumps(dict), dict['response'], dict['article'], dict['line']))

        if 'entities' in dict:
            sentence_data = self.current_data
            sentence_data['annotations'].append(dict['entities'])

            sql = "UPDATE sentence SET data = ? WHERE article = ? AND line = ?;"
            cur.execute(sql,(json.dumps(sentence_data), dict['article'], dict['line']))

        response = dict['response']
        if response == -2:
            sql = "UPDATE sentence SET response = -2 WHERE article = ? AND line = ?;"
            cur.execute(sql, (dict['article'], dict['line']))

        conn.commit()

        print('updated database: article = ' + str(dict['article']) + ', line = ' + str(dict['line']) + ', response = ' + str(dict))

    def get_sample_from_database(self, conn, relation_name, uid):
        cur = conn.cursor()

        sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + relation_name + " r, sentence s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 " \
                "AND (annotator_1 != ? OR annotator_1 IS NULL) AND (annotator_2 != ? OR annotator_2 IS NULL) AND (annotator_3 != ? OR annotator_3 IS NULL) " \
                "AND (response_1 != -1 OR response_1 IS NULL) AND (response_2 != -1 OR response_2 IS NULL) AND (response_3 != -1 OR response_3 IS NULL) " \
                "AND (response_1 IS NULL OR response_2 IS NULL OR (response_1 != response_2 AND response_3 IS NULL)) LIMIT 1;"

        start = time.time()
        cur.execute(sql,(uid,uid,uid))
        end = time.time()
        print(end - start)

        rows = cur.fetchall()

        self.current_relation = relation_name

        print(rows)
        if len(rows) == 0:
            dict = {}
        else:
            row = rows[0]
            article = row[0]
            line = row[1]
            data = row[2]
            self.current_annotators = {}
            annotator_1 = row[3]
            annotator_2 = row[4]
            annotator_3 = row[5]

            if annotator_1 != None:
                self.current_annotators["annotator_1"] = annotator_1
            if annotator_2 != None:
                self.current_annotators["annotator_2"] = annotator_2
            if annotator_3 != None:
                self.current_annotators["annotator_3"] = annotator_3

            if annotator_1 == None:
                annotator = 1
            elif annotator_2 == None:
                annotator = 2
            else:
                annotator = 3


            #self.previous_response_relation = row[3]
            dict = json.loads(data)
            self.current_data = dict

            dict['article'] = article
            dict['line'] = line
            dict['entities'] = dict['annotations'][0]
            dict['subjects'] = []
            dict['objects'] = []
            dict['annotator'] = annotator

            print(dict)

        dict['mode'] = 1
        message = json.dumps(dict) + '\n'
        return message

    def reset_previous_sample(self, conn, relation_name, uid):

        if self.previous_line == -1:
            return

        print("previous article: %s, line: %d", (self.previous_article,self.previous_line))

        if "annotator_1" not in self.previous_annotators:
            sql = "UPDATE " + relation_name + " SET annotator_1 = NULL, data_1 = NULL, response_1 = NULL WHERE article = ? AND line = ?;"
        elif "annotator_2" not in self.previous_annotators:
            sql = "UPDATE " + relation_name + " SET annotator_2 = NULL, data_2 = NULL, response_2 = NULL WHERE article = ? AND line = ?;"
        elif "annotator_3" not in self.previous_annotators:
            sql = "UPDATE " + relation_name + " SET annotator_3 = NULL, data_3 = NULL, response_3 = NULL WHERE article = ? AND line = ?;"
        else:
            print("we already had 3 annotators here, this is a problem!!!")
            return

        cur = conn.cursor()
        cur.execute(sql, (self.previous_article, self.previous_line))

        if self.previous_response == -2:
            sql = "UPDATE sentence SET response = 0 WHERE article = ? AND line = ?;"
            cur.execute(sql, (self.previous_article, self.previous_line))

        self.previous_line = -1


        conn.commit()


def main():
    config = json.load(open('../config.json'))
    print(config)

if __name__ == "__main__":
    main()