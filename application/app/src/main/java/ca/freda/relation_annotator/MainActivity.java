package ca.freda.relation_annotator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import ca.freda.relation_annotator.fragment.LoginFragment;
import ca.freda.relation_annotator.handler.CommunicationHandler;

import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.OverviewFragment;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;
    public CommunicationHandler comHandler;
    private CustomViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("FREDA", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("FREDA", "Could not initialize Amplify", error);
        }

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



        Amplify.Auth.getCurrentUser(authUser -> {
            setUserId(authUser.getUserId());
            setPagerItem(1);

            Amplify.Auth.fetchUserAttributes(
                    attributes -> {
                        Log.i("AuthDemo", "User attributes = " + attributes.toString());
                        for (int i =0; i < attributes.size(); i++) {
                            if (attributes.get(i).getKey().getKeyString().equals("email")) {
                                String email = attributes.get(i).getValue();
                                pagerAdapter.overviewFragment.setEmail(email);
                                break;
                            }
                        }
                    },
                    error -> Log.e("AuthDemo", "Failed to fetch user attributes.", error)
            );
        }, exception -> {
            Log.e("FREDA", "User not signed in.", exception);
        });
    }

    public void logout() {
        Amplify.Auth.signOut( signOutResult -> {
            Log.i("AuthQuickStart", "Signed out successfully");
            runOnUiThread(() -> {
                showToast("Signed out successfully!");
            });
        });
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
        int duration = Toast.LENGTH_LONG;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
