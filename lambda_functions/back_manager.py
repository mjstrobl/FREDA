import json
import pymysql

PASSWORD = "qr-eLg6sYw"
USER = "freda_db_user"
URL = "freda-db.cmx7wabt5qyo.eu-central-1.rds.amazonaws.com"
PORT = 3306
DB = "freda"


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
    print(event)
    if 'queryStringParameters' in event:
        params = event['queryStringParameters']
        print(params)
        if 'dataset' in params and 'relation' in params and 'uid' in params:

            relation = params['relation']
            dataset = params['dataset']
            uid = params['uid']
            id = int(params['id'])
            annotator = int(params['annotator'])
            previousFull = int(params['response_full'])

            if annotator == 1:
                sql_1 = "UPDATE " + dataset + "_" + relation + " SET response_1 = NULL, response_2 = NULL , response_3 = NULL, data_1 = NULL, data_2 = NULL, data_3 = NULL, uid_1 = NULL, uid_2 = NULL, uid_3 = NULL WHERE id = %s"
                sql_2 = "UPDATE dataset SET annotations_1 = annotations_1 - 1 WHERE name = %s AND relation = %s;"
            elif annotator == 2:
                sql_1 = "UPDATE " + dataset + "_" + relation + " SET response_2 = NULL, response_3 = NULL, data_2 = NULL, data_3 = NULL, uid_2 = NULL, uid_3 = NULL WHERE id = %s"
                if previousFull == 0:
                    sql_2 = "UPDATE dataset SET annotations_2 = annotations_2 - 1 WHERE name = %s AND relation = %s;"
                else:
                    sql_2 = "UPDATE dataset SET annotations_2 = annotations_2 - 1, annotations_full = annotations_full - 1 WHERE name = %s AND relation = %s;"
            else:
                sql_1 = "UPDATE " + dataset + "_" + relation + " SET response_3 = NULL, data_3 = NULL, uid_3 = NULL WHERE id = %s"
                sql_2 = "UPDATE dataset SET annotations_full = annotations_full - 1 WHERE name = %s AND relation = %s;"

            conn = mysqlconnect()
            cur = conn.cursor()

            print(sql_1)
            print(sql_2)

            cur.execute(sql_1, (id,))
            cur.execute(sql_2, (dataset, relation))
            cur.execute("COMMIT;")

            response = {
                'statusCode': 200,
                'headers': {
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                },
            }

            conn.close()

            return response

    response = {
        'statusCode': 404
    }

    return response