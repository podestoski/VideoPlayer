package com.example.videoplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    public final String[] EXTERNAL_PERMS = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public final int EXTERNAL_REQUEST = 138;

    private final String DRAGON_BALL_FOLDER = "/storage/BA69-1AE1/Movies/Dragon Ball/";

    //Shared Preferences
    SharedPreferences sharedpreferences;
    private final String SHARED_PREFERENCES_NAME = "DRAGON_BALL_PLAYER";
    private final String VIDEO_NAME_KEY = "CURRENT_VIDEO";
    private final String POSITION_NAME_KEY = "POSITION_VIDEO";
    private SharedPreferences.Editor editor;


    //Video
    private VideoView videoView;
    private File[] playList;
    private final int INITIAL_MS = 123000;

    private TextView txtTitle;
    private TextView txtDuration;
    private Button btnPlayPause;
    private MediaController mediaController;






    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestForPermission();


        txtTitle = findViewById(R.id.txtTitle);
        txtDuration = findViewById(R.id.txtDuration);
        sharedpreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        playList = new File(DRAGON_BALL_FOLDER).listFiles();
        videoView = (VideoView) findViewById(R.id.videoView);
        btnPlayPause = findViewById(R.id.btnPlayPause);


        if(getVideoKey() == null || getVideoKey().toString() == "")
        {
            setVideoKey("Dragon Ball 001 - El secreto de la esfera del dragon.mp4");
            setPositionKey(INITIAL_MS);
        }

        try
        {
            // ID of video file.
            Uri play = Uri.fromFile(new File(DRAGON_BALL_FOLDER + getVideoKey()));
            videoView.setVideoURI(play);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        videoView.requestFocus();
        // When the video file ready for playback.

        if (mediaController == null) {
            mediaController = new MediaController(MainActivity.this);

            // Set the videoView that acts as the anchor for the MediaController.
            mediaController.setAnchorView(videoView);


            // Set MediaController for VideoView
            videoView.setMediaController(mediaController);
        }


    }

    public boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    private void setPositionKey(int position)
    {
        editor.putInt(POSITION_NAME_KEY, position);
        editor.commit();
    }

    private void setVideoKey(String videoKey)
    {
        editor.putString(VIDEO_NAME_KEY, videoKey);
        editor.commit();
    }


    private int getPositionKey()
    {
        int ms = sharedpreferences.getInt(POSITION_NAME_KEY,0);
        txtDuration.setText(getVideoTime(ms));
        return ms;
    }

    private String getVideoKey()
    {
        String videoKey = sharedpreferences.getString(VIDEO_NAME_KEY, "");
        txtTitle.setText(videoKey);
        return videoKey;
    }


    @Override
    public void onBackPressed() {
        setPositionKey(videoView.getCurrentPosition());
        super.onBackPressed();
    }

    public void btnPlayPauseOnClick(View v)
    {
            if(videoView.isPlaying())
        {
            videoView.pause();
            setPositionKey(videoView.getCurrentPosition());
        }
        else
        {
            videoView.seekTo(getPositionKey());
            mediaController.setAnchorView(videoView);
            videoView.start();

        }
    }

    public void btnNextOnClick(View v)
    {
        if(videoView.isPlaying())
        {
            videoView.stopPlayback();
        }
        int index = getNextId(getVideoKey());
        File videoToPlay = playList[index];
        setVideoKey(videoToPlay.getName());
        videoView.setVideoURI(Uri.fromFile(new File(DRAGON_BALL_FOLDER + videoToPlay.getName())));
        setPositionKey(INITIAL_MS);
        videoView.seekTo(getPositionKey());
        videoView.start();

    }

    public void btnPreviousOnClick(View v)
    {
        if(videoView.isPlaying())
        {
            videoView.stopPlayback();
        }
        int index = getPrev(getVideoKey());
        File videoToPlay = playList[index];
        setVideoKey(videoToPlay.getName());
        videoView.setVideoURI(Uri.fromFile(new File(DRAGON_BALL_FOLDER + videoToPlay.getName())));
        setPositionKey(INITIAL_MS);
        videoView.seekTo(getPositionKey());
        videoView.start();

    }

    int getNextId(String title)
    {
        int i = 0;
        for(File video: playList)
        {
            if(video.getName().equalsIgnoreCase(title))
            {
                if(i == playList.length)
                    return playList.length;
                else
                    return i + 1;
            }
            i++;
        }
        return 0;
    }

    int getPrev(String title)
    {
        int i = 0;
        for(File video: playList)
        {
            if(video.getName().equalsIgnoreCase(title))
            {
                if(i == 0)
                    return 0;
                else
                    return i - 1;
            }
            i++;
        }
        return 0;
    }

    private String getVideoTime(long ms)
    {
        long milliseconds = ms;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if(minutes < 10)
        {
            if (seconds < 10)
                return "0" + minutes + ":0" + seconds;
            else
                return "0" + minutes + ":" + seconds;
        }
        else if(seconds < 10)
            return minutes + ":0" + seconds;
        else
            return minutes + ":" + seconds;


    }

    @Override
    protected void onPause() {
        btnPlayPauseOnClick(btnPlayPause);
        super.onPause();
    }
}