package ca.freda.relation_annotator.handler;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.amplifyframework.auth.AuthUser;
import org.json.JSONException;
import org.json.JSONObject;
import ca.freda.relation_annotator.MainActivity;

public class CommunicationHandler {

    private ClientHandler clientHandler;
    private ServerHandler serverHandler;
    private HandlerThread clientThread;

    private MainActivity mainActivity;
    private AuthUser user;
    private JSONObject dataset;

    private int currentRow = 0;

    public CommunicationHandler(MainActivity mainActivity, AuthUser user) {
        this.mainActivity = mainActivity;
        this.user = user;
    }

    public void startClient() {
        System.out.println("start client");
        clientThread = new HandlerThread("Socket Thread");
        clientThread.start();
        Looper mLooper = clientThread.getLooper();
        serverHandler = new ServerHandler(mainActivity.getMainLooper(), mainActivity);
        clientHandler = new ClientHandler(mLooper, mainActivity);

    }

    public void restartClient() {
        System.out.println("reset client");
        clientThread.interrupt();
        startClient();
    }

    public void passMessageToServerHandler(Message msg) {
        this.serverHandler.sendMessage(msg);
    }

    public void sendMessage(JSONObject message) {
        if (clientHandler == null) {
            startClient();
        }
        try {
            message.put("uid", user.getUserId());
            clientHandler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Message obtainMessage() {
        return clientHandler.obtainMessage();
    }

    public void sendMessage(Message message) {
        clientHandler.sendMessage(message);
    }

    public JSONObject getDataset() {
        return dataset;
    }

    public void setDataset(JSONObject dataset) {
        this.currentRow = 0;
        this.dataset = dataset;
    }
}
