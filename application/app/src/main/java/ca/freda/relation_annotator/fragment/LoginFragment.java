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
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.amplifyframework.auth.result.step.AuthSignInStep;
import com.amplifyframework.core.Amplify;

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
        rootView.findViewById(R.id.button_reset_req).setOnClickListener(this);
        rootView.findViewById(R.id.button_reset).setOnClickListener(this);
        System.out.println("fill root view");
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
                                singedIn();
                            }
                        },
                        error -> {
                            Log.e("AuthQuickstart", error.toString());
                            activity.runOnUiThread(() -> {
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
            }
        });

        builder.show();
    }

    private void showPasswordResetMessage(String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reset Password");
        builder.setMessage("Please provide the activation code to reset the password. Minimum requirement for new password is 8 characters.");

        // Set up the input
        final EditText input = new EditText(activity);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String activationCode = input.getText().toString();

                Amplify.Auth.confirmResetPassword(
                        email,
                        password,
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

    private void singedIn() {
        Amplify.Auth.getCurrentUser(authUser -> {
            activity.setUserId(authUser.getUserId());
            activity.setPagerItem(1);
        }, exception -> {
            Log.e("FREDA", "Error getting current user", exception);
            activity.runOnUiThread(() -> {
                activity.showToast("Something went wrong, sign in failed!");
            });
        });
    }

    private void signIn(String email, String password) {
        System.out.println("sign in: " + email);

        if (email.length() == 0 || password.length() == 0) {
            activity.showToast("Email and password cannot be empty!");
            return;
        }

        Amplify.Auth.signIn(
                email,
                password,
                result -> {
                    if (result.isSignedIn()) {
                        singedIn();
                    } else if (result.getNextStep().getSignInStep() == AuthSignInStep.CONFIRM_SIGN_IN_WITH_NEW_PASSWORD) {
                        System.out.println("CONFIRM_SIGN_IN_WITH_NEW_PASSWORD");
                        activity.runOnUiThread(() -> {
                            showPasswordMessage();
                        });

                    } else {
                        System.out.println(result);
                        Log.i("AuthQuickstart", result.isSignedIn() ? "Sign in succeeded" : "Sign in not complete");
                    }
                },
                error -> {
                    activity.runOnUiThread(() -> {
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

        Amplify.Auth.resetPassword(
                email,
                result -> {
                    Log.i("AuthQuickstart", result.toString());
                    activity.runOnUiThread(() -> {
                        activity.showToast("We emailed you a verification code. You can use it to reset your password.");
                    });
                },
                error -> {
                    Log.e("AuthQuickstart", error.toString());
                    activity.runOnUiThread(() -> {
                        activity.showToast("Password reset failed!");
                    });
                }
        );
    }

    private void resetPwd(String email, String password) {
        if (email.length() == 0 || password.length() == 0) {
            activity.showToast("Email and password cannot be empty!");
            return;
        }

        showPasswordResetMessage(email, password);
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
            case R.id.button_reset:
                resetPwd(email, password);
                break;
            default:
                break;
        }
    }
}

