package com.primemedia.pictureinpicapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    StyledPlayerView styled_PlayerView;
    ExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;
    Map<String, String> defaultRequestProperties = new HashMap<>();
    String userAgent = "";
    String SampleMp4 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4";
    Context context = this;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);

        styled_PlayerView = findViewById(R.id.styled_PlayerView);
        DataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setKeepPostFor302Redirects(true)
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
                .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
                .setDefaultRequestProperties(defaultRequestProperties);
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context, httpDataSourceFactory);
        MediaItem mediaItem = new MediaItem.Builder()
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .setUri(SampleMp4)
                .build();
        MediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem);

        initializePlayer(progressiveMediaSource);
    }

    private void initializePlayer(MediaSource progressiveMediaSource) {
        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(MainActivity.this, videoTrackSelectionFactory);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setTrackSelector(trackSelector)
                .setSeekForwardIncrementMs(10000)
                .setSeekBackIncrementMs(10000)
                .build();
        styled_PlayerView.setPlayer(exoPlayer);
        exoPlayer.setMediaSource(progressiveMediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                enterPictureInPictureMode();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (newConfig != null) {

        }
        if (isInPictureInPictureMode) {
            styled_PlayerView.hideController();
        } else {
            styled_PlayerView.showController();
        }

        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            finishPlayer();
            finishAndRemoveTask();
        } else if (getLifecycle().getCurrentState() == Lifecycle.State.STARTED) {
            startPlayer();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
    }

    private void startPlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }
    Boolean isBackPressed = false;

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        releasePlayer();
        finish();
    }
    private void finishPlayer() {

        isBackPressed = true;
        releasePlayer();
        finish();
    }

    private void releasePlayer()
    {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer.clearVideoSurface();
            exoPlayer = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            pausePlayer();
        }

    }

    private void pausePlayer()
    {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

}