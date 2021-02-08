package ca.freda.relation_annotator;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.RelationDataCreatorFragment;
import ca.freda.relation_annotator.handler.ClientHandler;
import ca.freda.relation_annotator.handler.ServerHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    private JSONObject relation;
    private String uid;
    private ClientHandler clientHandler;
    private ServerHandler serverHandler;
    private HandlerThread clientThread;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private CustomViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_screen_slide);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (CustomViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.disableScroll(true);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (preferences.contains("uid")) {
            uid = preferences.getString("uid",null);
        } else {
            uid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uid", uid);
            editor.commit();
        }

        //uid = "<SET UID>";

        System.out.println("UID: " + uid);

        startClient();
    }

    private void startClient() {
        System.out.println("start client");
        clientThread = new HandlerThread("Socket Thread");
        clientThread.start();
        Looper mLooper = clientThread.getLooper();
        serverHandler = new ServerHandler(getMainLooper(),this);
        clientHandler = new ClientHandler(mLooper,this);
    }


    public void restartClient() {
        System.out.println("reset client");
        clientThread.interrupt();
        startClient();
        //switchButtonState(true);
    }

    public Message obtainMessage() {
        return clientHandler.obtainMessage();
    }

    public void sendMessage(Message message) {
        clientHandler.sendMessage(message);
    }

    public void receiveMessage(JSONObject message) throws JSONException {
        int mode = message.getInt("mode");

        switch (mode) {
            case 1: {
                if (message.getString("article") == null) {
                    showToast("No data available for this relation and annotator!");
                } else {
                    pagerAdapter.annotationFragment.createData(message);
                }
                break;
            }
            case 3: {
                // got database status
                String labeled = message.getString("labeled");
                break;
            }
            case 5: {
                pagerAdapter.relationDataCreatorFragment.showRelations(message.getJSONArray("relations"));
                break;
            }
            case -2: {
                if (message.has("message")){
                    showToast(message.getString("message"));
                } else {
                    showToast("Something went wrong, ask for data again.");
                    sendMessageWithMode(1);
                }

                break;
            }
            case -3: {
                if (message.has("message")) {
                    showToast(message.getString("message"));
                }
                break;
            }
        }
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public void passMessageToServerHandler(Message msg) {
        this.serverHandler.sendMessage(msg);
    }


    /*
    Rest of the methods.
     */
    public void sendMessageWithMode(int mode) {
        try {
            JSONObject object = new JSONObject();
            object.put("mode", mode);
            object.put("uid",uid);
            if (relation != null) {
                object.put("relation", relation.getString("name"));
            }
            clientHandler.sendMessage(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public JSONObject getRelation() {
        return relation;
    }

    public void setRelation(JSONObject relation) {
        this.relation = relation;
    }

    public String getUID() {
        return uid;
    }

    public void setPagerItem(int item) {
        pagerAdapter.annotationFragment.removeData();
        System.out.println("set pager item: " + item);
        mPager.setCurrentItem(item);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public RelationDataCreatorFragment relationDataCreatorFragment;
        public AnnotationFragment annotationFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("get item: " + position);
            switch (position) {
                case 0: {
                    relationDataCreatorFragment = new RelationDataCreatorFragment();
                    return relationDataCreatorFragment;
                }
                case 1: {
                    annotationFragment = new AnnotationFragment();
                    return annotationFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }


    }
}
