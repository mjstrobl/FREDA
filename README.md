# FREDA

Fast Relation Extraction Data Annotation

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

# Server

Start server: `python server/main.py`

# Android Application

1. Download "Android Studio" from https://developer.android.com/studio.
2. Open project (`application/` subdirectory) in Android Studio.
3. Create Emulator in AVD Manager (e.g. Samsung A10).
4. Start app.





