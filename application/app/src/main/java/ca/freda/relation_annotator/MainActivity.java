package ca.freda.relation_annotator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ca.freda.relation_annotator.fragment.LoginFragment;
import ca.freda.relation_annotator.handler.CommunicationHandler;

import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.OverviewFragment;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;
    public CommunicationHandler comHandler;
    private FirebaseAuth mAuth;
    private CustomViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_screen_slide);

        mAuth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mPager = (CustomViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.disableScroll(true);
        mPager.setOffscreenPageLimit(NUM_PAGES);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            comHandler = new CommunicationHandler(this, currentUser);
            System.out.println("Username: " + currentUser.getDisplayName());
            setPagerItem(1);
        }
    }

    public void logout() {
        mAuth.signOut();
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
                pagerAdapter.overviewFragment.showDatasets(message.getJSONArray("datasets"));
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

    public void setPagerItem(int item) {
        System.out.println("set pager item: " + item);
        mPager.setCurrentItem(item);
    }

    public FirebaseUser getUser() {
        return this.user;
    }

    public void setUser(FirebaseUser user) {
        this.user = user;
    }

    public FirebaseAuth getmAuth() {
        return this.mAuth;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public LoginFragment loginFragment;
        public OverviewFragment overviewFragment;
        public AnnotationFragment annotationFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("get item: " + position);
            switch (position) {
                case 0: {
                    loginFragment = new LoginFragment();
                    return loginFragment;
                }
                case 1: {
                    overviewFragment = new OverviewFragment();
                    return overviewFragment;
                }
                case 2: {
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
