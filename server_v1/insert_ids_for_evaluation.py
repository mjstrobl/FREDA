import sqlite3
from sqlite3 import Error
import json
import random

config = json.load(open('../config/config.json'))

conn = None
try:
    conn = sqlite3.connect(config['database'])
except Error as e:
    print(e)

with open('/home/michi/Desktop/brat-v1.3_Crunchy_Frog/data/freda_collection/sentences_25_100_5.id') as f:
    for line in f:
        line = line.strip()
        if len(line) > 0:
            id = int(line)
            statement = 'INSERT INTO frances_25_100_5 SELECT * FROM spouse WHERE id = ' + str(id) + ';'
            print(statement)
            c = conn.cursor()
            c.execute(statement)
conn.commit()
conn.close()