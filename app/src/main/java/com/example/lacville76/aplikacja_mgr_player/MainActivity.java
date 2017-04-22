package com.example.lacville76.aplikacja_mgr_player;

import android.app.ListActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer = new MediaPlayer();
    String actualTrackFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try
        {
            actualTrackFilePath = getIntent().getStringExtra("TRACK_FILEPATH");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        prepareMediaPlayer(actualTrackFilePath);
    }

    public void playTrack(View view)
    {
        mediaPlayer.start();
    }

    public void getActualTrack()
    {

    }
    public void openTrack(View view)
    {
        Intent openNewFileIntent = new Intent(MainActivity.this, ListActivity.class);
        startActivity(openNewFileIntent);
    }

    public void prepareMediaPlayer(String filepath)
    {
        try
        {
            mediaPlayer.setDataSource(filepath);
            mediaPlayer.prepare();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
