import json
import sqlite3
from sqlite3 import Error
import re
import glob
import random
import datefinder

class RelationManager:

    def get_relations(self, conn, uid):
        c = conn.cursor()
        c.execute("SELECT * FROM relation WHERE status = 1;")

        relations = []
        for row in c:
            name = row[0]
            #keywords = json.loads(row[1])
            response = json.loads(row[2])
            info_short = row[4]
            relations.append({"name": name, 'response': response, "info_short": info_short})

        print("found relations")
        print(relations)

        for relation in relations:
            relation_name = relation['name']
            cur = conn.cursor()
            #sql = "SELECT COUNT(a.id) FROM annotation a, sentence s, " + relation_name + " r WHERE a.response > -1 AND s.response > -2 AND r.responses > -1 AND a.article = s.article AND s.article = r.article AND a.line = s.line AND s.line = r.line AND a.name = ? AND a.relation = ?;"

            sql_annotator_1 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_1 = ? AND r.response_1 > -1 " \
                                "AND (response_2 IS NULL OR response_2 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_2 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_2 = ? AND r.response_2 > -1 " \
                                "AND (response_1 IS NULL OR response_1 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_3 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_3 = ? AND r.response_3 > -1 " \
                                                                                    "AND (response_1 IS NULL OR response_1 > -1) AND (response_2 IS NULL OR response_2 > -1);"

            sql_annotator_once = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                                                                                        "AND r.annotator_1 IS NOT NULL AND response_1 > -1;"

            sql_annotator_twice = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                    "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1;"

            sql_annotator_full = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                                 "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1 AND (response_1 = response_2 OR (annotator_3 IS NOT NULL AND response_3 > -1));"


            cur.execute(sql_annotator_1, (uid,))
            relation['annotations_1'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_2, (uid,))
            relation['annotations_2'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_3, (uid,))
            relation['annotations_3'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_once)
            relation['once'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_twice)
            relation['twice'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_full)
            relation['full'] = cur.fetchall()[0][0]

            print("found info for relation: " + relation_name)
            print(relation)

        message = {"mode":5,"relations":relations}

        print(message)

        message = json.dumps(message) + '\n'
        return message

    def add_relations(self, relations, keyword_mapping, conn):
        try:
            c = conn.cursor()
            c.execute("CREATE TABLE IF NOT EXISTS sentence(article TEXT, line INTEGER, data TEXT, PRIMARY KEY (article, line));")
            c.execute("CREATE TABLE IF NOT EXISTS annotation(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, data TEXT, relation TEXT, article TEXT, line INTEGER, response INTEGER);")
            c.execute("CREATE TABLE IF NOT EXISTS relation(name TEXT PRIMARY KEY, keywords TEXT, response TEXT, unidirectional INTEGER);")

            relation_counter = {}
            for relation in relations:
                name = relation[0]
                unidirectional = relation[1]
                keywords = relation[2]
                response = relation[3]
                sql = '''INSERT OR IGNORE INTO relation (name, keywords, response, unidirectional) VALUES (?, ?, ?, ?)'''

                c.execute(sql, ((name, json.dumps(keywords), json.dumps(response), unidirectional)))
                c.execute("CREATE TABLE IF NOT EXISTS " + name + "_temp (article TEXT, line INTEGER, type TEXT, PRIMARY KEY (article, line));")

                relation_counter[name] = 0

            self.fill_relation_table(keyword_mapping, conn, relation_counter)


            for relation in relations:
                name = relation[0]
                c.execute("CREATE TABLE IF NOT EXISTS " + name + " (article TEXT, line INTEGER, PRIMARY KEY (article, line));")
                c.execute("INSERT OR IGNORE INTO " + name + " SELECT * from " + name + "_temp ORDER BY random();")
                c.execute("DROP TABLE " + name + "_temp;")

            conn.commit()
        except Error as e:
            print(e)

    def create_article_list(self):
        articles = []
        article_directories = glob.glob("/media/michi/Data/latest_wiki/final_articles/*/")
        for article_directory in article_directories:
            articles.extend(glob.glob(article_directory + "*.txt"))

        print("found " + str(len(articles)) + ' articles.')
        random.shuffle(articles)
        print('articles shuffled.')
        return articles

    def create_keyword_lists(self, keyword_mapping, relation_counter):
        regexes = []
        relation_lists = []
        keywords = []
        for keyword in keyword_mapping:
            relations = keyword_mapping[keyword]
            relations_to_keep = []
            for relation in relations:
                if relation_counter[relation] < 10000:
                    relations_to_keep.append(relation)
            if len(relations_to_keep) > 0:
                relation_lists.append(relations_to_keep)
                keywords.append(keyword)
                regexes.append(re.compile(r'\b' + keyword + r'\b', re.IGNORECASE))

        print("recreated regexes, now we have: " + str(len(regexes)))
        return regexes, relation_lists, keywords

    def fill_relation_table(self, keyword_mapping, conn, relation_counter):
        regexes, relation_lists, keywords = self.create_keyword_lists(keyword_mapping, relation_counter)

        inserts = 0
        current_articles = 0
        num_articles = 0
        articles = self.create_article_list()
        for article in articles:
            num_articles += 1
            current_articles += 1
            if current_articles > 1000:
                regexes, relation_lists, keywords = self.create_keyword_lists(keyword_mapping, relation_counter)
                current_articles = 0
                if len(regexes) == 0:
                    break
            with open(article) as f:
                num_line = 0
                for sentence in f:
                    sentence = sentence.strip()
                    if len(sentence.split()) < 50 and len(sentence) > 0 and sentence[0] != '=':
                        for i in range(len(regexes)):
                            regex = regexes[i]
                            m = re.search(regex, sentence)
                            if m != None:
                                start_regex = m.start()
                                self.process_sentence(article, num_line, relation_lists[i], sentence, start_regex, conn, relation_counter)
                                inserts += 1

                                if inserts % 1000 == 0:
                                    conn.commit()
                                break

                    num_line += 1

            print(str(num_articles) + ' num_articles, ' + str(inserts) + ' inserts.',end='\r')

        print(relation_counter)
        print(str(num_articles) + ' num_articles.')
        print(str(inserts) + ' inserts.')
        conn.commit()


    def process_sentence(self, article, num_line, relations, line, start_regex, conn, relation_counter):
        #line = re.sub(r'\([^)]*\)', '', line)
        line = re.sub(' +', ' ', line).strip()

        entities = {}

        idx = 0
        positions_annotated = set()
        while True:
            start = line[idx:].find('[[')
            end = line[idx:].find(']]')
            if start > -1 and end > -1:
                start += idx
                end += idx
                mention = line[start + 2:end]
                mention_parts = mention.split("|")
                before = line[:start]

                if len(mention_parts) != 3:
                    return

                entity = mention_parts[0]
                alias = mention_parts[1]
                mention_type = mention_parts[2]

                if "DISAMBIGUATION" not in mention_type and (mention_type != "UNKNOWN" or " " in alias):
                    if entity not in entities:
                        entities[entity] = []

                    for j in range(len(before), len(before) + len(alias)):
                        positions_annotated.add(j)

                    entities[entity].append((len(before), len(alias)))

                if len(before) < start_regex:
                    shortened_by = (end + 2 - start) - len(alias)
                    start_regex -= shortened_by

                before += alias
                idx = len(before)

                line = before + line[end + 2:]
            else:
                break

        #date finder here.
        matches = datefinder.find_dates(line, index=True)

        for match in matches:
            start = match[1][0]
            end = match[1][1]

            if line[end-1] == ',' or line[end-1] == ".":
                end -= 1

            if start in positions_annotated:
                continue

            if end < len(line) and line[end] == 's':
                end += 1

            if line[start] == ' ':
                start += 1

            if line[end - 1] == ' ':
                end -= 1

            for j in range(start,end):
                positions_annotated.add(j)

            entities[line[start:end]] = [(start,end-start)]

        for match in re.finditer(r'\d+', line):
            start = match.start()
            end = match.end()

            if start in positions_annotated:
                continue

            if end < len(line) and line[end] == 's':
                end += 1

            if start not in positions_annotated:
                entities[line[start:end]] = [(start, end - start)]


        subject_min = -1
        object_max = 1000000

        subjects = []
        objects = []
        entities_list = []
        for entity in entities:
            positions = entities[entity]
            for position in positions:
                start = position[0]

                if start < start_regex:
                    if start > subject_min:
                        subject_min = start
                        subjects.append(len(entities_list))
                else:
                    if start < object_max:
                        object_max = start
                        objects.append(len(entities_list))

            entities_list.append({"wiki_name": entity, "positions": positions})

        if len(entities_list) > 0:
            d = {'sentence': line, 'subjects': subjects, 'objects': objects, 'entities': entities_list}
            json_s = json.dumps(d)

            sql = "INSERT OR IGNORE INTO sentence(article, line, data) VALUES(?, ?, ?)"
            cur = conn.cursor()
            cur.execute(sql, (article, num_line, json_s))

            for name in relations:
                relation_counter[name] += 1
                sql = "INSERT OR IGNORE INTO " + name + "_temp (article, line, type) VALUES(?, ?, ?)"
                cur = conn.cursor()
                cur.execute(sql, (article, num_line, "keyword"))

def create_connection(db_filename):
    """ create a database connection to a SQLite database """
    conn = None
    try:
        conn = sqlite3.connect(db_filename)
        print(sqlite3.version)
    except Error as e:
        print(e)

    return conn

def main():
    config = json.load(open('../config.json'))
    print(config)

    relations_to_consider = {"related_to"}


    keywords_json = json.load(open('../keywords.json'))
    keyword_mapping = {}
    relations = []
    for w in keywords_json:
        relation = w['relation']
        if relation in relations_to_consider or len(relations_to_consider) == 0:
            unidirectional = w['unidirectional']
            keywords = w['keywords']
            response = w['response']
            relations.append((relation,unidirectional,keywords, response))

            for word in keywords:
                if word not in keyword_mapping:
                    keyword_mapping[word] = []
                keyword_mapping[word].append(relation)

    print('found ' + str(len(keyword_mapping)) + " keywords.")

    conn = create_connection(config['database'])
    relation_manager = RelationManager()

    if conn == None:
        print("connection could not be established.")
    else:
        relation_manager.add_relations(relations, keyword_mapping, conn)

    conn.close()

if __name__ == "__main__":
    main()