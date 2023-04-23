# IMPORTANT

This is still in progress and I will update this repository soon after the conference (ACM SAC 2023). See branch thesis for a working version. Unfortunately, the app is not yet available in the Play Store, will come sometime in April 2023 though.

# FREDA

Fast Relation Extraction Data Annotation

See our paper on Arxiv (accepted for the Knowledge and Natural Language Processing track at ACM SAC
2023): https://arxiv.org/abs/2204.07150

FREDA can be used to manually annotate sentences quickly and accurately. A simple procedure for sentence acquisition from a partially annotated Wikipedia-based corpus is provided to be able to create datasets for new relations.

In addition, FREDA can also be used to annotate datasets for the tasks Named Entity Recognition, Co-reference Resolution and Entity Linking.

Current database (`database/main.tar.xz`) contains at least 500 annotated sentences for 19 relations. In addition, four more relations are added without any annotations so far.

# Acknowledgements

We would like to thank all the data annotators for their hard work towards creating these datasets.

# Evaluation 

Please see Michael Strobl's PhD thesis (link will be added once it's published) for an evaluation against the open-source system BRAT (https://brat.nlplab.org/). Here are links to the videos the evaluation is based on:

BRAT RE: https://drive.google.com/file/d/1q5MKxxk5kSgVGn_VDt6Fif6HWHoMvkFL/view?usp=share_link

BRAT CR: https://drive.google.com/file/d/16Vi2m-Nhz-2MZXhZYFb9Xv2ppfb453Eu/view?usp=share_link

BRAT NER: https://drive.google.com/file/d/1h9Y2R2F05mF6ZitQRVHX3eDw3uiBZf4d/view?usp=share_link

FREDA RE: https://drive.google.com/file/d/1vs6VIssuYI98NeT3k25dgxvyGY50NtHS/view?usp=share_link

FREDA CR: https://drive.google.com/file/d/1vzVaXbluN_ixU5ELa7h_SDdqjsRmneJy/view?usp=share_link

FREDA NER: https://drive.google.com/file/d/1w0_wLld92Hw82tdF90VIUaq0xOuerKSq/view?usp=share_link

A stopwatch was added to each video as a sanity check. For the annotations conducted on the Android device, the clock can be seen on top.

# Get Started

(In offline mode, i.e. demo mode, the remaining steps can be skipped, apart from uploading the Android app to your device or an emulator. Just select the task to annotate for, examples are provided. However, this mode is only for demonstration purposes, annotations are not stored.)

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

# Start Annotations for Relation Extraction

Server needs to be running when app is used.

## Server

Start server: `python server_v1/main.py`

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