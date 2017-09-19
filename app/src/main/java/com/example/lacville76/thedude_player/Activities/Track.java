package com.example.lacville76.thedude_player.Activities;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


public class Track {

    public long     id;
    public long     albumId;
    public long     artistId;
    public String   path;
    public String   title;
    public String   album;
    public String   artist;
    public Uri uri;            // URI
    public long     duration;
    public int      trackNo;


    public  Track(Cursor cursor)
    {
        id              = cursor.getLong( cursor.getColumnIndex( MediaStore.Audio.Media._ID ));
        path            = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        title           = cursor.getString( cursor.getColumnIndex( MediaStore.Audio.Media.TITLE ));
        album           = cursor.getString( cursor.getColumnIndex( MediaStore.Audio.Media.ALBUM ));
        artist          = cursor.getString( cursor.getColumnIndex( MediaStore.Audio.Media.ARTIST ));
        albumId         = cursor.getLong( cursor.getColumnIndex( MediaStore.Audio.Media.ALBUM_ID ));
        artistId        = cursor.getLong( cursor.getColumnIndex( MediaStore.Audio.Media.ARTIST_ID ));
        duration        = cursor.getLong( cursor.getColumnIndex( MediaStore.Audio.Media.DURATION ));
        trackNo         = cursor.getInt( cursor.getColumnIndex( MediaStore.Audio.Media.TRACK ));
        uri             = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

}