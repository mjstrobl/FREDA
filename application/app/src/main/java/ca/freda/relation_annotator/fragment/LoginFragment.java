package ca.freda.relation_annotator.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.amazonaws.http.HttpMethodName;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.result.step.AuthSignInStep;
import com.amplifyframework.core.Amplify;

import java.util.HashMap;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.data.User;

public class LoginFragment extends Fragment implements View.OnClickListener {

    protected ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.login_slide_page, container, false);
        fillRootView();

        disableButtons();

        Amplify.Auth.getCurrentUser(authUser -> {
            Amplify.Auth.fetchAuthSession(
                    result -> {
                        AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                        switch(cognitoAuthSession.getIdentityIdResult().getType()) {
                            case SUCCESS:
                                String userId = authUser.getUserId();
                                String username = authUser.getUsername();
                                User user = new User(userId, username);
                                activity.setUser(user);
                                activity.runOnUiThread(() -> {
                                    setButtonStatus(true);
                                    activity.showToast("You are already signed in!");
                                });
                                Log.i("Auth", "IdentityId: " + cognitoAuthSession.getIdentityIdResult().getValue());
                                activity.getGatewayHandler().doInvokeAPI(HttpMethodName.GET, "info", new HashMap<>(), null);
                                break;
                            case FAILURE:
                                activity.runOnUiThread(() -> {
                                    setButtonStatus(false);
                                });
                                Log.i("Auth", "IdentityId not present because: " + cognitoAuthSession.getIdentityIdResult().getError().toString());
                        }
                    },
                    error -> {
                        activity.runOnUiThread(() -> {
                            setButtonStatus(false);
                        });
                        Log.e("Auth", error.toString());
                    }
            );

            Amplify.Auth.fetchUserAttributes(
                    attributes -> {
                        Log.i("Auth", "User attributes = " + attributes.toString());
                        for (int i =0; i < attributes.size(); i++) {
                            if (attributes.get(i).getKey().getKeyString().equals("email")) {
                                String email = attributes.get(i).getValue();
                                activity.runOnUiThread(() -> {
                                    TextView emailTextView = rootView.findViewById(R.id.email);
                                    emailTextView.setText(email);
                                });
                                break;
                            }
                        }
                    },
                    error -> Log.e("Auth", "Failed to fetch user attributes.", error)
            );
        }, exception -> {
            Log.e("Auth", "User not signed in.", exception);
            activity.runOnUiThread(() -> {
                setButtonStatus(false);
                showInfo(getResources().getString(R.string.pwd_info_textview));
            });
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
    }

    protected void fillRootView() {
        activity = (MainActivity) getActivity();
        rootView.findViewById(R.id.button_login).setOnClickListener(this);
        rootView.findViewById(R.id.button_reset_req).setOnClickListener(this);
        rootView.findViewById(R.id.button_update).setOnClickListener(this);
        rootView.findViewById(R.id.button_logout).setOnClickListener(this);
        rootView.findViewById(R.id.button_datasets).setOnClickListener(this);
        System.out.println("fill root view");
    }

    private void disableButtons() {
        rootView.findViewById(R.id.button_login).setEnabled(false);
        rootView.findViewById(R.id.button_reset_req).setEnabled(false);
        rootView.findViewById(R.id.button_update).setEnabled(false);
        rootView.findViewById(R.id.button_logout).setEnabled(false);
        rootView.findViewById(R.id.button_datasets).setEnabled(false);
        //rootView.findViewById(R.id.email).setEnabled(false);
        //rootView.findViewById(R.id.password).setEnabled(false);
    }

    protected void setButtonStatus(boolean signedIn) {
        if (signedIn) {
            rootView.findViewById(R.id.button_login).setEnabled(false);
            rootView.findViewById(R.id.button_reset_req).setEnabled(false);
            rootView.findViewById(R.id.button_update).setEnabled(true);
            rootView.findViewById(R.id.button_logout).setEnabled(true);
            rootView.findViewById(R.id.button_datasets).setEnabled(true);
            //rootView.findViewById(R.id.email).setEnabled(false);
            //rootView.findViewById(R.id.password).setEnabled(false);
        } else {
            rootView.findViewById(R.id.button_login).setEnabled(true);
            rootView.findViewById(R.id.button_reset_req).setEnabled(true);
            rootView.findViewById(R.id.button_update).setEnabled(false);
            rootView.findViewById(R.id.button_logout).setEnabled(false);
            rootView.findViewById(R.id.button_datasets).setEnabled(false);
            //rootView.findViewById(R.id.email).setEnabled(true);
            //rootView.findViewById(R.id.password).setEnabled(true);
        }
    }

    public void showInfo(String info) {
        TextView infoTextView = rootView.findViewById(R.id.textView_info);
        infoTextView.setText(info);
    }

    private void showPasswordMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reset Password");
        builder.setMessage("You need to reset your password since this is your first sign in. Minimum requirement is 8 characters.");

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = input.getText().toString();
                Amplify.Auth.confirmSignIn(newPassword,
                        result -> {
                            Log.i("AuthQuickstart", "Updated password successfully");
                            activity.runOnUiThread(() -> {
                                activity.showToast("Updated password successfully!");
                            });
                            if (result.isSignedIn()) {
                                signedIn();
                            } else {
                                activity.runOnUiThread(() -> {
                                    setButtonStatus(false);
                                });
                            }
                        },
                        error -> {
                            Log.e("AuthQuickstart", error.toString());
                            activity.runOnUiThread(() -> {
                                setButtonStatus(false);
                                activity.showToast("Update password failed! Please message admin or use a longer password!");
                            });
                        }
                );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                setButtonStatus(false);
            }
        });

        builder.show();
    }

    private void showPasswordUpdateMessage(String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Update Password");
        builder.setMessage("Please provide the new password. Minimum requirement is 8 characters.");

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = input.getText().toString();

                Amplify.Auth.updatePassword(
                        password,
                        newPassword,
                        () -> {
                            activity.runOnUiThread(() -> {
                                activity.showToast("Updated password successfully!");
                            });
                            Log.i("AuthQuickstart", "Updated password successfully");
                        },
                        error -> {
                            activity.runOnUiThread(() -> {
                                activity.showToast("Update password failed!");
                            });
                            Log.e("AuthQuickstart", error.toString());
                        }
                );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showPasswordResetMessage(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Update Password");
        builder.setMessage("Please provide the new password. Minimum requirement is 8 characters.");

        LinearLayout layout = new LinearLayout(getActivity().getApplicationContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText inputPassword = new EditText (getActivity().getApplicationContext());
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputPassword.setHint("new password");
        EditText inputActivationCode = new EditText (getActivity().getApplicationContext());
        inputActivationCode.setInputType(InputType.TYPE_CLASS_TEXT);
        inputActivationCode.setHint("activation code");
        layout.addView(inputPassword);
        layout.addView(inputActivationCode);

        builder.setView(layout);



        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = inputPassword.getText().toString();
                String activationCode = inputActivationCode.getText().toString();
                Amplify.Auth.confirmResetPassword(
                        email,
                        newPassword,
                        activationCode,
                        () -> {
                            Log.i("AuthQuickstart", "New password confirmed");
                            activity.runOnUiThread(() -> {
                                activity.showToast("Reset password successfully!");
                            });
                        },
                        error -> {
                            Log.e("AuthQuickstart", error.toString());
                            activity.runOnUiThread(() -> {
                                activity.showToast("Reset password failed!");
                            });
                        }
                );
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void signedIn() {
        Amplify.Auth.getCurrentUser(authUser -> {
            activity.setUser(new User(authUser.getUserId(), authUser.getUsername()));
            activity.getGatewayHandler().doInvokeAPI(HttpMethodName.GET, "info", new HashMap<>(), null);
            activity.runOnUiThread(() -> {
                setButtonStatus(true);
            });
        }, exception -> {
            Log.e("FREDA", "Error getting current user", exception);
            activity.runOnUiThread(() -> {
                setButtonStatus(false);
                activity.showToast("Something went wrong, sign in failed!");
            });
        });
    }

    private void logout() {
        Amplify.Auth.signOut( signOutResult -> {
            Log.i("Auth", "Signed out successfully");
            activity.runOnUiThread(() -> {
                setButtonStatus(false);
                activity.showToast("Signed out successfully!");
            });
        });
    }

    private void signIn(String email, String password) {
        System.out.println("sign in: " + email);
        disableButtons();
        if (email.length() == 0 || password.length() == 0) {
            activity.showToast("Email and password cannot be empty!");
            setButtonStatus(false);
            return;
        }

        Amplify.Auth.signIn(
                email,
                password,
                result -> {
                    if (result.isSignedIn()) {
                        signedIn();
                    } else if (result.getNextStep().getSignInStep() == AuthSignInStep.CONFIRM_SIGN_IN_WITH_NEW_PASSWORD) {
                        System.out.println("CONFIRM_SIGN_IN_WITH_NEW_PASSWORD");
                        activity.runOnUiThread(() -> {
                            showPasswordMessage();
                        });

                    } else {
                        System.out.println(result);
                        activity.runOnUiThread(() -> {
                            setButtonStatus(false);
                        });
                        Log.i("AuthQuickstart", result.isSignedIn() ? "Sign in succeeded" : "Sign in not complete");
                    }
                },
                error -> {
                    activity.runOnUiThread(() -> {
                        setButtonStatus(false);
                        activity.showToast("Authentication failed!");
                    });
                    Log.e("AuthQuickstart", error.toString());
                }
        );
    }

    private void requestPwdReset(String email) {
        if (email.length() == 0) {
            activity.showToast("Email cannot be empty!");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reset Password");
        builder.setMessage("Do you already have an activation code?");

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPasswordResetMessage(email);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Amplify.Auth.resetPassword(
                        email,
                        result -> {
                            activity.runOnUiThread(() -> {
                                activity.showToast("We emailed you an activation code!");
                            });
                            Log.i("AuthQuickstart", result.toString());
                        },
                        error -> {
                            activity.runOnUiThread(() -> {
                                activity.showToast("Password reset failed!");
                            });
                            Log.e("AuthQuickstart", error.toString());
                        }
                );
            }
        });

        builder.show();
    }

    private void updatePwd(String password) {
        if (password.length() == 0) {
            activity.showToast("Please provide old password in password text field!");
            return;
        }
        showPasswordUpdateMessage(password);
    }

    @Override
    public void onClick(View view) {
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email);
        String email = emailTextView.getText().toString();
        TextView passwordTextView = (TextView) rootView.findViewById(R.id.password);
        String password = passwordTextView.getText().toString();

        switch (view.getId()) {
            case R.id.button_login:
                signIn(email, password);
                break;
            case R.id.button_reset_req:
                requestPwdReset(email);
                break;
            case R.id.button_update:
                updatePwd(password);
                break;
            case R.id.button_logout:
                logout();
                break;
            case R.id.button_datasets:
                activity.setPagerItem(1);
                break;
            default:
                break;
        }
    }
}

