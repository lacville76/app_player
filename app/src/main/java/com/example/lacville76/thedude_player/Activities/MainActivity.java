package com.example.lacville76.thedude_player.Activities;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lacville76.thedude_player.R;
import java.util.*;
import static com.example.lacville76.thedude_player.R.id.albumThumb;
import static com.example.lacville76.thedude_player.R.id.volumeSeekbar;


public class MainActivity extends FragmentActivity implements EqualizerFragment.OnFragmentInteractionListener {

    SeekBar musicControlSeekbar;
    SeekBar volumeControlSeekbar;
    Handler seekHandler = new Handler();
    private MediaPlayerService playerService;
    boolean isRunning = false;
    boolean serviceBound = false;
    EqualizerFragment fragment;
    TextView songTitleLabel;
    TextView playerTimeLabel;
    Button stopPlayButton;
    Button playingModeButton;
    AudioManager audioManager;
    Random rand = new Random();
    ImageView albumView;
    Intent playerIntent;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        serviceBound = getIntent().getBooleanExtra("serviceBound",false);
        setContentView(R.layout.activity_main);
        musicControlSeekbar = (SeekBar)findViewById(R.id.musicControlBar);
        volumeControlSeekbar = (SeekBar)findViewById(volumeSeekbar);
        albumView = (ImageView) findViewById(albumThumb);
        songTitleLabel = (TextView)findViewById(R.id.songTitleLabel);
        playerTimeLabel = (TextView)findViewById(R.id.playerTimeLabel);
        stopPlayButton = (Button) findViewById(R.id.play_button);
        playingModeButton = (Button) findViewById(R.id.playingModeButton);
        playerIntent= new Intent(this, MediaPlayerService.class);
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);
        if(isRunning)
        {
            stopPlayButton.setBackgroundResource(R.drawable.pause_button);
        }
        else
        {
            stopPlayButton.setBackgroundResource(R.drawable.play_button);
        }

        musicControlSeekbar.setMax(0);
        seekUpdation();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeControlSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeControlSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        musicControlSeekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(playerService.getMediaPlayer() != null)
                {
                    playerTimeLabel.setText(getTimeString(progress));
                    songTitleLabel.setText(playerService.getSongName());
                    if(progress==0) seekBar.setMax(playerService.getMediaPlayer().getDuration());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar musicControlSeekbar) {
                if(playerService.getMediaPlayer() != null)
                {
                    playerService.setPlayerPosition(musicControlSeekbar.getProgress());
                }

            }
        });

        volumeControlSeekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar musicControlSeekbar) {

            }
        });
    }


    public void goBack(View view)
    {
        this.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void playTrack(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            if (isRunning)
            {
                playerService.pauseMedia();
                stopPlayButton.setBackgroundResource(R.drawable.play_button);
                isRunning = false;
            }
            else
            {
                try
                {
                    playerService.resumeMedia();
                    stopPlayButton.setBackgroundResource(R.drawable.pause_button);
                    isRunning = true;
                } catch (NullPointerException e)
                {
                    Toast.makeText(this, "Choose song", Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            Toast.makeText(this, "Choose song", Toast.LENGTH_LONG).show();
        }

    }

    public void setPlayingMode(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            if (playerService.getOrderedPlayingMode())
            {
                playerService.setOrderedPlayingMode(false);
                playingModeButton.setBackgroundResource(R.drawable.shuffle_image);
            }
            else
                {
                playerService.setOrderedPlayingMode(true);
                playingModeButton.setBackgroundResource(R.drawable.ordered_image);
            }
        }
    }


    public void openTrack(View view)
    {
        Intent openNewFileIntent = new Intent(this, ListFileActivity.class);
        openNewFileIntent.putExtra("serviceBound", serviceBound);
        startActivity(openNewFileIntent);

    }
    public void openListForEdit(View view)
    {
        if(playerService.getMediaPlayer() !=null)
        {
            Intent openNewFileIntent = new Intent(this, PlaylistEditActivity.class);
            startActivity(openNewFileIntent);
        }
        else
        {
            Toast.makeText(this, "Choose song",
                    Toast.LENGTH_LONG).show();
        }
    }
    public void openListForPlay(View view)
    {
        Intent openNewFileIntent = new Intent(this, PlaylistAudioActivity.class);
        startActivity(openNewFileIntent);

    }
    public void openEqualizer(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            fragment = new EqualizerFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.mainLayout, fragment).addToBackStack("").commit();
        }
        else
        {
            Toast.makeText(this, "Choose song",
                    Toast.LENGTH_LONG).show();
        }

    }
    public void stopTrack(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            if (isRunning)
            {
                stopPlayButton.setBackgroundResource(R.drawable.play_button);
                musicControlSeekbar.setProgress(0);
                playerService.setPlayerPosition(0);
                playerService.pauseMedia();
                isRunning = false;
            }
        }
        else
        {
            Toast.makeText(this, "Choose song",
                    Toast.LENGTH_LONG).show();
        }
    }
    public void playNext(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            if (playerService.getIsPlayedfromAlbum())
            {
              if (playerService.getOrderedPlayingMode())
              {
                 if (playerService.getActualTrackPosition() + 1 < playerService.getTrackCount())
                     playAudio(getSongFromAlbum(playerService.getAlbumID(), playerService.getActualTrackPosition() + 1));
                 else
                     playAudio(getSongFromAlbum(playerService.getAlbumID(), 0));

            }
            else
                {
                try
                {
                    int randomNum = rand.nextInt((playerService.getTrackCount() - 0) + 1) + 0;
                    playAudio(getSongFromAlbum(playerService.getAlbumID(), randomNum));
                }
                catch (IllegalStateException | UnsupportedOperationException e)
                {
                }
            }
        }
        else
            {
            if (playerService.getOrderedPlayingMode())
            {
                if (playerService.getActualTrackPosition() + 1 < playerService.getTrackCount())
                    playAudio(getSongfromPlaylist(playerService.getAlbumName(), playerService.getActualTrackPosition() + 1, playerService.getPlaylistID()));
                else
                    playAudio(getSongfromPlaylist(playerService.getAlbumName(), 0, playerService.getPlaylistID()));

            } else
                {
                try
                {
                    int randomNum = rand.nextInt((playerService.getTrackCount() - 0) + 1) + 0;
                    playAudio(getSongfromPlaylist(playerService.getAlbumName(), randomNum, playerService.getPlaylistID()));
                } catch (IllegalStateException | UnsupportedOperationException e) {

                }
            }
        }
        musicControlSeekbar.setMax(playerService.getMediaPlayer().getDuration());
        songTitleLabel.setText(playerService.getSongName());
        musicControlSeekbar.setProgress(playerService.getActualTrackPosition());
    }
    else
    {
        Toast.makeText(this, "Choose song",
                Toast.LENGTH_LONG).show();
    }

    }
    public void playPrevious(View view)
    {
        if(playerService.getMediaPlayer() != null)
        {
            if (playerService.getIsPlayedfromAlbum())
            {
                if (playerService.getOrderedPlayingMode())
                {
                    if (playerService.getActualTrackPosition() - 1 >= 0)
                        playAudio(getSongFromAlbum(playerService.getAlbumID(), playerService.getActualTrackPosition() - 1));
                    else
                        playAudio(getSongFromAlbum(playerService.getAlbumID(), playerService.getTrackCount() - 1));
                }
            else
            {

                int randomNum = rand.nextInt((playerService.getTrackCount() - 0) + 1) + 0;
                playAudio(getSongFromAlbum(playerService.getAlbumID(), randomNum));
            }
        }
        else
            {
            if (playerService.getOrderedPlayingMode())
            {
                if (playerService.getActualTrackPosition() - 1 >= 0)
                    playAudio(getSongfromPlaylist(playerService.getAlbumName(), playerService.getActualTrackPosition() - 1, playerService.getPlaylistID()));
                else
                    playAudio(getSongfromPlaylist(playerService.getAlbumName(), playerService.getTrackCount() - 1, playerService.getPlaylistID()));
            }
            else
            {

                int randomNum = rand.nextInt((playerService.getTrackCount() - 0) + 1) + 0;
                playAudio(getSongfromPlaylist(playerService.getAlbumName(), randomNum, playerService.getPlaylistID()));
            }
        }

        musicControlSeekbar.setMax(playerService.getMediaPlayer().getDuration());
        songTitleLabel.setText(playerService.getSongName());
        musicControlSeekbar.setProgress(playerService.getActualTrackPosition());
    }
        else
        {
            Toast.makeText(this, "Wybierz utwór",
                    Toast.LENGTH_LONG).show();
        }
    }


    Runnable run = new Runnable() {

        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        try{
            musicControlSeekbar.setProgress(playerService.getPlayerPosition());
        }
        catch (NullPointerException e)
        {

        }
        seekHandler.postDelayed(run, 1000);
    }


    public void finishFragment(View view)
    {

        onBackPressed();
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
    public  void playAudio(String media)
    {
        if (isMyServiceRunning(MediaPlayerService.class))
        {
            playerService.stopMedia();
        }
        playerIntent.putExtra("media", media);
        startService(playerIntent);
        isRunning = true;
        stopPlayButton.setBackgroundResource(R.drawable.pause_button);
    }

    private String getTimeString(long millis) {
        StringBuffer buffer = new StringBuffer();
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buffer
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));
        return buffer.toString();
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playerService = binder.getService();
            isRunning = true;
            try
            {

                musicControlSeekbar.setMax(playerService.getMediaPlayer().getDuration());
                songTitleLabel.setText(playerService.getSongName());
                musicControlSeekbar.setProgress(playerService.getActualTrackPosition());
                stopPlayButton.setBackgroundResource(R.drawable.pause_button);
                playerService.setOrderedPlayingMode(true);
                Drawable img = Drawable.createFromPath(getCoverArtPath(playerService.getAlbumID(),getApplicationContext()));
                if(img !=null)
                {
                    albumView.setImageDrawable(img);
                }
            }
            catch (NullPointerException e)
            {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    public String getSongFromAlbum(long albumID, int trackPosition)
    {
        String songName = new String();
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM_ID + " == " + "'"+albumID+"'";
        String sortOrder = MediaStore.Audio.Media.TRACK + " ASC";
        Cursor cur = contentResolver.query(uri, null, selection, null, sortOrder);
        if( cur != null && cur.moveToPosition(trackPosition)){
            Track track = new Track(cur);
            songName = track.path;
            playerService.setSongName(track.artist+'-'+track.album+'-'+track.title);
            playerService.setActualTrackPosition(trackPosition);
            playerService.setTrack(track);
            playerService.setIsPlayedfromAlbum(true);
        }
        cur.close();
        return songName;
    }

    public String getSongfromPlaylist(String albumName, int trackPosition,int id)
    {
        String songName = new String();
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri =  MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if( cursor != null && cursor.moveToPosition(trackPosition))
        {
            Track track = new Track(cursor);
            songName = track.path;
            playerService.setSongName(track.artist+'-'+track.album+'-'+track.title);
            playerService.setActualTrackPosition(trackPosition);
            playerService.setAlbumName(albumName);
            playerService.setTrack(track);
            playerService.setPlaylistID(id);
        }
        cursor.close();
        return songName;
    }

    private static String getCoverArtPath(long albumId, Context context)
    {
        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );

        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult)
        {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }
}
