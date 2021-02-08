import json

class RelationManager:

    def get_relations(self, conn, uid):
        c = conn.cursor()
        c.execute("SELECT * FROM relation WHERE status = 1;")

        relations = []
        for row in c:
            name = row[0]
            info = row[2]
            relations.append({"name": name, "info": info})

        print("found relations")
        print(relations)

        for relation in relations:
            relation_name = relation['name']
            cur = conn.cursor()

            sql_annotator_1 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line AND r.annotator_1 = ? AND r.response_1 > -1 " \
                                "AND (response_2 IS NULL OR response_2 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_2 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line AND r.annotator_2 = ? AND r.response_2 > -1 " \
                                "AND (response_1 IS NULL OR response_1 > -1) AND (response_3 IS NULL OR response_3 > -1);"
            sql_annotator_3 = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line AND r.annotator_3 = ? AND r.response_3 > -1 " \
                                                                                    "AND (response_1 IS NULL OR response_1 > -1) AND (response_2 IS NULL OR response_2 > -1);"

            sql_annotator_once = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line " \
                                                                                        "AND r.annotator_1 IS NOT NULL AND response_1 > -1;"

            sql_annotator_twice = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line " \
                    "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1;"

            sql_annotator_full = "SELECT COUNT(*) FROM sentence s, " + relation_name + " r WHERE s.status = 1 AND s.article = r.article AND s.line = r.line " \
                                 "AND r.annotator_1 IS NOT NULL AND response_1 > -1 AND annotator_2 IS NOT NULL AND response_2 > -1 AND (response_1 = response_2 OR (annotator_3 IS NOT NULL AND response_3 > -1));"


            cur.execute(sql_annotator_1, (uid,))
            relation['annotations_1'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_2, (uid,))
            relation['annotations_2'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_3, (uid,))
            relation['annotations_3'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_once)
            relation['once'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_twice)
            relation['twice'] = cur.fetchall()[0][0]

            cur.execute(sql_annotator_full)
            relation['full'] = cur.fetchall()[0][0]

            print("found info for relation: " + relation_name)
            print(relation)

        message = {"mode":5,"relations":relations}

        print(message)

        message = json.dumps(message) + '\n'
        return message
