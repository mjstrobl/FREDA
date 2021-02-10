import json
import sqlite3
from sqlite3 import Error
import re
import glob
import random
import math

from stanza.server import CoreNLPClient


class RelationManager:

    def add_relations(self, relations, keyword_mapping, conn, config, max_relation_counter):
        props = {"ssplit.isOneSentence": True}
        annotators = ['tokenize', 'ssplit', 'pos', 'lemma', 'ner']
        client = CoreNLPClient(
            annotators=annotators,
            properties=props,
            timeout=60000, endpoint="http://localhost:9000", start_server=False, memory='16g')


        try:
            c = conn.cursor()

            relation_counter = {}
            for tuple in relations:
                relation = tuple[0]

                c.execute("CREATE TABLE IF NOT EXISTS " + relation + "_temp (article TEXT, line INTEGER, type TEXT, PRIMARY KEY (article, line));")

                relation_counter[relation] = {}

            self.fill_relation_table(keyword_mapping, conn, relation_counter, config, max_relation_counter, client, props, annotators)

            for tuple in relations:
                relation = tuple[0]
                unidirectional = tuple[1]
                info = tuple[2]
                sql = '''INSERT OR IGNORE INTO relation (relation, unidirectional, info, status) VALUES (?, ?, ?, 1)'''

                c.execute(sql, (relation, unidirectional, info))

                c.execute("CREATE TABLE IF NOT EXISTS " + relation + " (id INTEGER PRIMARY KEY AUTOINCREMENT, article TEXT, line INTEGER, type TEXT, annotator_1 TEXT, response_1 INTEGER, data_1 TEXT, annotator_2 TEXT, response_2 INTEGER, data_2 TEXT, annotator_3 TEXT, response_3 INTEGER, data_3 TEXT);")
                c.execute("INSERT OR IGNORE INTO " + relation + " (article, line, type) SELECT * from " + relation + "_temp ORDER BY random();")
                c.execute("DROP TABLE " + relation + "_temp;")

            conn.commit()
        except Error as e:
            print(e)

    def create_article_list(self,wexea_path):
        articles = []
        article_directories = glob.glob(wexea_path)
        for article_directory in article_directories:
            articles.extend(glob.glob(article_directory + "*.txt"))

        print("found " + str(len(articles)) + ' articles.')
        random.shuffle(articles)
        print('articles shuffled.')
        return articles

    def create_keyword_lists(self, keyword_mapping, relation_counter, max_relation_counter):
        regexes = []
        relation_lists = []
        keywords = []
        for keyword in keyword_mapping:
            relations = keyword_mapping[keyword]
            relations_to_keep = []
            for relation in relations:
                if keyword not in relation_counter[relation]:
                    relation_counter[relation][keyword] = 0
                if relation_counter[relation][keyword] < max_relation_counter[relation]:
                    relations_to_keep.append(relation)
            if len(relations_to_keep) > 0:
                relation_lists.append(relations_to_keep)
                keywords.append(keyword)
                regexes.append(re.compile(r'\b' + keyword + r'\b', re.IGNORECASE))

        print("recreated regexes, now we have: " + str(len(regexes)))
        return regexes, relation_lists, keywords

    def fill_relation_table(self, keyword_mapping, conn, relation_counter, config, max_relation_counter, client, props, annotators):
        regexes, relation_lists, keywords = self.create_keyword_lists(keyword_mapping, relation_counter, max_relation_counter)

        inserts = 0
        current_articles = 0
        num_articles = 0
        articles = self.create_article_list(config['wexea_path'])
        for article in articles:
            num_articles += 1
            current_articles += 1
            if current_articles > 1000:
                regexes, relation_lists, keywords = self.create_keyword_lists(keyword_mapping, relation_counter, max_relation_counter)
                current_articles = 0
                if len(regexes) == 0:
                    break
            with open(article) as f:
                num_line = 0
                for sentence in f:
                    sentence = sentence.strip()
                    if len(sentence.split()) < 50 and len(sentence) > 0 and sentence[0] != '=' and "[[" in sentence:
                        for i in range(len(regexes)):
                            regex = regexes[i]
                            m = re.search(regex, sentence)
                            if m != None:
                                self.process_sentence(article, num_line, relation_lists[i], keywords[i], sentence, conn, relation_counter, client, props, annotators, max_relation_counter)
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


    def process_sentence(self, article, num_line, relations, keyword, line, conn, relation_counter, client, props, annotators, max_relation_counter):
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

                before += alias
                idx = len(before)

                line = before + line[end + 2:]
            else:
                break

        if len(entities) == 0:
            return

        entities_list = []
        annotation = client.annotate(line,properties=props,annotators=annotators)
        for i, sent in enumerate(annotation.sentence):
            for mention in sent.mentions:
                ner = mention.ner
                tokens = sent.token[mention.tokenStartInSentenceInclusive:mention.tokenEndInSentenceExclusive]
                start = tokens[0].beginChar
                end = tokens[-1].endChar

                mention_name = line[start:end]
                if ner == "DATE" and len(mention.timex.value) > 0 and mention.timex.value[0].isdigit() and \
                        mention.timex.value[-1].isdigit():
                    mention_range = set(range(start, end))
                    if len(mention_range.intersection(positions_annotated)) == 0:
                        entities_list.append({"wiki_name": mention_name, "positions": [[start, end - start]]})

        for entity in entities:
            positions = entities[entity]
            entities_list.append({"wiki_name": entity, "positions": positions})

        d = {'sentence': line, 'annotations': [entities_list]}
        json_s = json.dumps(d)

        sql = "INSERT OR IGNORE INTO sentence(article, line, data, status) VALUES(?, ?, ?, 1)"
        cur = conn.cursor()
        cur.execute(sql, (article, num_line, json_s))

        for relation in relations:
            if relation_counter[relation][keyword] < max_relation_counter[relation]:
                relation_counter[relation][keyword] += 1
                sql = "INSERT OR IGNORE INTO " + relation + "_temp (article, line, type) VALUES(?, ?, ?)"
                cur = conn.cursor()
                cur.execute(sql, (article, num_line, keyword))

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
    config = json.load(open('config/config.json'))
    print(config)

    conn = create_connection(config['database'])
    c = conn.cursor()
    existing_relations = set()
    sql = "SELECT relation from relation;"
    rows = c.execute(sql).fetchall()
    c.close()
    for row in rows:
        relation = row[0]
        if relation == "colleague" or relation == "diagnosed_with" or relation == "related_to" or relation == "owns" or relation == "knows":
            continue
        existing_relations.add(row[0])

    keywords_json = json.load(open('database/keywords.json'))
    keyword_mapping = {}
    relations = []
    max_relation_counter = {}
    for w in keywords_json:
        relation = w['relation']
        if relation not in existing_relations:
            print(relation)
            unidirectional = w['unidirectional']
            keywords = w['keywords']
            info = w['info']
            relations.append((relation,unidirectional,info))
            max_relation_counter[relation] = math.ceil(config['num_max_sentences'] / len(keywords))
            for word in keywords:
                if word not in keyword_mapping:
                    keyword_mapping[word] = []
                keyword_mapping[word].append(relation)

    print('found ' + str(len(keyword_mapping)) + " keywords.")


    relation_manager = RelationManager()

    if conn == None:
        print("connection could not be established.")
    else:
        relation_manager.add_relations(relations, keyword_mapping, conn, config, max_relation_counter)

    conn.close()

if __name__ == "__main__":
    main()
