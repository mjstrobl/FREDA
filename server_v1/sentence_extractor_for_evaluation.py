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

c = conn.cursor()
rows = c.execute("select s.data, r.id from sentence s, spouse r where s.line = r.line and s.article = r.article and r.annotator_1 is null and s.response = 0 ORDER BY RANDOM();").fetchall()
length = {}
sentences = []
ids = []
sentence_length = 25
set_length = 100


for row in rows:
    data = json.loads(row[0])
    id = int(row[1])
    sentence = data['sentence']
    count = sentence.count(' ')
    if count not in length:
        length[count] = 0
    length[count] += 1

    if count == 25:
        sentences.append(sentence)
        ids.append(id)
        #file.write(sentence + '\n\n')

        #if length[25] == 100:
        #    break

#random.shuffle(sentences)
print(int(len(sentences) / set_length))
for i in range(int(len(sentences) / set_length)):
    print(i)
    filename = '/home/michi/sentences_' + str(sentence_length) + '_' + str(set_length) + '_' + str(i) +'.txt'
    with open(filename, 'w') as file:
        filename = '/home/michi/sentences_' + str(sentence_length) + '_' + str(set_length) + '_' + str(i) + '.ann'
        with open(filename, 'w') as f2:
            filename = '/home/michi/sentences_' + str(sentence_length) + '_' + str(set_length) + '_' + str(i) + '.id'
            with open(filename, 'w') as f:
                for j in range(i * set_length, (i + 1) * set_length):
                    file.write(sentences[j] + '\n\n')
                    f.write(str(ids[j]) + '\n')


for i in range(100):
    if i in length:
        print(str(i) + '\t' + str(length[i]))

conn.close()