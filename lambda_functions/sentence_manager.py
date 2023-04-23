import json
import boto3
from datetime import datetime
import pymysql

dynamodb = boto3.client('dynamodb')

TIME_LIMIT = 30

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
            seconds = datetime.today().timestamp() - TIME_LIMIT
            response_entities = None

            table_name = dataset + "_" + relation

            sql = "SELECT id, sentence, uid_1, uid_2, uid_3, response_1, response_2, data_1, data_2  FROM " + table_name + " WHERE (uid_1 != %s OR uid_1 IS NULL) AND (uid_2 != %s OR uid_2 IS NULL) AND (uid_3 != %s OR uid_3 IS NULL) " \
                                                                                                                           "AND (response_1 != -1 or response_1 is null) AND (response_2 != -1 OR response_2 IS NULL) AND (response_3 != -1 OR response_3 IS NULL) " \
                                                                                                                           "AND (response_2 IS NULL OR (response_1 != response_2 AND response_3 IS NULL)) AND (timestamp is null OR timestamp < %s OR uid = %s) LIMIT 1;"

            conn = mysqlconnect()
            cur = conn.cursor()
            params = (uid, uid, uid, seconds, uid)
            print(params)
            cur.execute(sql, params)
            output = cur.fetchall()

            print(output)

            for row in output:
                id = row[0]
                sentence = row[1]
                uid_1 = row[2]
                uid_2 = row[3]
                uid_3 = row[4]
                response_1 = row[5]
                response_2 = row[6]
                data_1 = row[7]
                data_2 = row[8]
                responses = []

                cur.execute("UPDATE " + table_name + " SET timestamp = %s, uid = %s WHERE id = %s;",
                            (datetime.today().timestamp(), uid, id))
                cur.execute("COMMIT;")

                data = dynamodb.get_item(
                    TableName=dataset,
                    Key={'sentence': {"S": sentence}}
                )

                print(data)
                entities = json.loads(data["Item"]['entities']['S'])
                text = data['Item']['text']['S']

                if uid_1 == None:
                    annotator = 1
                elif uid_2 == None:
                    annotator = 2
                    responses.append(response_1)
                    entities = json.loads(data_1)['entities']
                else:
                    annotator = 3
                    responses.append(response_1)
                    responses.append(response_2)
                    entities = json.loads(data_2)['entities']

                data = {"entities": entities, "text": text, "annotator": annotator, "id": id, "responses": responses}

                response = {
                    'statusCode': 200,
                    'body': json.dumps(data),
                    'headers': {
                        'Content-Type': 'application/json',
                        'Access-Control-Allow-Origin': '*'
                    },
                }

                conn.close()

                return response

            response = {
                'statusCode': 200,
                'body': json.dumps({}),
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