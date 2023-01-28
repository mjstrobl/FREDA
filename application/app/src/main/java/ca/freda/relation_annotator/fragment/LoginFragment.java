package ca.freda.relation_annotator.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    protected ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.login_slide_page, container, false);
        fillRootView();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
    }

    protected void fillRootView() {
        activity = (MainActivity) getActivity();
        rootView.findViewById(R.id.button_login).setOnClickListener(this);
        //rootView.findViewById(R.id.button_create_account).setOnClickListener(this);
        System.out.println("fill root view");
    }


    /*private void createAccount(String email, String password) {
        System.out.println("create account");
        FirebaseAuth mAuth = activity.getmAuth();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            activity.setUser(user);
                            activity.setPagerItem(1);
                        } else {
                            activity.showToast("Authentication failed!");
                        }
                    }
                });
    }*/

    private void signIn(String email, String password) {
        System.out.println("sign in");
        FirebaseAuth mAuth = activity.getmAuth();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            activity.setUser(user);
                            activity.setPagerItem(1);
                        } else {
                            // If sign in fails, display a message to the user.
                            activity.showToast("Authentication failed!");
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email);
        String email = emailTextView.getText().toString();
        TextView passwordTextView = (TextView) rootView.findViewById(R.id.password);
        String password = passwordTextView.getText().toString();

        if (email.length() == 0 || password.length() == 0) {
            activity.showToast("Email and password cannot be empty!");
            return;
        }

        switch (view.getId()) {
            /*case R.id.button_create_account:
                createAccount(email, password);
                break;*/
            case R.id.button_login:
                signIn(email, password);
                break;
            default:
                break;
        }
    }
}

