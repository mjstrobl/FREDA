import sqlite3
from sqlite3 import Error
import json

config = json.load(open('../config/config.json'))
print(config['database'])
conn = None
try:
    conn = sqlite3.connect(config['database'])
except Error as e:
    print(e)

cn = 0
filename = "/home/michi/sentences_25_100_7."
with open(filename + "txt") as f_txt:
    with open(filename + "id") as f_id:
        for line in f_id:
            line = line.strip()
            if len(line) > 0:
                spouse_relation_id = int(line)
                c = conn.cursor()
                rows = c.execute("select r.article, r.line from spouse r where r.id = ?;", (spouse_relation_id,)).fetchall()
                row = rows[0]
                article = row[0]
                line = row[1]
                print(row)
                c = conn.cursor()
                c.execute("insert into WEXEA_v1_CR_spouse_100 (article, line) VALUES (?, ?);", (article, line))
                cn += 1

c = conn.cursor()
c.execute("COMMIT;")
conn.close()

print(cn)