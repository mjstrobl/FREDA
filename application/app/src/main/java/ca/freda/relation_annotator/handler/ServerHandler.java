package ca.freda.relation_annotator.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import ca.freda.relation_annotator.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerHandler extends Handler {

    private MainActivity mainActivity;

    public ServerHandler(Looper myLooper, MainActivity mainActivity) {
        super(myLooper);
        this.mainActivity = mainActivity;
    }

    public void handleMessage(Message msg) {
        System.out.println("server handler received message: " + msg.obj);
        try {
            JSONObject jsonObject = new JSONObject((String)msg.obj);
            mainActivity.receiveMessage(jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
            mainActivity.showToast("JSON exception, cannot read data from server.");
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            mainActivity.showToast("Null pointer exception, please reload sentence or restart the app!");
            mainActivity.restartClient();

        }
    }
}
