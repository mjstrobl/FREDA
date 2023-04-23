import json
import boto3

s3_client = boto3.client('s3')

def lambda_handler(event, context):
    csv_obj = s3_client.get_object(Bucket="freda-info", Key='info.txt')
    body = csv_obj['Body']
    info_text = body.read().decode('utf-8')

    response = {
        'statusCode': 200,
        'body': json.dumps({"info": info_text}),
        'headers': {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        },
    }

    return response