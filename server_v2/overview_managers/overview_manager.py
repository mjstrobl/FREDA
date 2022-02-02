import json

class OverviewManager:

    def get_datasets(self, conn, uid, task):
        c = conn.cursor()
        c.execute("SELECT * FROM dataset WHERE status = 1 AND task = ?;",(task,))

        datasets = []
        for row in c:
            name = row[0]
            info_short = row[4]
            datasets.append({"name": name, "info_short": info_short})

        print("found datasets")
        print(datasets)

        for dataset in datasets:
            dataset_name = dataset['name']
            cur = conn.cursor()

            sql_annotator_1 = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_1 = ? AND r.response_1 > -1 " \
                                                                                    "AND (response_2 IS NULL OR response_2 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_2 = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_2 = ? AND r.response_2 > -1 " \
                                                                                    "AND (response_1 IS NULL OR response_1 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_3 = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line AND r.annotator_3 = ? AND r.response_3 > -1 " \
                                                                                    "AND (response_1 IS NULL OR response_1 > -1) AND (response_2 IS NULL OR response_2 > -1);"

            sql_annotator_once = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                                                                                       "AND r.annotator_1 IS NOT NULL AND response_1 > -1;"

            sql_annotator_twice = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                                                                                        "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1;"

            sql_annotator_full = "SELECT COUNT(*) FROM sentence s, " + dataset_name + " r WHERE s.response > -2 AND s.article = r.article AND s.line = r.line " \
                                                                                       "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1 AND (response_1 = response_2 OR (annotator_3 IS NOT NULL AND response_3 > -1));"

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

            print("found info for dataset: " + dataset_name)
            print(dataset)

        message = {"mode": 5, "datasets": datasets}

        print(message)

        message = json.dumps(message) + '\n'
        return message

