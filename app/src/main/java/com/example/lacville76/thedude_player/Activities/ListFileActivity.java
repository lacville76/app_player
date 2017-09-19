package com.example.lacville76.thedude_player.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.lacville76.thedude_player.R;

import java.util.ArrayList;
import java.util.List;

public class ListFileActivity extends Activity {
    private String filePath;
    private ListView lv;
    private Boolean isDirectory;
    private String name;
    private MediaPlayerService player;
    private boolean runningStatus;
    private long albumID;
    Intent playerIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);
        lv = (ListView) findViewById(R.id.list1);
        isDirectory = true;
        playerIntent= new Intent(this, MediaPlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        try
        {
            runningStatus = MediaPlayerService.getInstance().getRunningStatus();
        }
        catch (NullPointerException E)
        {
            runningStatus = false;
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.edit_list_layout,
                R.id.Itemname,
                getAlbumsList() );
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {


               if(isDirectory) {
                    name = (String) arg0.getItemAtPosition(arg2);
                   albumID = getAlbumID(arg2);
                   ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                           getApplicationContext(),
                           R.layout.edit_list_layout,
                           R.id.Itemname,
                           getAlbumSongs(getAlbumID(arg2)));
                   lv.setAdapter(arrayAdapter);
                   isDirectory = false;

               }
               else
               {
                 playAudio(getSong(albumID,arg2 ));
               }
            }

        });

    }

    public List<String> getAlbumsList()
    {
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM + " IS NOT NULL) GROUP BY (" + MediaStore.Audio.Media.ALBUM;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;
        List<String> audio_array_list = new ArrayList<String>();
        if(cur != null)
        {
            count = cur.getCount();
            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)) + " - " +
                            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    audio_array_list.add(data);
                }

            }
        }

        cur.close();
        return audio_array_list;
    }
    public List<String> getAlbumSongs(long albumID)
    {
        ContentResolver cr = this.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.ALBUM_ID + " == " + "'"+albumID+"'";
            String sortOrder = MediaStore.Audio.Media.TRACK + " ASC";
            Cursor cur = cr.query(uri, null, selection, null, sortOrder);
            int count = 0;
            List<String> audio_array_list = new ArrayList<String>();
            if(cur != null)
            {
                count = cur.getCount();
                player.setTrackCount(count);
                if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String data = Integer.toString(cur.getPosition()+1) + ". "
                            +cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    audio_array_list.add(data);
                }
            }
        }
        cur.close();
        return audio_array_list;
    }

    public String getSong(long albumID, int trackPosition)
    {
        String pos = new String();
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM_ID + " == " + "'"+albumID+"'";
        String sortOrder = MediaStore.Audio.Media.TRACK + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
                if( cur != null && cur.moveToPosition(trackPosition)){
                    Track track = new Track(cur);
                    pos = track.path;
                    player.setSongName(track.artist+'-'+track.album+'-'+track.title);
                    player.setActualTrackPosition(trackPosition);
                   // player.setAlbumName(albumName);
                    player.setAlbumID(albumID);
                    player.setTrack(track);
                    player.setIsPlayedfromAlbum(true);
                }
        cur.close();
        return pos;
    }

    public void goBack(View view) {
        if(isDirectory == true)
        {
            Intent openNewFileIntent = new Intent(this, MainActivity.class);
            startActivity(openNewFileIntent);
        }
        if(isDirectory == false)
        {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    R.layout.edit_list_layout,
                    R.id.Itemname,
                    getAlbumsList() );
            lv.setAdapter(arrayAdapter);
            isDirectory=true;
        }
    }


    public  void playAudio(String media) {
        if (isMyServiceRunning(MediaPlayerService.class)) {
            player.stopMedia();
        }
            playerIntent.putExtra("media", media);
            startService(playerIntent);

        runningStatus = true;

    }
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("MediaPlayer Error", service.service.getClassName() );
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public long getAlbumID(int position)
    {
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM + " IS NOT NULL) GROUP BY (" + MediaStore.Audio.Media.ALBUM;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        long ID = 0;
        List<String> audio_array_list = new ArrayList<String>();
        if( cur != null && cur.moveToPosition(position)){
        Track track = new Track(cur);
            ID = track.albumId;
    }

        cur.close();
        return ID;
    }

}
