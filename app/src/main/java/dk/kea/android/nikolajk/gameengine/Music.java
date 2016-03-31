package dk.kea.android.nikolajk.gameengine;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Nikol_000 on 14-03-2016.
 */
public class Music implements MediaPlayer.OnCompletionListener
{
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;

    public Music(AssetFileDescriptor assetFileDescriptor)
    {
        mediaPlayer = new MediaPlayer();
        try
        {
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            mediaPlayer.prepare();
            isPrepared = true;
            mediaPlayer.setOnCompletionListener(this);
        }catch (Exception e)
        {
            throw new RuntimeException("Could not load the music file!!");
        }
    }

    public void dispose()
    {
        if (mediaPlayer.isPlaying()){ mediaPlayer.stop(); }
        mediaPlayer.release();
    }

    public boolean isLooping()
    {
        return mediaPlayer.isLooping();
    }

    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

    public boolean isStopped()
    {
        return !isPrepared;
    }

    public void pause()
    {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
        }
    }

    public void play()
    {
        if (mediaPlayer.isPlaying()) return;

        try
        {
            synchronized (this)
            {
                if (!isPrepared) { mediaPlayer.prepare();}
                mediaPlayer.start();
            }
        }catch (IllegalStateException isE)
        {
            isE.printStackTrace();
        }catch (IOException ioE)
        {
            ioE.printStackTrace();
        }
    }

    public void setLooping(boolean loop)
    {
        mediaPlayer.setLooping(loop);
    }

    public void setVolume(float volume)
    {
        mediaPlayer.setVolume(volume,volume);
    }

    public void stop()
    {
        synchronized (this)
        {
            if (!isPrepared) { return; }
            mediaPlayer.stop();
            isPrepared = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        synchronized (this)
        {
            isPrepared = false;
        }
    }
}
