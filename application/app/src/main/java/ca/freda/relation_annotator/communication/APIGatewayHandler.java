package ca.freda.relation_annotator.communication;

import android.util.Log;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import ca.freda.relation_annotator.FredaapiClient;
import ca.freda.relation_annotator.MainActivity;


public class APIGatewayHandler {

    private MainActivity mainActivity;

    public APIGatewayHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    //New overloaded function that gets Cognito User Pools tokens
    public void doInvokeAPI(HttpMethodName methodName, String path, Map<String, String> params, String body){
        HttpMethodName finalMethodName = methodName;
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch(cognitoAuthSession.getIdentityIdResult().getType()) {
                        case SUCCESS:
                            doInvokeAPI(cognitoAuthSession.getUserPoolTokensResult().getValue().getIdToken(), path, params, body, finalMethodName);
                            Log.i("Auth", "IdentityId: " + cognitoAuthSession.getIdentityIdResult().getValue());
                            break;
                        case FAILURE:
                            Log.i("Auth", "IdentityId not present because: " + cognitoAuthSession.getIdentityIdResult().getError().toString());
                    }
                },
                error -> Log.e("Auth", error.toString())
        );
    }

    //Updated function with arguments and code updates
    public void doInvokeAPI(String token, String path, Map<String, String> params, String body, HttpMethodName methodName) {
        ApiClientFactory factory = new ApiClientFactory();
        final FredaapiClient client = factory.build(FredaapiClient.class);

        System.out.println(token);

        ApiRequest localRequest = new ApiRequest(client.getClass().getSimpleName())
                .withPath(path)
                .withParameters(params)
                .withHttpMethod(methodName)
                .addHeader("Content-Type", "application/json")
                .addHeader("authorization", token);


        if (body != null) {
            try {
                byte[] content = body.getBytes("UTF-8");
                localRequest = localRequest.withBody(content).addHeader("Content-Length", content.length + "");
                System.out.println(body);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        final ApiRequest request = localRequest;
        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("t",
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = client.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        Log.d("TAG", "Response : " + responseData);
                        mainActivity.runOnUiThread(() -> {
                            try {
                                if (path.equals("dataset")) {
                                    mainActivity.getPagerAdapter().getOverviewFragment().showDatasets(new JSONObject(responseData));
                                } else if (path.equals("sentence")) {
                                    mainActivity.getPagerAdapter().getAnnotationFragment().showSentence(new JSONObject(responseData));
                                } else if (path.equals("response")) {
                                    mainActivity.getPagerAdapter().getAnnotationFragment().getSentence();
                                } else if (path.equals("back")) {
                                    mainActivity.getPagerAdapter().getAnnotationFragment().setBackParams(null);
                                    mainActivity.getPagerAdapter().getAnnotationFragment().getSentence();
                                } else if (path.equals("info")) {
                                    mainActivity.getPagerAdapter().getLoginFragment().showInfo(new JSONObject(responseData).getString("info"));
                                }
                            }catch (JSONException err){
                                Log.d("Error", err.toString());
                            }
                        });
                    }

                    Log.d("TAG", response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                    Log.e("TAG", exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();

    }

}
