package com.example.smartmirror;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

public class SpotifyService extends Service {
    public static final String DATA = "data";
    public static final String REDIRECT_URI_EXTRA = "redirect";
    public static final String CLIENT_ID_EXTRA = "client_id";
    public static final String ACTION = "action";

    private static final int TIMEOUT = 2;

    private SpotifyAppRemote mSpotifyAppRemote;
    private SpotifyActions action;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("SpotifyService", "Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SpotifyService", "Service started");

        action = (SpotifyActions) intent.getSerializableExtra(ACTION);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(intent.getStringExtra(CLIENT_ID_EXTRA))
                        .setRedirectUri(intent.getStringExtra(REDIRECT_URI_EXTRA))
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("SpotifyService", "Connected! Yay!");

                        switch(action){
                            case PLAY:
                                Log.d("SpotifyService", "Playing");
                                mSpotifyAppRemote.getPlayerApi().play(intent.getStringExtra(DATA)).await(TIMEOUT,TimeUnit.SECONDS);
                                break;
                            case SKIP:
                                Log.d("SpotifyService", "Skipping");
                                mSpotifyAppRemote.getPlayerApi().skipNext().await(TIMEOUT,TimeUnit.SECONDS);
                                break;
                            case RESUME:
                                Log.d("SpotifyService", "Resuming");
                                mSpotifyAppRemote.getPlayerApi().resume().await(TIMEOUT,TimeUnit.SECONDS);
                                break;
                            case PAUSE:
                                Log.d("SpotifyService", "Pausing");
                                mSpotifyAppRemote.getPlayerApi().pause().await(TIMEOUT,TimeUnit.SECONDS);
                                break;
                        }

                        stopSelf();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("SpotifyService", throwable.getMessage(), throwable);
                        stopSelf();
                    }
                });

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("SpotifyService", "Destroyed");
        mSpotifyAppRemote.disconnect(mSpotifyAppRemote);
        super.onDestroy();
    }
}
