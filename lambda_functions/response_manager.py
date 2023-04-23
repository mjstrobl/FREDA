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
            response = int(params['response'])
            response_once = int(params['response_once'])
            response_twice = int(params['response_twice'])
            response_full = int(params['response_full'])
            body = event['body']

            table_name = dataset + "_" + relation

            sql = "SELECT uid from " + table_name + " WHERE id = %s;"

            conn = mysqlconnect()
            cur = conn.cursor()
            params = (id,)
            print(params)
            print(sql)
            cur.execute(sql, params)
            output = cur.fetchall()

            print(output)

            for row in output:
                if row[0] == uid:

                    data_field = "data_1"
                    response_field = "response_1"
                    uid_field = "uid_1"

                    if annotator == 2:
                        data_field = "data_2"
                        response_field = "response_2"
                        uid_field = "uid_2"
                    elif annotator == 3:
                        data_field = "data_3"
                        response_field = "response_3"
                        uid_field = "uid_3"

                    sql_1 = "UPDATE " + table_name + " SET  " + data_field + " = %s, " + response_field + " = %s, " + uid_field + " = %s WHERE id = %s;"
                    sql_2 = "UPDATE dataset SET annotations_1 = annotations_1 + %s, annotations_2 = annotations_2 + %s, annotations_full = annotations_full + %s WHERE name = %s AND relation = %s;"
                    cur.execute(sql_1, (body, response, uid, id))
                    cur.execute(sql_2, (response_once, response_twice, response_full, dataset, relation))
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

            conn.close()

    response = {
        'statusCode': 404
    }

    return response