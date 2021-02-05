import sqlite3
from sqlite3 import Error
import json


config = json.load(open('config/config.json'))

conn = None
try:
    conn = sqlite3.connect(config['database'])
except Error as e:
    print(e)

c = conn.cursor()
rows = c.execute("select relation from relation").fetchall()

print("relation\t\tyes/no responses\n")

for row in rows:
    relation = row[0]
    sql = "SELECT COUNT(*) FROM " + relation + " WHERE annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL and response_2 > -1 AND (annotator_3 IS NOT NULL OR response_1 = response_2);"
    print(relation + "\t\t" + str(c.execute(sql).fetchall()[0][0]))

conn.close()