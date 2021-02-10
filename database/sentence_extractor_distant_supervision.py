import json
import sqlite3
from sqlite3 import Error
import re
import glob
import random
from stanza.server import CoreNLPClient
import jaydebeapi

class DSManager:

    def add_relations(self, relations, entities, conn, max_relation_counter, wexea_path):
        props = {"ssplit.isOneSentence": True}
        annotators = ['tokenize', 'ssplit', 'pos', 'lemma', 'ner']
        client = CoreNLPClient(
            annotators=annotators,
            properties=props,
            timeout=60000, endpoint="http://localhost:9000", start_server=False, memory='16g')

        try:
            c = conn.cursor()

            relation_counter = {}
            for relation in relations:
                relation_temp = relation + "_temp"
                c.execute("CREATE TABLE IF NOT EXISTS " + relation_temp + " (article TEXT, line INTEGER, type TEXT, PRIMARY KEY (article, line));")
                relation_counter[relation] = 0


            self.fill_relation_table(entities, conn, relation_counter, max_relation_counter, wexea_path, client, props, annotators)

            for relation in relations:
                relation_temp = relation + "_temp"
                c.execute("INSERT OR IGNORE INTO " + relation_temp + " SELECT article,line,type from " + relation + ";")
                c.execute("DROP TABLE " + relation + ";")
                c.execute("CREATE TABLE " + relation + " (id INTEGER PRIMARY KEY AUTOINCREMENT, article TEXT, line INTEGER, type TEXT, annotator_1 TEXT, response_1 INTEGER, data_1 TEXT, annotator_2 TEXT, response_2 INTEGER, data_2 TEXT, annotator_3 TEXT, response_3 INTEGER, data_3 TEXT);")
                c.execute("INSERT OR IGNORE INTO " + relation + " (article, line, type) SELECT * from " + relation_temp + " ORDER BY random();")
                c.execute("DROP TABLE " + relation_temp + ";")

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

    def create_entity_lists(self, entities, relation_counter, max_relation_counter):
        new_entities = {}

        for subject in entities:
            tuples = entities[subject]
            for tuple in tuples:
                relation = tuple[1]
                if relation_counter[relation] < max_relation_counter:
                    if subject not in new_entities:
                        new_entities[subject] = []
                    new_entities[subject].append(tuple)

        print("recreated entities, now we have: " + str(len(new_entities)))
        print(relation_counter)
        return new_entities

    def fill_relation_table(self, entities, conn, relation_counter, max_relation_counter,wexea_path, client, props, annotators):
        actual_entities = self.create_entity_lists(entities, relation_counter, max_relation_counter)

        inserts = 0
        current_articles = 0
        num_articles = 0
        articles = self.create_article_list(wexea_path)
        for article in articles:
            num_articles += 1
            current_articles += 1
            if current_articles > 100000:
                actual_entities = self.create_entity_lists(entities, relation_counter, max_relation_counter)
                current_articles = 0
                if len(actual_entities) == 0:
                    break
            with open(article) as f:
                num_line = 0
                for sentence in f:
                    sentence = sentence.strip()
                    if len(sentence.split()) < 50 and len(sentence) > 0 and sentence[0] != '=' and "[[" in sentence:
                        c = self.process_sentence(article, num_line, actual_entities, sentence, conn, relation_counter, client, props, annotators, max_relation_counter)
                        inserts += c

                        if inserts % 1000 == 0:
                            conn.commit()
                        break

                    num_line += 1

            print(str(num_articles) + ' num_articles, ' + str(inserts) + ' inserts.',end='\r')

        print(relation_counter)
        print(str(num_articles) + ' num_articles.')
        print(str(inserts) + ' inserts.')
        conn.commit()


    def process_sentence(self, article, num_line, actual_entities, line, conn, relation_counter, client, props, annotators, max_relation_counter):
        line = re.sub(' +', ' ', line).strip()

        entities = {}

        idx = 0
        positions_annotated = set()
        entities_found = set()

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
                    return 0

                entity = mention_parts[0]

                if entity in actual_entities:
                    entities_found.add(entity)

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

        if len(entities_found) == 0:
            return 0

        current_relations = set()

        for entity in entities_found:
            tuples = actual_entities[entity]
            for tuple in tuples:
                object = tuple[0]
                relation = tuple[1]
                if object in entities:
                    current_relations.add(relation)

        if len(current_relations) == 0:
            return 0

        entities_list = []
        annotation = client.annotate(line, properties=props, annotators=annotators)
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

        entities_list = []
        annotation = client.annotate(line, properties=props, annotators=annotators)
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

        for relation in current_relations:
            if relation_counter[relation] < max_relation_counter:
                relation_counter[relation] += 1
                sql = "INSERT OR IGNORE INTO " + relation + "_temp (article, line, type) VALUES(?, ?, ?)"
                cur = conn.cursor()
                cur.execute(sql, (article, num_line, "distant_supervision"))

        return 1

def create_connection(db_filename):
    """ create a database connection to a SQLite database """
    conn = None
    try:
        conn = sqlite3.connect(db_filename)
        print(sqlite3.version)
    except Error as e:
        print(e)

    return conn

def clean_entity(entity):
    entity = entity.split("/")[-1]
    entity = entity.split("_(")[0]
    entity = entity.replace("_", " ")

    return entity

def query_tdb(relation, queries, conn_tdb, entities):
    c = conn_tdb.cursor()

    for query in queries:
        c.execute(query)
        rows = c.fetchall()
        for row in rows:
            subject = row[0]
            object = row[1]
            if len(subject) > 0 and len(object) > 0:
                subject = clean_entity(subject)
                object = clean_entity(object)
                if subject not in entities:
                    entities[subject] = []
                entities[subject].append((object, relation))

    c.close()

def main():
    config = json.load(open('config/config.json'))
    print(config)

    jclass = "org.apache.jena.jdbc.JenaJDBC"
    conn_string = "jdbc:jena:tdb:location=" + config['tdbstore']
    conn_tdb = jaydebeapi.connect(jclass, conn_string,jars=config['jena_jdbc'])

    conn = create_connection(config['database'])
    c = conn.cursor()

    entities = {}
    relations = []
    distant_supervision_queries = json.load(open('database/queries.json'))
    for d in distant_supervision_queries:
        relation = d['relation']
        queries = d['queries']

        try:
            sql = "SELECT COUNT(*) FROM " + relation + " WHERE type = \"distant_supervision\";"
            n = c.execute(sql).fetchall()[0][0]
            if n == 0:
                query_tdb(relation,queries,conn_tdb,entities)
                relations.append(relation)
            else:
                continue
        except Error as e:
            print("Relation \"" + relation + "\" not available.")

    conn_tdb.close()

    print('found ' + str(len(entities)) + " entities.")

    conn = create_connection(config['database'])
    relation_manager = DSManager()

    if conn == None:
        print("connection could not be established.")
    else:
        relation_manager.add_relations(relations, entities, conn, config['num_max_sentences'], config['wexea_path'])

    conn.close()

if __name__ == "__main__":
    main()
