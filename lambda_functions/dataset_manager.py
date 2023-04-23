import json
import pymysql

PASSWORD = "<freda db user password>"
USER = "<freda db user name>"
URL = "<freda db url>"
PORT = 3306
DB = "<freda db name>"


def mysqlconnect():
    # To connect MySQL database
    conn = pymysql.connect(
        host=URL,
        user=USER,
        password=PASSWORD,
        db=DB,
    )

    return conn


def lambda_handler(event, context):
    conn = mysqlconnect()
    cur = conn.cursor()
    cur.execute("select * from dataset order by (case when relation = 'Test' then 1 else 2 end), relation;")
    output = cur.fetchall()

    datasets = {}
    for row in output:
        dataset = row[0]
        relation = row[1]
        active = True if row[2] == 1 else False
        info = row[3]
        sentences = row[4]
        annotations_1 = row[5]
        annotations_2 = row[6]
        annotations_full = row[7]

        if dataset not in datasets:
            datasets[dataset] = {'relations': [], 'active': False, 'sentences': 0, 'annotations': 0}
        datasets[dataset]['relations'].append(
            (relation, active, info, sentences, annotations_1, annotations_2, annotations_full))
        datasets[dataset]['sentences'] += sentences
        datasets[dataset]['annotations'] += annotations_full