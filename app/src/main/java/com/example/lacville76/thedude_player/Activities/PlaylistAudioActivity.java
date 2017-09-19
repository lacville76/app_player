package com.example.lacville76.thedude_player.Activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.lacville76.thedude_player.R;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAudioActivity extends Activity {
    Intent playerIntent;

    private ListView playlistListView;
    private MediaPlayerService playerService;
    int ID;
    Boolean isPlaylist;
    private boolean runningStatus;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        isPlaylist = true;
        setContentView(R.layout.activity_playlist_audio);
        playerIntent= new Intent(this, MediaPlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        playlistListView = (ListView) findViewById(R.id.audioPlaylistView);
            try
            {
                runningStatus = MediaPlayerService.getInstance().getRunningStatus();
            }
            catch (NullPointerException E)
            {
                runningStatus = false;
            }
        arrayAdapter = new ArrayAdapter<String>
                (
                this,
                R.layout.edit_list_layout,
                R.id.Itemname,
                fillPlaylist()
                );
        playlistListView.setAdapter(arrayAdapter);
        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if(isPlaylist)
                {     String   name = (String) arg0.getItemAtPosition(arg2);
                    ID = getPlaylistID(name);
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.edit_list_layout,
                            R.id.Itemname,
                            getSongsForPlaylists(ID));
                    playlistListView.setAdapter(arrayAdapter);
                    isPlaylist = false;
                }
                else
                {    String   name = (String) arg0.getItemAtPosition(arg2);
                    playAudio(getSong(name,arg2,ID ));

                }
            }



        });

    }

    public void goBack(View view) {
        if(isPlaylist == true)
        {
            Intent openNewFileIntent = new Intent(this, MainActivity.class);
            startActivity(openNewFileIntent);
        }
        if(isPlaylist == false)
        {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    R.layout.edit_list_layout,
                    R.id.Itemname,
                    fillPlaylist() );
            playlistListView.setAdapter(arrayAdapter);
            isPlaylist=true;
        }

    }

    public List<String> fillPlaylist()
    {
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String selection = "name LIKE "+"'usr_%'" ;
        List<String> album_array_list = new ArrayList<String>();
        Cursor cursor = cr.query(uri, null, selection, null, null);
        cursor.moveToFirst();
        do {
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
            album_array_list.add(data);
        }
        while (cursor.moveToNext());
        cursor.close();
        return album_array_list;
    }

    public ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            Log.d("MediaPlayer Error", service.service.getClassName() );
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    public int getPlaylistID(String playlistName)
    {
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String selection = "name LIKE "+"'"+playlistName+"%'" ;
        Integer playlistID;
        Cursor cursor = cr.query(uri, null, selection, null, null);
        cursor.moveToFirst();
        do
        {
            playlistID = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));

        }
        while (cursor.moveToNext());
        cursor.close();

        return playlistID;
    }

    public  List<String> getSongsForPlaylists(int id) {

        ContentResolver cr = this.getContentResolver();
        List<String> playlist_array_list = new ArrayList<String>();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cursor = cr.query(uri, null, null, null, null);
        int count = 0;
        count = cursor.getCount();
        playerService.setTrackCount(count);
        cursor.moveToFirst();
        do
        {
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) + " - " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            playlist_array_list.add(data);
        }
        while (cursor.moveToNext());
        cursor.close();
        return  playlist_array_list;
    }

    public String getSong(String playlistName, int trackPosition,int id)
    {
        String songName = new String();
        ContentResolver cr = this.getContentResolver();
        Uri uri =  MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cur = cr.query(uri, null, null, null, null);

        if( cur != null && cur.moveToPosition(trackPosition))
        {
            Track track = new Track(cur);
            songName = track.path;
            playerService.setSongName(track.artist+'-'+track.album+'-'+track.title);
            playerService.setActualTrackPosition(trackPosition);
            playerService.setAlbumName(playlistName);
            playerService.setTrack(track);
            playerService.setPlaylistID(id);
            playerService.setIsPlayedfromAlbum(false);
        }
        cur.close();
        return songName;
    }

    public  void playAudio(String media)
    {
        if (isMyServiceRunning(MediaPlayerService.class))
        {
            playerService.stopMedia();
        }
        playerIntent.putExtra("media", media);
        startService(playerIntent);
        runningStatus = true;
    }
}
