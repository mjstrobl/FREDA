import json

class AnnotationManager:

    def __init__(self):
        self.previous_article = {}
        self.previous_line = {}
        self.current_annotators = {}
        self.previous_annotators = {}
        self.previous_sentences_name = {}
        self.current_data = {}
        self.previous_response = {}

    def write_to_database(self, dict, conn, dataset_name, dataset_source, uid, task):
        if uid not in self.previous_article:
            self.previous_article[uid] = {}
            self.previous_line[uid] = {}
            self.previous_response[uid] = {}
            self.previous_sentences_name[uid] = {}

        self.previous_article[uid][task] = dict['article']
        self.previous_line[uid][task] = dict['line']
        self.previous_response[uid][task] = dict['response']
        self.previous_sentences_name[uid][task] = dataset_name
        self.previous_annotators = self.current_annotators

        cur = conn.cursor()

        if 'entities' in dict and uid in self.current_data:
            sentence_data = self.current_data[uid]
            sentence_data['annotations'].append(dict['entities'])

            sql = "UPDATE " + dataset_source + " SET data = ?, " + task + " = 1 WHERE article = ? AND line = ?;"
            cur.execute(sql, (json.dumps(sentence_data), dict['article'], dict['line']))

            dict['entities'] = dict['entities']['entities']

        if "annotator_1" not in self.current_annotators[uid]:
            sql = "UPDATE " + dataset_name + " SET annotator_1 = ?, data_1 = ?, response_1 = ? WHERE article = ? AND line = ?;"
        elif "annotator_2" not in self.current_annotators[uid]:
            sql = "UPDATE " + dataset_name + " SET annotator_2 = ?, data_2 = ?, response_2 = ? WHERE article = ? AND line = ?;"
        elif "annotator_3" not in self.current_annotators[uid]:
            sql = "UPDATE " + dataset_name + " SET annotator_3 = ?, data_3 = ?, response_3 = ? WHERE article = ? AND line = ?;"
        else:
            print("we already had 3 annotators here, this is a problem!!!")
            return

        new_dict = {}
        if 'entities' in dict:
            new_dict['entities'] = dict['entities']
        if 'subjects' in dict:
            new_dict['subjects'] = dict['subjects']
        if 'objects' in dict:
            new_dict['objects'] = dict['objects']

        cur.execute(sql, (dict['uid'], json.dumps(new_dict), dict['response'], dict['article'], dict['line']))

        response = dict['response']
        if response == -2:
            sql = "UPDATE " + dataset_source + " SET response = -2 WHERE article = ? AND line = ?;"
            cur.execute(sql, (dict['article'], dict['line']))

        conn.commit()

        print('updated database: article = ' + str(dict['article']) + ', line = ' + str(
            dict['line']) + ', response = ' + str(dict))

    def get_sample_from_database(self, conn, dataset_name, dataset_source, uid, task):
        cur = conn.cursor()

        sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + dataset_name + " r, " + dataset_source + " s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 " \
                                                                                                                      "AND annotator_1 != ? AND (annotator_2 != ? OR annotator_2 IS NULL) AND (annotator_3 != ? OR annotator_3 IS NULL) " \
                                                                                                                      "AND response_1 != -1 AND (response_2 != -1 OR response_2 IS NULL) AND (response_3 != -1 OR response_3 IS NULL) " \
                                                                                                                      "AND (response_2 IS NULL OR (response_1 != response_2 AND response_3 IS NULL)) LIMIT 1;"

        cur.execute(sql, (uid, uid, uid))
        rows = cur.fetchall()
        if len(rows) == 0:
            sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + dataset_name + " r, " + dataset_source + " s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 " \
                                                                                                                       "AND annotator_1 IS NULL AND (RE = 1 OR NER = 1 OR EL = 1 OR CR = 1) LIMIT 1;"
            cur.execute(sql)
            rows = cur.fetchall()

        if len(rows) == 0:
            sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + dataset_name + " r, " + dataset_source + " s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 AND annotator_1 IS NULL LIMIT 1;"
            cur.execute(sql)
            rows = cur.fetchall()


        '''sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + dataset_name + " r, sentence s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 " \
                                                                                                                      "AND (annotator_1 != ? OR annotator_1 IS NULL) AND (annotator_2 != ? OR annotator_2 IS NULL) AND (annotator_3 != ? OR annotator_3 IS NULL) " \
                                                                                                                      "AND (response_1 != -1 OR response_1 IS NULL) AND (response_2 != -1 OR response_2 IS NULL) AND (response_3 != -1 OR response_3 IS NULL) " \
                                                                                                                      "AND (response_1 IS NULL OR response_2 IS NULL OR (response_1 != response_2 AND response_3 IS NULL)) LIMIT 1;"'''

        print(rows)
        if len(rows) == 0:
            dict = {}
        else:
            row = rows[0]
            article = row[0]
            line = row[1]
            data = row[2]
            if uid not in self.current_annotators:
                self.current_annotators[uid] = {}

            if task not in self.current_annotators[uid]:
                self.current_annotators[uid][task] = {}

            annotator_1 = row[3]
            annotator_2 = row[4]
            annotator_3 = row[5]

            if annotator_1 != None:
                self.current_annotators[uid][task]["annotator_1"] = annotator_1
            if annotator_2 != None:
                self.current_annotators[uid][task]["annotator_2"] = annotator_2
            if annotator_3 != None:
                self.current_annotators[uid][task]["annotator_3"] = annotator_3

            if annotator_1 == None:
                annotator = 1
            elif annotator_2 == None:
                annotator = 2
            else:
                annotator = 3

            dict = json.loads(data)
            self.current_data = dict

            dict['article'] = article
            dict['line'] = line
            # TODO: We may need to consider filtering here, but for now it should be okay, although these entities do contain the task as well. Maybe we will introduce other tasks later that do not involve entity annotations.
            dict['entities'] = dict['annotations'][-1]
            dict['annotator'] = annotator

            print(dict)

        dict['mode'] = 1
        dict['task'] = task
        message = json.dumps(dict) + '\n'
        return message

    def reset_previous_sample(self, conn, dataset_name, dataset_source, uid, task):

        if uid not in self.previous_response or uid not in self.previous_line or uid not in self.previous_article or uid not in self.previous_annotators or self.previous_line[uid] == -1:
            return

        if task not in self.previous_response[uid] or task not in self.previous_line[uid] or task not in self.previous_article[uid] or task not in self.previous_annotators[uid]:
            return

        print("previous article: %s, line: %d", (self.previous_article[uid][task], self.previous_line[uid][task]))

        if "annotator_1" not in self.previous_annotators[uid][task]:
            sql = "UPDATE " + dataset_name + " SET annotator_1 = NULL, data_1 = NULL, response_1 = NULL WHERE article = ? AND line = ?;"
        elif "annotator_2" not in self.previous_annotators[uid][task]:
            sql = "UPDATE " + dataset_name + " SET annotator_2 = NULL, data_2 = NULL, response_2 = NULL WHERE article = ? AND line = ?;"
        elif "annotator_3" not in self.previous_annotators[uid][task]:
            sql = "UPDATE " + dataset_name + " SET annotator_3 = NULL, data_3 = NULL, response_3 = NULL WHERE article = ? AND line = ?;"
        else:
            print("we already had 3 annotators here, this is a problem!!!")
            return

        cur = conn.cursor()
        cur.execute(sql, (self.previous_article[uid][task], self.previous_line[uid][task]))

        if self.previous_response[uid][task] == -2:
            sql = "UPDATE " + dataset_source + " SET response = 0 WHERE article = ? AND line = ?;"
            cur.execute(sql, (self.previous_article[uid][task], self.previous_line[uid][task]))

        self.previous_line[uid][task] = -1

        conn.commit()

    def get_candidates(self, message, aliases, aliases_lower, redirects, abstracts):
        candidates = []
        mention = message['mention']

        if mention in aliases:
            candidates = aliases[mention]
        elif mention.lower() in aliases_lower:
            candidates = aliases_lower[mention.lower()]

        if mention in redirects and redirects[mention] not in candidates:
            candidates.insert(0,redirects[mention])

        if 'wikiName' in message and message['wikiName'] not in candidates:
            if message['wikiName'] in redirects:
                message['wikiName'] = redirects[message['wikiName']]
            candidates.insert(0, message['wikiName'])

        candidates_with_abstracts = []
        for candidate in candidates:
            if candidate in abstracts:
                candidates_with_abstracts.append([candidate,abstracts[candidate]])
            else:
                candidates_with_abstracts.append([candidate,""])

        message['candidates'] = candidates_with_abstracts
        print(message)
        message = json.dumps(message) + '\n'
        return message

