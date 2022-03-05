package ca.freda.relation_annotator.handler;

import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ca.freda.relation_annotator.MainActivity;

public class CommunicationHandler {

    private ClientHandler clientHandler;
    private ServerHandler serverHandler;
    private HandlerThread clientThread;

    private MainActivity mainActivity;
    private String uid;
    private JSONObject reData;
    private JSONObject crData;
    private JSONObject nerData;
    private JSONObject elData;
    private JSONObject elAbstracts;
    private JSONObject elAliases;

    private JSONObject dataset;

    private int currentRow = 0;

    private boolean online = false;

    public CommunicationHandler(MainActivity mainActivity, String uid) {
        this.mainActivity = mainActivity;
        this.uid = uid;
        this.reData = readFile("re.json","assets");
        this.crData = readFile("coref.json","assets");
        this.nerData = readFile("ner.json","assets");
        this.elData = readFile("el.json","assets");


        String downloads_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File[] path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();

        for (File f : path) {
            System.out.println(f.getAbsolutePath());
        }

        this.elAbstracts = readFile("abstracts_100000.json","assets");
        this.elAliases = readFile("aliases_100000.json","assets");
    }

    private JSONObject readFile(String filename, String type) {
        JSONObject result = null;
        try {
            BufferedReader br;
            if (type.equals("assets")) {
                br = new BufferedReader(new InputStreamReader(mainActivity.getAssets().open(filename)));
            } else {
                br = new BufferedReader(new FileReader(filename));
            }

            String content = "";
            String line = br.readLine();
            while (line != null){
                content += line;
                line = br.readLine();
            }
            result = new JSONObject(content);

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public void startClient() {
        if (online) {
            System.out.println("start client");
            clientThread = new HandlerThread("Socket Thread");
            clientThread.start();
            Looper mLooper = clientThread.getLooper();
            serverHandler = new ServerHandler(mainActivity.getMainLooper(), mainActivity);
            clientHandler = new ClientHandler(mLooper, mainActivity);
        }
    }

    public void restartClient() {
        if (online) {
            System.out.println("reset client");
            clientThread.interrupt();
            startClient();
        }
    }

    public void passMessageToServerHandler(Message msg) {
        if (online) {
            this.serverHandler.sendMessage(msg);
        }
    }

    public void sendMessage(JSONObject message) {
        try {
            if (online) {
                if (clientHandler == null) {
                    startClient();
                }
                try {
                    message.put("uid", uid);
                    clientHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("send offline message");
                int mode = message.getInt("mode");
                String task = message.getString("task");
                switch (mode) {
                    case 0:
                        break;
                    case 1:
                        if (task.equals("RE")) {
                            // REAnnotationFragment asks for data.
                            message = this.reData.getJSONArray(dataset.getString("name")).getJSONObject(currentRow);
                        } else if (task.equals("CR")) {
                            // CRAnnotationFragment asks for data.
                            message = this.crData.getJSONArray(dataset.getString("name")).getJSONObject(currentRow);
                        } else if (task.equals("NER")) {
                            // NERAnnotationFragment asks for data.
                            message = this.nerData.getJSONArray(dataset.getString("name")).getJSONObject(currentRow);
                        } else if (task.equals("EL")) {
                            // ELAnnotationFragment asks for data.
                            message = this.nerData.getJSONArray(dataset.getString("name")).getJSONObject(currentRow);
                        }
                        message.put("task",task);
                        mainActivity.receiveMessage(message);
                        currentRow++;
                        if (currentRow >= 1000) {
                            currentRow = 0;
                        }
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        currentRow -= 2;
                        if (currentRow < 0) {
                            currentRow = 999;
                        }
                        break;
                    case 5:


                        if (task.equals("RE")) {
                            // get all relations
                            this.reData.put("mode", 5);
                            this.reData.put("task","RE");
                            mainActivity.receiveMessage(this.reData);
                        } else if (task.equals("CR")) {
                            // get coref datasets
                            this.crData.put("mode", 5);
                            this.crData.put("task","CR");
                            mainActivity.receiveMessage(this.crData);
                        } else if (task.equals("NER")) {
                            // get coref datasets
                            this.nerData.put("mode", 5);
                            this.nerData.put("task","NER");
                            mainActivity.receiveMessage(this.nerData);
                        } else if (task.equals("EL")) {
                            // get coref datasets
                            this.elData.put("mode", 5);
                            this.elData.put("task","EL");
                            mainActivity.receiveMessage(this.elData);
                        }

                        break;
                    case 6:
                        if (task.equals("EL")) {
                            // annotator asks for candidates
                            String mention = message.getString("mention");
                            JSONArray candidates = new JSONArray();

                            if (message.has("wikiName")) {
                                String[] tuple = {message.getString("wikiName"),"<no abstract available>"};
                                candidates.put(new JSONArray(tuple));
                            }
                            if (elAliases.has(mention)) {
                                JSONArray entities = elAliases.getJSONArray(mention);
                                for (int i = 0; i < entities.length(); i++) {
                                    String candidate = entities.getString(i);
                                    if (elAbstracts.has(candidate)) {
                                        String[] tuple = {candidate, elAbstracts.getString(candidate)};
                                        candidates.put(new JSONArray(tuple));
                                    }
                                }
                            }
                            message.put("candidates",candidates);
                            mainActivity.receiveMessage(message);
                        }

                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Message obtainMessage() {
        if (online) {
            return clientHandler.obtainMessage();
        } else {
            return null;
        }
    }

    public void sendMessage(Message message) {
        if (online) {
            clientHandler.sendMessage(message);
        }
    }

    public JSONObject getDataset() {
        return dataset;
    }

    public void setDataset(JSONObject dataset) {
        this.currentRow = 0;
        this.dataset = dataset;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public JSONObject getElAbstracts() {
        return elAbstracts;
    }

    public JSONObject getElAliases() {
        return elAliases;
    }
}
