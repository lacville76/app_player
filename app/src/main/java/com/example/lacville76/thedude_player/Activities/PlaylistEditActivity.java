package com.example.lacville76.thedude_player.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.lacville76.thedude_player.R;
import java.util.ArrayList;
import java.util.List;

public class PlaylistEditActivity extends Activity {
    Intent playerIntent;
    private static final String TAG ="" ;
    private ListView playlistEditListview;
    private MediaPlayerService playerService;
    int ID;
    private String dialogMessage = "";
     ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerIntent= new Intent(this, MediaPlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.activity_playlist);
        playlistEditListview = (ListView) findViewById(R.id.playlistsListView);
        arrayAdapter = new ArrayAdapter<String>(
                this,
                R.layout.edit_list_layout,
                R.id.Itemname,
        fullfillPlaylist() );
        playlistEditListview.setAdapter(arrayAdapter);
        playlistEditListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String   name = (String) arg0.getItemAtPosition(arg2);
                ID = getPlaylistID(name);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    builder = new AlertDialog.Builder(arg1.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else
                    {
                    builder = new AlertDialog.Builder(arg1.getContext());
                }
                builder.setTitle("Adding song")
                        .setMessage("Are you sure you want to add new song?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                addToPlaylist(ID);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }



        });

        playlistEditListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {

                String   name = (String) arg0.getItemAtPosition(pos);
                final int position = pos;
                ID = getPlaylistID(name);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(arg1.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(arg1.getContext());
                }
                builder.setTitle("Deleting playlist")
                        .setMessage("Are you sure you want to delete this playlist?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removePlaylist(ID);
                                arrayAdapter.remove(arrayAdapter.getItem(position));
                                arrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });
    }

    public void goBack(View view)
    {
        Intent openNewFileIntent = new Intent(this, MainActivity.class);
        startActivity(openNewFileIntent);
    }
    public void insertPlaylist(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adding new playlist");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogMessage = input.getText().toString();
                addPlaylist("usr_"+ dialogMessage);
                arrayAdapter = new ArrayAdapter<String>(
                        getBaseContext(),
                        R.layout.edit_list_layout,
                        R.id.Itemname,
                        fullfillPlaylist() );
                arrayAdapter.notifyDataSetChanged();
                playlistEditListview.setAdapter(arrayAdapter);
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

    public boolean addPlaylist(String pname) {
        boolean flag = false;
        ContentResolver contentResolver = this.getContentResolver();
        Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;;
        Cursor cursor = contentResolver.query(playlists, new String[] { "*" }, null, null, null);
        long playlistId = 0;
        cursor.moveToFirst();
        do {
            String plname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
            if (plname.equalsIgnoreCase(pname))
            {
                playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                break;
            }
        } while (cursor.moveToNext());
        cursor.close();

        if (playlistId != 0)
        {
            Uri deleteUri = ContentUris.withAppendedId(playlists, playlistId);
            Log.d(TAG, "REMOVING Existing Playlist: " + playlistId);
            contentResolver.delete(deleteUri, null, null);
        }
        Log.d(TAG, "CREATING PLAYLIST: " + pname);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Playlists.NAME, pname);
        contentValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
                System.currentTimeMillis());
        Uri newPlaylist = contentResolver.insert(playlists, contentValues);
        Log.d(TAG, "Added PlayLIst: " + newPlaylist);
        flag=true;
        return flag;
    }

    public List<String> fullfillPlaylist()
    {
        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String selection = "name LIKE "+"'usr_%'" ;
        List<String> audio_array_list = new ArrayList<String>();
        Cursor cur = cr.query(uri, null, selection, null, null);
        cur.moveToFirst();
        do {
            String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Playlists.NAME));
            audio_array_list.add(data);
            }
         while (cur.moveToNext());
        cur.close();

        return audio_array_list;
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

    public int getPlaylistID(String playlistName)
    {

        ContentResolver cr = this.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        String selection = "name LIKE "+"'"+playlistName+"%'" ;
        Integer playlistID;
        Cursor cur = cr.query(uri, null, selection, null, null);
        cur.moveToFirst();
        do
        {
             playlistID = cur.getInt(cur.getColumnIndex(MediaStore.Audio.Playlists._ID));
        }
        while (cur.moveToNext());
        cur.close();

        return playlistID;
    }
    public  void addToPlaylist(int id)
    {
        ContentResolver cr = this.getContentResolver();
        String[] cols = new String[]
                {
                "count(*)"
                };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cursor = cr.query(uri, cols, null, null, null);
        cursor.moveToFirst();
        final int base = cursor.getInt(0);
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, (base + playerService.getTrack().id));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, playerService.getTrack().id);
        cr.insert(uri, values);
        Toast.makeText(this, "Song added to list",
                Toast.LENGTH_LONG).show();
    }

    public void removePlaylist(int id)
    {
        ContentResolver contentResolver = this.getContentResolver();
        Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Uri deleteUri = ContentUris.withAppendedId(playlists, id);
        contentResolver.delete(deleteUri, null, null);
    }

}
