package com.example.lacville76.thedude_player.Activities;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

/**
 * Created by lacville76 on 2017-09-07.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {
    public MediaPlayer mediaPlayer;
    private String mediaFile;
    private AudioManager audioManager;
    private int resumePosition;
    public boolean isRunning;
    private final IBinder iBinder =  new LocalBinder();
    private static MediaPlayerService mService;
    private String songName;
    private String albumName;
    private int ActualTrackPosition;
    private int trackCount;
    Equalizer equalizer;
    Track mpTrack;
    private boolean orderedPlayingMode;
    public int     playlistID;
    public long albumID;
    public boolean isPlayedfromAlbum ;

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        if(this.isPlayedfromAlbum)
        {
            if (this.getOrderedPlayingMode()) {
                if (this.getActualTrackPosition() + 1 < this.getTrackCount())
                    mediaFile = (getSongfromAlbum(this.getAlbumID(), this.getActualTrackPosition() + 1));
                else
                    mediaFile = (getSongfromAlbum(this.getAlbumID(), 0));

            } else {
                try {
                    if (this.getActualTrackPosition() + 1 < this.getTrackCount())
                        mediaFile = (getSongfromAlbum(this.getAlbumID(), this.getActualTrackPosition() + 1));
                    else
                        mediaFile = (getSongfromAlbum(this.getAlbumID(), 0));

                } catch (IllegalStateException | UnsupportedOperationException e) {

                }
            }
        }
        else
        {
            if (this.getOrderedPlayingMode()) {
                if (this.getActualTrackPosition() + 1 < this.getTrackCount())
                    mediaFile = (getSongfromPlaylist(this.getAlbumName(), this.getActualTrackPosition() + 1,this.playlistID));
                else
                    mediaFile = (getSongfromPlaylist(this.getAlbumName(), 0,this.playlistID));

            } else {
                try {
                    if (this.getActualTrackPosition() + 1 < this.getTrackCount())
                        mediaFile = (getSongfromPlaylist(this.getAlbumName(), this.getActualTrackPosition() + 1,this.playlistID));
                    else
                        mediaFile = (getSongfromPlaylist(this.getAlbumName(), 0,this.playlistID));

                } catch (IllegalStateException | UnsupportedOperationException e) {

                }
            }
        }
        stopSelf();
        initMediaPlayer();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                equalizer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        try {
            mediaPlayer.prepareAsync();
        }
        catch (RuntimeException e)
        {
            stopSelf();
        }
        mService = this;
        Log.d("MediaPlayer Error", "Zainicjalizowano " );
        try {
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);
            equalizer.getNumberOfBands(); //it tells you the number of equalizer in device.
            equalizer.getNumberOfPresets();
        }
        catch (RuntimeException e)
        {
            stopSelf();
        }
    }
    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }


    }


    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            //An audio file is passed to the service through putExtra();
            mediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();

        }

        //Request audio focus
        if (requestAudioFocus() == false) {

            stopSelf();
        }

        if (mediaFile != null && mediaFile != "")
            initMediaPlayer();
        mService = this;
        isRunning = true;
        return START_NOT_STICKY  ;
    }
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
            equalizer.release();
        }

        removeAudioFocus();
    }


    public String getSongfromAlbum(long albumID, int trackPosition)
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
            this.setSongName(track.artist+'-'+track.album+'-'+track.title);
            this.setActualTrackPosition(trackPosition);
            this.setTrack(track);
            this.setIsPlayedfromAlbum(true);
        }
        cur.close();
        return pos;
    }

    public String getSongfromPlaylist(String albumName, int trackPosition,int id)
    {
        String pos = new String();
        ContentResolver cr = this.getContentResolver();
        Uri uri =  MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cur = cr.query(uri, null, null, null, null);

        if( cur != null && cur.moveToPosition(trackPosition)){
            Track track = new Track(cur);
            pos = track.path;
            this.setSongName(track.artist+'-'+track.album+'-'+track.title);
            this.setActualTrackPosition(trackPosition);
            this.setAlbumName(albumName);
            this.setTrack(track);
            this.setPlaylistID(id);
        }
        cur.close();
        return pos;
    }
    public boolean getRunningStatus()
    {
        return isRunning;
    }
    public boolean setRunningStatus(boolean IsRunning)
    {
        isRunning=IsRunning;
        return isRunning;
    }
    public static MediaPlayerService getInstance()
    {
        return mService;
    }
    public String getMediaFile()
    {
        return mediaFile;
    }
    public int getPlayerPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }
    public void setPlayerPosition(int position)
    {
        mediaPlayer.seekTo(position);
    }
    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }
    public void setSongName(String songName)
    {
        this.songName = songName;
    }
    public String getSongName()
    {
        return songName;
    }
    public void setActualTrackPosition(int ActualTrackPosition)
    {
        this.ActualTrackPosition = ActualTrackPosition;
    }
    public int getActualTrackPosition()
    {
        return ActualTrackPosition;
    }
    public void setTrackCount(int trackCount)
    {
        this.trackCount = trackCount;
    }
    public int getTrackCount()
    {
        return trackCount;
    }
    public void setAlbumName(String albumName)
    {
        this.albumName = albumName;
    }
    public String getAlbumName()
    {
        return albumName;
    }
    public Equalizer getEqualizer()
    {
        return equalizer;
    }
    public Track getTrack()
    {
        return this.mpTrack;
    }
    public void setTrack(Track track)
    {
        this.mpTrack = track;
    }
    public boolean getOrderedPlayingMode()
    {
        return this.orderedPlayingMode;
    }
    public void setOrderedPlayingMode(boolean orderedPlayingMode)
    {
        this.orderedPlayingMode=orderedPlayingMode;
    }
    public boolean getIsPlayedfromAlbum()
    {
        return  isPlayedfromAlbum;
    }
    public void setIsPlayedfromAlbum(boolean isPlayedfromAlbum)
    {
        this.isPlayedfromAlbum=isPlayedfromAlbum;
    }
    public void setPlaylistID(int playlistID)
    {
        this.playlistID = playlistID;
    }
    public int getPlaylistID()
    {
        return playlistID;
    }
    public void setAlbumID(long albumID)
    {
        this.albumID = albumID;
    }
    public  long getAlbumID()
    {
        return albumID;
    }
}