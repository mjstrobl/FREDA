package ca.freda.relation_annotator.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import ca.freda.relation_annotator.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientHandler extends Handler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MainActivity mainActivity;

    private static final int SERVERPORT = 4444;
    //private static final String SERVERIP = "199.116.235.147";
    private static final String SERVERIP = "192.168.178.23";

    public ClientHandler(Looper myLooper, MainActivity mainActivity) {
        super(myLooper);
        this.mainActivity = mainActivity;
    }

    public void handleMessage(Message msg) {
        System.out.println("Client handler got a message.");
        establishConnection();
        if (socket == null || !socket.isConnected()) {
            mainActivity.showToast("Cannot connect to Server!");
        } else {
            out.println(msg.obj);
            out.flush();
            System.out.println("wrote message to socket: " + msg.toString());

            try {
                JSONObject jsonObject = new JSONObject((String)msg.obj);
                if ((Integer)jsonObject.get("mode") == 1 || (Integer)jsonObject.get("mode") == 3 || (Integer)jsonObject.get("mode") == 5 || (Integer)jsonObject.get("mode") == 6) {
                    receive();
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void establishConnection() {
        System.out.println("try to establish connection");
        if (socket == null || !socket.isConnected()) {
            boolean result = connect();
            if (result) {
                try {
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    in = new BufferedReader( new InputStreamReader(socket.getInputStream()));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("established new connection");
        } else {
            System.out.println("already connected");
        }
    }

    public void receive() {
        establishConnection();
        try {
            System.out.println("read new line.");
            String input = in.readLine();
            System.out.println("got new line: " + input);
            Message msg = obtainMessage();
            msg.obj = input;
            mainActivity.passMessageToServerHandler(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean connect() {
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            socket = new Socket(serverAddr, SERVERPORT);
            System.out.println(socket);

            return true;

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        return false;
    }

    public void sendMessage(JSONObject object) {
        System.out.println("send message" + object);
        Message msg = obtainMessage();
        msg.obj = object.toString();
        sendMessage(msg);
    }
}
