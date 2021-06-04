package com.example.smartmirror;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "b26c640a3fa24b71b612651ab61890ae";
    private static final String REDIRECT_URI = "smartmirror://callback";
    private static final String CLIENT_SECRET = "d6616baee44b4a6db2dabd9b13881d11";
    private static final String OAUTH_SCOPE = "user-read-private%20user-read-email%20user-read-playback-state"+
            "%20streaming%20user-read-currently-playing%20playlist-read-private%20user-library-read";

    private static String state = "";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        OAuth.getInstance(this);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        // login in you haven't already done so
        if(FileIO.readFile("tokens.json").equals("")) {
            Dialog auth_dialog;

            state = generateState(10);

            auth_dialog = new Dialog(MainActivity.this);
            auth_dialog.setContentView(R.layout.auth_dialog);
            WebView web = (WebView) auth_dialog.findViewById(R.id.webv);
            web.getSettings().setJavaScriptEnabled(true);
            web.loadUrl("https://accounts.spotify.com/authorize?client_id=" + CLIENT_ID
                    + "&response_type=code&redirect_uri=" + REDIRECT_URI + "&scope=" + OAUTH_SCOPE + "&state=" + state);

            web.setWebViewClient(new WebViewClient() {

                boolean authComplete = false;
                Intent resultIntent = new Intent();

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                }

                String authCode;

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    Uri uri = Uri.parse(url);

                    if (uri.toString().startsWith(REDIRECT_URI) &&
                            uri.getQueryParameter("state").equals(state) &&
                            authComplete != true) {

                        authCode = uri.getQueryParameter("code");
                        authComplete = true;
                        resultIntent.putExtra("code", authCode);
                        MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                        setResult(Activity.RESULT_CANCELED, resultIntent);

                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("Code", authCode);
                        edit.commit();
                        auth_dialog.dismiss();

                        OAuth jParser = new OAuth(SmartMirror.getAppContext());
                        jParser.getInstance().getToken(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI,
                                "authorization_code", authCode, new NetworkCallback<String>() {
                                    @Override
                                    public void onSuccess(String response) {
                                        FileIO.writeToFile(response, "tokens.json");
                                    }

                                    @Override
                                    public void onFailure(String error) {

                                    }
                                });
                    }
                }
            });
            auth_dialog.show();
            auth_dialog.setCancelable(true);
        }

        Intent mqttIntent = new Intent(this, MQTTService.class);
        mqttIntent.putExtra(MQTTService.CLIENT_ID_EXTRA, CLIENT_ID);
        mqttIntent.putExtra(MQTTService.REDIRECT_URI_EXTRA, REDIRECT_URI);
        mqttIntent.putExtra(MQTTService.CLIENT_SECRET_EXTRA, CLIENT_SECRET);

        ContextCompat.startForegroundService(this, mqttIntent);
    }

    private String generateState(int len) {
        String text = "";
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < len; i++) {
            text += possible.charAt((int)Math.floor(Math.random() * 62));
        }
        return text;
    }
}
