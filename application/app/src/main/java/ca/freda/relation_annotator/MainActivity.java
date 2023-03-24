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
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;

import ca.freda.relation_annotator.communication.APIGatewayHandler;
import ca.freda.relation_annotator.data.User;
import ca.freda.relation_annotator.fragment.DatasetFragment;
import ca.freda.relation_annotator.fragment.LoginFragment;

import ca.freda.relation_annotator.fragment.AnnotationFragment;
import ca.freda.relation_annotator.fragment.OverviewFragment;

public class MainActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 4;
    private CustomViewPager mPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private User user;
    private APIGatewayHandler gatewayHandler;

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

        gatewayHandler = new APIGatewayHandler(this);
    }

    @Override
    public void onStart() {
        super.onStart();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ScreenSlidePagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public APIGatewayHandler getGatewayHandler() {
        return gatewayHandler;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private LoginFragment loginFragment;
        private OverviewFragment overviewFragment;
        private DatasetFragment datasetFragment;
        private AnnotationFragment annotationFragment;

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
                    datasetFragment = new DatasetFragment();
                    return datasetFragment;
                }
                case 3: {
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

        public LoginFragment getLoginFragment() {
            return loginFragment;
        }

        public DatasetFragment getDatasetFragment() {
            return datasetFragment;
        }

        public OverviewFragment getOverviewFragment() {
            return overviewFragment;
        }

        public AnnotationFragment getAnnotationFragment() {
            return annotationFragment;
        }
    }
}
