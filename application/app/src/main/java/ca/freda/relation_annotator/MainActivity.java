package ca.freda.relation_annotator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ca.freda.relation_annotator.fragment.StartFragment;
import ca.freda.relation_annotator.handler.ClientHandler;
import ca.freda.relation_annotator.handler.CommunicationHandler;
import ca.freda.relation_annotator.handler.ServerHandler;

import ca.freda.relation_annotator.fragment.RE.REAnnotationFragment;
import ca.freda.relation_annotator.fragment.RE.REOverviewFragment;
import ca.freda.relation_annotator.fragment.EL.ELAnnotationFragment;
import ca.freda.relation_annotator.fragment.EL.ELOverviewFragment;
import ca.freda.relation_annotator.fragment.NER.NERAnnotationFragment;
import ca.freda.relation_annotator.fragment.NER.NEROverviewFragment;
import ca.freda.relation_annotator.fragment.CR.CRAnnotationFragment;
import ca.freda.relation_annotator.fragment.CR.CROverviewFragment;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 9;


    private String uid;
    public CommunicationHandler comHandler;

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
        mPager.setOffscreenPageLimit(NUM_PAGES);


        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (preferences.contains("uid")) {
            uid = preferences.getString("uid",null);
        } else {
            uid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uid", uid);
            editor.commit();
        }

        uid = "0520f3ec-5831-4be4-9416-51b39810512d";

        comHandler = new CommunicationHandler(this,uid);



        System.out.println("UID: " + uid);


    }





    public void receiveMessage(JSONObject message) throws JSONException {
        int mode = message.getInt("mode");
        String task = message.getString("task");

        switch (mode) {
            case 1: {
                if (message.getString("article") == null) {
                    showToast("No data available for this relation and annotator!");
                } else {
                    if (task.equals("RE")) {
                        pagerAdapter.reAnnotationFragment.createData(message);
                    } else if (task.equals("CR")) {
                        pagerAdapter.crAnnotationFragment.createData(message);
                    } else if (task.equals("NER")) {
                        pagerAdapter.nerAnnotationFragment.createData(message);
                    } else if (task.equals("EL")) {
                        pagerAdapter.elAnnotationFragment.createData(message);
                    }
                }
                break;
            }
            case 3: {
                // got database status
                String labeled = message.getString("labeled");
                break;
            }
            case 5: {
                if (task.equals("RE")) {
                    pagerAdapter.reOverviewFragment.showRelations(message.getJSONArray("relations"));
                } else if (task.equals("CR")) {
                    pagerAdapter.crOverviewFragment.showTypes(message.getJSONArray("datasets"));
                } else if (task.equals("NER")) {
                    pagerAdapter.nerOverviewFragment.showTypes(message.getJSONArray("datasets"));
                } else if (task.equals("EL")) {
                    pagerAdapter.elOverviewFragment.showTypes(message.getJSONArray("datasets"));
                }
                break;
            }
            case 6: {
                if (task.equals("EL")) {
                    pagerAdapter.elAnnotationFragment.showCandidates(message.getJSONObject("candidates"), message.getInt("index"));
                }
                break;
            }
            case -2: {
                if (message.has("message")){
                    showToast(message.getString("message"));
                } else {
                    showToast("Something went wrong, ask for data again.");
                    message.put("mode",1);
                    comHandler.sendMessage(message);
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



    public String getUID() {
        return uid;
    }

    public void setPagerItem(int item) {
        System.out.println("set pager item: " + item);
        mPager.setCurrentItem(item);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public StartFragment startFragment;

        public REOverviewFragment reOverviewFragment;
        public REAnnotationFragment reAnnotationFragment;

        public ELOverviewFragment elOverviewFragment;
        public ELAnnotationFragment elAnnotationFragment;

        public CROverviewFragment crOverviewFragment;
        public CRAnnotationFragment crAnnotationFragment;

        public NEROverviewFragment nerOverviewFragment;
        public NERAnnotationFragment nerAnnotationFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("get item: " + position);
            switch (position) {
                case 0: {
                    startFragment = new StartFragment();
                    return startFragment;
                }
                case 1: {
                    nerOverviewFragment = new NEROverviewFragment();
                    return nerOverviewFragment;
                }
                case 2: {
                    crOverviewFragment = new CROverviewFragment();
                    return crOverviewFragment;
                }
                case 3: {
                    elOverviewFragment = new ELOverviewFragment();
                    return elOverviewFragment;
                }
                case 4: {
                    reOverviewFragment = new REOverviewFragment();
                    return reOverviewFragment;
                }
                case 5: {
                    nerAnnotationFragment = new NERAnnotationFragment();
                    return nerAnnotationFragment;
                }
                case 6: {
                    crAnnotationFragment = new CRAnnotationFragment();
                    return crAnnotationFragment;
                }
                case 7: {
                    elAnnotationFragment = new ELAnnotationFragment();
                    return elAnnotationFragment;
                }
                case 8: {
                    reAnnotationFragment = new REAnnotationFragment();
                    return reAnnotationFragment;
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
