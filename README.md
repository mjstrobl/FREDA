# FREDA

Fast Relation Extraction Data Annotation

Current database (`database/main.tar.xz`) contains at least 500 annotated sentences for 19 relations. In addition, four more relations are added without any annotations so far.

# Get Started

Decompress database in `database/`:

`tar xf database/main.tar.xz`

Print relations and number of sentences with yes/no responses from current database:

`python server/database_statistics.py`

# Configuration

## Server config

In `config/config.json`:

1. If database path changed, replace `database` value accordingly.
2. Replace `port` and `ip` with your desired port and the ip address of your machine.

## Android application config

Replace `SERVERPORT` and `SERVERIP` in `application/app.src/main/java/ca/freda/relation_annotator/handler/ClientHandler.java` with your values.

# Start Annotations

Server needs to be running when app is used.

## Server

Start server: `python server/main.py`

## Android Application

1. Download "Android Studio" from https://developer.android.com/studio.
2. Open project (`application/` subdirectory) in Android Studio.
3. Create Emulator in AVD Manager (e.g. Samsung A10).
4. Start app.

# Create Data for New Relations

These modules are able to create new relations and extract sentences from WEXEA based on keywords and distant supervision using SPARQL queries and DBpedia. If relations do not exist in DBPedia, a keyword-based only approach can be used.

## CoreNLP Server

This is used to find dates in text.

Please download the CoreNLP tool from: https://stanfordnlp.github.io/CoreNLP/download.html

Follow the instructions to start the server: https://stanfordnlp.github.io/CoreNLP/corenlp-server.html

## Keywords

1. Modify `database/keywords.json` and add new tuple with relation name, info (presentend in the app to make sure subject/object are annotated accordingly), direction of relation and list of keywords.
2. De-compress `database/wexea.tar.xz` (contains 10,000 articles, should be replaced with original dataset for production system).
3. Run `python database/sentence_extractor_keywords.py` (will take around 12h or longer, depending on how many keywords are used)

## Distant Supervision

### Create DBpedia RDF store

1. Download and decompress DBpedia infobox properties: https://downloads.dbpedia.org/repo/dbpedia/generic/infobox-properties/2020.12.01/infobox-properties_lang=en.ttl.bz2
2. Download Apache Jena (we used the latest version 3.17.0): https://jena.apache.org/download/index.cgi
3. Set Jena home: `export JENA_HOME=<PATH TO apache-jena-3.17.0/>`
4. Create RDF store: `tdbloader2 --loc <LOCATION OF OUTPUT RDF STORE> infobox-properties_lang\=en.ttl`
5. Download Jena JDBC driver: https://search.maven.org/artifact/org.apache.jena/jena-jdbc-driver-bundle/3.17.0/jar
6. Add `jena-jdbc-driver-bundle-3.17.0.jar` to Java CLASSPATH.

### Extract sentences

1. Modify `database/queries.json` and add a list of SPARQL queries for each relation, where applicable. 
   Make sure that the relation added here already exists in the database (through running the aforementioned keyword extractor).
   Also all previous annotations on these relations are deleted, therefore it makes sense to create relations with keywords first and rightafter run this part.
2. Run `python database/sentence_extractor_distant_supervision.py`

It is possible that some SPARQL queries are either too generic or too specific. They can be tested beforehand, e.g. on https://yasgui.triply.cc/.
