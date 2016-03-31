package dk.kea.android.nikolajk.gameengine;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class GameEngine extends Activity
        implements Runnable, View.OnKeyListener, SensorEventListener
{

    private Thread mainLoopThread;
    private GameState state = GameState.Paused;
    private List<GameState> stateChanges = new ArrayList<>();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Screen screen;
    private Canvas virtualCanvas, physicalCanvas;
    private Rect src = new Rect(), dst = new Rect();
    private Bitmap virtualSurface;
    private boolean pressedKeys[] = new boolean[256];

    private KeyEventPool keyEventPool = new KeyEventPool();
    private List<MyKeyEvent> keyEvents = new ArrayList<>();
    private List<MyKeyEvent> keyEventBuffer = new ArrayList<>();

    private TouchHandler touchHandler;
    private TouchEventPool touchEventPool = new TouchEventPool();
    private List<TouchEvent> touchEvents = new ArrayList<>();
    private List<TouchEvent> touchEventBuffer = new ArrayList<>();


    private float[] accelerometer = new float[3];

    private SoundPool soundPool;

    private int framesPerSecond = -1;


    // end of global variables and start of methods

    public abstract Screen createStartScreen();

    @Override
    protected void onCreate(Bundle instanceBundle)
    {
        super.onCreate(instanceBundle);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);
        surfaceHolder = surfaceView.getHolder();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.soundPool = new SoundPool(20,AudioManager.STREAM_MUSIC ,0);
        screen = createStartScreen();
        if (surfaceView.getWidth() > surfaceView.getHeight())
        {
            setVirtualSurface(480,320);
        }else
        {
            setVirtualSurface(320,480);
        }
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocus();
        surfaceView.setOnKeyListener(this);
        touchHandler = new MultiTouchHandler(surfaceView, touchEventBuffer, touchEventPool);
        SensorManager manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0)
        {
            Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void onPause()
    {
        super.onPause();
        synchronized (stateChanges)
        {
            if(isFinishing())
            {
                stateChanges.add(stateChanges.size(), GameState.Disposed);
                ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
            }
            else
            {
                stateChanges.add(stateChanges.size(), GameState.Paused);
            }
        }
        try
        {
            mainLoopThread.join();
        }
        catch (InterruptedException ie)
        {
            Log.d("BobGame","Interrupted Exeption");
        }
        if (isFinishing())
        {
            soundPool.release();
        }
    }

    public void onResume()
    {
        super.onResume();
        mainLoopThread = new Thread(this);
        mainLoopThread.start();
        synchronized (stateChanges)
        {
            stateChanges.add(stateChanges.size(),GameState.Resumed);
        }
    }

    public void setScreen(Screen newScreen)
    {
        if (screen != null) { screen.dispose(); }
        screen = newScreen;
    }

    public void setVirtualSurface(int width, int height)
    {
        if (virtualSurface != null) virtualSurface.recycle();
        virtualSurface = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        virtualCanvas = new Canvas(virtualSurface);
    }

    public Bitmap loadBitMap(String fileName)
    {
        InputStream in = null;
        try
        {
            in = this.getAssets().open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null) throw new RuntimeException("Could not get a bitmap from the file: " + fileName);
            return bitmap;
        }catch (IOException ioe)
        {
            throw new RuntimeException("Could not load the file: " + fileName);
        }finally
        {
            if (in != null )
            {
                try
                {
                    in.close();
                }catch (IOException ioe)
                {
                    Log.d("Closing inputStream","Could not close inputStream");
                }
            }
        }
    }


    public Music loadMusic(String fileName)
    {
        try
        {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(fileName);
            return new Music(assetFileDescriptor);
        }catch (IOException ioE)
        {
            throw new RuntimeException("Could not load music file: " + fileName);
        }
    }

    public Sound loadSound(String fileName)
    {
        try
        {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(fileName);
            int soundID = soundPool.load(assetFileDescriptor, 0);
            Sound soundTrack = new Sound(soundPool, soundID);
            return soundTrack;
        }catch (IOException ioE)
        {
            throw new RuntimeException("Could not load sound file: " + fileName + " BAD ERROR!!" );
        }
    }


    public void clearFrameBuffer(int color)
    {
        if (virtualCanvas != null) virtualCanvas.drawColor(color);
    }

    public int getFrameBufferWidth()
    {
        return surfaceView.getWidth();
    }

    public int getFrameBufferHeight()
    {
        return surfaceView.getHeight();
    }

    public int getVirtualScreenWidth()
    {
        return virtualSurface.getWidth();
    }

    public int getVirtualScreenHeight()
    {
        return virtualSurface.getHeight();
    }

    public void drawBitmap(Bitmap bitmap, int x, int y)
    {
        if (virtualCanvas != null) virtualCanvas.drawBitmap(bitmap,x,y,null);
    }

    public void drawBitmap(Bitmap bitmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight)
    {
        if (virtualCanvas == null) return;
        src.left = srcX;
        src.top = srcY;
        src.right = srcX + srcWidth;
        src.bottom = srcY + srcHeight;

        dst.left = x;
        dst.top = y;
        dst.right = x + srcWidth;
        dst.bottom = y + srcHeight;

        virtualCanvas.drawBitmap(bitmap, src, dst, null);
    }

    public boolean onKey(View currentView, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            pressedKeys[keyCode] = true;
        }else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            pressedKeys[keyCode] = false;
        }
        return false;
    }

    public boolean isKeyPressed(int keyCode)
    {
        return pressedKeys[keyCode];
    }

    public boolean isTouchDown(int pointer)
    {
        return touchHandler.isTouchDown(pointer);
    }

    public int getTouchX(int pointer)
    {
        float ratioX = (float)virtualSurface.getWidth() / (float)surfaceView.getWidth();
        int x = touchHandler.getTouchX(pointer);
        x = (int)(x * ratioX);

        return x;
    }

    public int getTouchY(int pointer)
    {
        float ratioY = (float)virtualSurface.getHeight() / (float)surfaceView.getHeight();
        int y = touchHandler.getTouchY(pointer);
        y = (int)(y * ratioY);

        return y;
    }

    private void fillEvents()
    {
        synchronized (keyEventBuffer)
        {
            int stop = keyEventBuffer.size();
            for (int i = 0; i < stop ;i++ )
            {
                keyEvents.add(keyEventBuffer.get(i));
            }
            keyEventBuffer.clear();
        }
    }

    private void freeEvents()
    {
        synchronized (keyEvents)
        {
            int stop = keyEvents.size();
            for(int i = 0; i < stop; i++)
            {
                keyEventPool.free(keyEvents.get(i));
            }
        }
    }
    /*
    public List<MyKeyEvent> getKeyEvents()
    {
        return null;
    }
    */

    public float[] getAccelerometer()
    {
        return accelerometer;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    public void onSensorChanged(SensorEvent event)
    {
        System.arraycopy(event.values, 0, accelerometer,0,3);
    }

    public int getFrameRate()
    {
        return framesPerSecond;
    }

    public void run()
    {
        int frames = 0;
        long lastTime = System.nanoTime(), currentTime, startTime = System.nanoTime();

        while (true)
        {
            synchronized (stateChanges)
            {
                for (int i=0 ; i < stateChanges.size();i++)
                {
                    state = stateChanges.get(i);
                    if (state == GameState.Disposed)
                    {
                        if (screen != null) screen.dispose();
                        Log.d("BobGame", "GameState is Disposed");
                    }
                    else if (state == GameState.Paused)
                    {
                        if (screen != null) screen.pause();
                        Log.d("BobGame", "GameState is Paused");
                    }
                    else if (state == GameState.Resumed)
                    {
                        if (screen != null) screen.resume();
                        state = GameState.Running;
                        Log.d("BobGame", "GameState is Resumed");
                    }
                }
                stateChanges.clear();
            }

            if (state == GameState.Running)
            {
                if(!surfaceHolder.getSurface().isValid()) continue;
                physicalCanvas = surfaceHolder.lockCanvas();
                currentTime = System.nanoTime();
                if (screen != null) screen.update((currentTime-lastTime)/1000000000.0f);
                lastTime = currentTime;

                src.left = 0;
                src.top = 0;
                src.right = virtualSurface.getWidth()-1;
                src.bottom = virtualSurface.getHeight()-1;

                dst.left = 0;
                dst.top = 0;
                dst.right = surfaceView.getWidth();
                dst.bottom = surfaceView.getHeight();

                physicalCanvas.drawBitmap(virtualSurface, src, dst, null);
                surfaceHolder.unlockCanvasAndPost(physicalCanvas);
                physicalCanvas = null;
            }

            frames++;
            if (System.nanoTime() - startTime >= 1000000000)
            {
                framesPerSecond = frames;
                frames = 0;
                startTime = System.nanoTime();
            }

        }
    }
}