import json
import random
import sqlite3
from json.decoder import JSONDecodeError
from sqlite3 import Error

database_filename = '/media/michi/Data/datasets/freda/main.db'
coref_data_filename = '/application/app/src/main/assets/ner.json'

conn = None
try:
    conn = sqlite3.connect(database_filename)
    print(sqlite3.version)
except Error as e:
    print(e)

coref_data = {
  "datasets": [
    {"name": "spouse",
      "annotations_1": 0, "annotations_2": 0, "annotations_3": 0, "once": 0, "twice": 0, "full": 0, "types": ["PER","LOC","ORG","MISC"], "system":"flat"},
    {"name": "ceo_of",
      "annotations_1": 0, "annotations_2": 0, "annotations_3": 0, "once": 0, "twice": 0, "full": 0, "types": ["PER","LOC","ORG","MISC"], "system":"flat"},
    {"name": "date_of_birth",
      "annotations_1": 0, "annotations_2": 0, "annotations_3": 0, "once": 0, "twice": 0, "full": 0, "types":
         {"PER":["ACTOR","DOCTOR","ATHLETE","POLITICIAN","PER"],
          "LOC":["CITY","COUNTRY","STATE","LOC"],
          "ORG":["airline","company","military","government","ORG"]},
     "system":"hierarchy"}
  ]
}

relations = ['spouse','ceo_of','date_of_birth']
for d in relations:
    dataset_name = d
    cur = conn.cursor()

    sql = "SELECT r.article, r.line, s.data, r.annotator_1, r.annotator_2, r.annotator_3  FROM " + dataset_name + " r, sentence s WHERE r.article = s.article AND r.line = s.line AND s.response != -2 " \
                                                                                                                   "AND (annotator_1 IS NULL) LIMIT 10000;"
    cur.execute(sql)
    rows = cur.fetchall()

    result = []

    for row in rows:
        article = row[0]
        line = row[1]
        data = row[2]
        current_annotators = {}
        annotator_1 = row[3]
        annotator_2 = row[4]
        annotator_3 = row[5]

        if annotator_1 != None:
            current_annotators["annotator_1"] = annotator_1
        if annotator_2 != None:
            current_annotators["annotator_2"] = annotator_2
        if annotator_3 != None:
            current_annotators["annotator_3"] = annotator_3

        if annotator_1 == None:
            annotator = 1
        elif annotator_2 == None:
            annotator = 2
        else:
            annotator = 3

        # self.previous_response_relation = row[3]
        dict = json.loads(data)
        current_data = dict

        dict['article'] = article
        dict['line'] = line
        dict['entities'] = dict['annotations'][0]
        dict['annotator'] = annotator

        dict['mode'] = 1
        if dict['sentence'][-1] == '.' and len(result) < 1000:
            result.append(dict)

    random.shuffle(result)
    coref_data[dataset_name] = result

dsf = 0

with open(coref_data_filename,'w') as f:
    json.dump(coref_data,f)

conn.close()