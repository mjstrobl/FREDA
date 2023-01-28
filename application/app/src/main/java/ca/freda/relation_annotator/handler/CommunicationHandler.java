package ca.freda.relation_annotator.handler;

import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.google.firebase.auth.FirebaseUser;

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
    private FirebaseUser user;
    private JSONObject data;
    private JSONObject dataset;

    private int currentRow = 0;

    private boolean online = false;

    public CommunicationHandler(MainActivity mainActivity, FirebaseUser user) {
        this.mainActivity = mainActivity;
        this.user = user;
        this.data = readFile("re.json","assets");
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
                    message.put("uid", user.getUid());
                    clientHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("send offline message");
                int mode = message.getInt("mode");
                switch (mode) {
                    case 0:
                        break;
                    case 1:
                        message = this.data.getJSONArray(dataset.getString("name")).getJSONObject(currentRow);
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
                        this.data.put("mode", 5);
                        mainActivity.receiveMessage(this.data);
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
}
