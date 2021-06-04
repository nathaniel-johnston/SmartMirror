package com.example.smartmirror;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class OAuth {
    private static OAuth instance = null;

    public RequestQueue queue;

    public OAuth(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public static synchronized OAuth getInstance(Context context)
    {
        if (null == instance)
            instance = new OAuth(context);
        return instance;
    }

    //this is so you don't need to pass context each time
    public static synchronized OAuth getInstance()
    {
        if (null == instance)
        {
            throw new IllegalStateException(OAuth.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    public void getToken(String clientId, String clientSecret, String redirectUri, String grantType,
                         String codeOrToken, final NetworkCallback<String> callback) {
        StringRequest request = new StringRequest(Request.Method.POST,
                    "https://accounts.spotify.com/api/token",
                    new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("OAuth", error.toString());
                        callback.onFailure(error.toString());
                    }
                })
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", grantType);

                if(grantType.equals("authorization_code")){
                    params.put("code", codeOrToken);
                    params.put("redirect_uri", redirectUri);
                }
                else {
                    params.put("refresh_token", codeOrToken);
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();

                String auth = "";

                try {
                    byte[] bytes = (clientId + ":" + clientSecret).getBytes("UTF-8");
                    auth = "Basic " + Base64.encodeToString(bytes, Base64.NO_WRAP);
                }
                catch (Exception e) {
                    Log.e("b64", "something went wrong");
                }

                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(request);
    }
}
