import json

class OverviewManager:
    def get_datasets(self, conn, uid, task):
        c = conn.cursor()
        if task == "RE":
            c.execute("SELECT name, language, dataset, info_short, data, status FROM dataset_RE;")
        elif task == "EL":
            c.execute("SELECT name, language, dataset, info_short, data, status FROM dataset_EL;")
        elif task == "CR":
            c.execute("SELECT name, language, dataset, info_short, data, status FROM dataset_CR;")
        elif task == "NER":
            c.execute("SELECT name, language, dataset, info_short, data, status FROM dataset_NER;")

        datasets = []
        for row in c:
            print(row)
            dataset_name = row[0]
            language = row[1]
            dataset_source = row[2]
            info_short = row[3]
            if row[4] is not None:
                dataset = json.loads(row[4])
            else:
                dataset = {}
            status = row[5]

            if status == 0:
                continue

            #dataset = {"name": dataset_name, "info_short": info_short, "dataset": dataset_source, ""}
            dataset['name'] = dataset_name
            dataset['info_short'] = info_short
            dataset['dataset'] = dataset_source
            dataset['language'] = language

            cur = conn.cursor()

            sql_annotator_1 = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_1 = ? AND response_1 > -1 AND (response_2 IS NULL OR response_2 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_2 = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_2 = ? AND response_2 > -1 AND (response_1 IS NULL OR response_1 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_3 = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_3 = ? AND response_3 > -1 AND (response_1 IS NULL OR response_1 > -1) AND (response_2 IS NULL OR response_2 > -1);"

            sql_annotator_once  = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_1 IS NOT NULL AND response_1 > -1;"

            sql_annotator_twice = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1;"

            sql_annotator_full  = "SELECT COUNT(*) FROM " + dataset_name + " WHERE annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1 AND (response_1 = response_2 OR (annotator_3 IS NOT NULL AND response_3 > -1));"

            cur.execute(sql_annotator_1, (uid,))
            dataset['annotations_1'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_2, (uid,))
            dataset['annotations_2'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_3, (uid,))
            dataset['annotations_3'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_once)
            dataset['once'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_twice)
            dataset['twice'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_full)
            dataset['full'] = cur.fetchall()[0][0]

            datasets.append(dataset)

            print("found info for dataset: " + dataset_name)
            print(dataset)

        message = {"mode": 5, "datasets": datasets, "task": task}

        print(message)

        message = json.dumps(message) + '\n'
        return message

