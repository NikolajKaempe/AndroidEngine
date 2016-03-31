package dk.kea.android.nikolajk.gameengine;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by Nikol_000 on 17-02-2016.
 */
public class BobScreen extends Screen
{
    Bitmap bob, bitmapWalkingMario, bitmapSonicMovement;
    BitmapPoints[] marioPoints, sonicPoints;
    int marioAction = 0, sonicAction = 0;
    boolean sonicInJump = false;
    float x = 0, y = 0, x1=0, y1 = 200;
    final double marioResetValue = 0.1, sonicResetValue = 0.175;
    double marioResetWalk = marioResetValue, sonicResetWalk = sonicResetValue ;
    int clearColor = Color.BLACK;
    Sound soundTrack;
    Music music;
    boolean userWantsMusic = false;

    public BobScreen(GameEngine gameEngine)
    {
        super(gameEngine);

        bob = gameEngine.loadBitMap("marioSprite.jpg");
        bitmapWalkingMario = gameEngine.loadBitMap("walkingMarioSprite.jpg");
        marioPoints = new BitmapPoints[10];
        marioPoints[0] = new BitmapPoints(840,8,80,115);
        marioPoints[1] = new BitmapPoints(88,5,76,116);
        marioPoints[2] = new BitmapPoints(176,5,75,116);
        marioPoints[3] = new BitmapPoints(261,5,80,115);
        marioPoints[4] = new BitmapPoints(343,5,80,117);
        marioPoints[5] = new BitmapPoints(430,5,79,118);
        marioPoints[6] = new BitmapPoints(509,5,78,118);
        marioPoints[7] = new BitmapPoints(590,5,78,118);
        marioPoints[8] = new BitmapPoints(674,4,79,118);
        marioPoints[9] = new BitmapPoints(754,5,79,119);

        bitmapSonicMovement = gameEngine.loadBitMap("SonicMovement.png");
        sonicPoints = new BitmapPoints[15];
        sonicPoints[0] = new BitmapPoints(12,57,25,35);
        sonicPoints[1] = new BitmapPoints(40,56,30,37);
        sonicPoints[2] = new BitmapPoints(74,53,32,35);
        sonicPoints[3] = new BitmapPoints(106,56,31,36);
        sonicPoints[4] = new BitmapPoints(144,58,25,33);
        sonicPoints[5] = new BitmapPoints(172,56,30,36);
        sonicPoints[6] = new BitmapPoints(205,53,33,34);
        sonicPoints[7] = new BitmapPoints(238,57,30,35);
        sonicPoints[8] = new BitmapPoints(157,103,35,42);
        sonicPoints[9] = new BitmapPoints(44,111,26,29);
        sonicPoints[10] = new BitmapPoints(74,112,30,27);
        sonicPoints[11] = new BitmapPoints(108,110,26,29);
        sonicPoints[12] = new BitmapPoints(10,112,30,27);
        sonicPoints[13] = new BitmapPoints(157,103,35,42);
        sonicPoints[14] = new BitmapPoints(70,13,30,31);

        //soundTrack = gameEngine.loadSound("explosion.ogg");
        music = gameEngine.loadMusic("music.ogg");
    }

    public void update(float deltaTime)
    {
        Log.d("FrameRate", "fps: " + gameEngine.getFrameRate());
        gameEngine.clearFrameBuffer(clearColor);
        x = x + 30 * deltaTime;
        x1 = x1 + 50* deltaTime;
        y1 = y1 + 30* deltaTime;
        marioResetWalk -= deltaTime;
        sonicResetWalk -= deltaTime;

        // Checks
        if (x > gameEngine.getVirtualScreenWidth()) { x = -80; }
        if (x1 > gameEngine.getVirtualScreenWidth()){ x1 = -30; }
        if (y1 > gameEngine.getVirtualScreenHeight()){ y1= -30; }
        if (marioResetWalk <= 0){ marioAction++; marioResetWalk = marioResetValue; }
        if (sonicResetWalk <= 0){ sonicAction++; sonicResetWalk = sonicResetValue; }
        if (marioAction >= marioPoints.length) { marioAction = 0; }
        if (sonicAction >= sonicPoints.length ) {sonicAction=0;sonicInJump = false; }
        if (!sonicInJump && sonicAction >= 8){ sonicAction=0; }
        if (sonicInJump && sonicAction >= sonicPoints.length){ sonicInJump=false; sonicAction=0; }
        if (sonicInJump && sonicAction <= 11 && sonicAction >= 8)
        {
            if(sonicResetWalk == sonicResetValue )
            {
                y1-= 10;
            }
        }
        if (sonicInJump && sonicAction <= 14 && sonicAction >= 12)
        {
            if(sonicResetWalk == sonicResetValue )
            {
                y1+=10;
            }
        }
            // Logic
        gameEngine.drawBitmap(bitmapWalkingMario,(int)x*0 + 10,(int)y,
                marioPoints[marioAction].getSrcX(), marioPoints[marioAction].getSrcY(),
                marioPoints[marioAction].getWidth(), marioPoints[marioAction].getHeight());

        gameEngine.drawBitmap(bitmapSonicMovement,(int)x1,(int)y1,
                sonicPoints[sonicAction].getSrcX(),sonicPoints[sonicAction].getSrcY(),
                sonicPoints[sonicAction].getWidth(),sonicPoints[sonicAction].getHeight());

        gameEngine.drawBitmap(bob,(int)x,(int)y1+100);

        if (gameEngine.isTouchDown(0))
        {
            if (!sonicInJump)
            {
                sonicInJump = true;
                sonicAction = 8;
                sonicResetWalk = sonicResetValue;
            }


            if (userWantsMusic)
            {
                music.pause();
                userWantsMusic = false;
            }else
            {
                music.play();
                userWantsMusic = true;
            }

        }

        /*
        for(int pointer = 0; pointer < 5; pointer++)
        {
            if(gameEngine.isTouchDown(pointer))
            {
                gameEngine.drawBitmap(bob, gameEngine.getTouchX(pointer),gameEngine.getTouchY(pointer));
                soundTrack.play(1);
            }
        }

        */
        
        /*
        float x = -1*gameEngine.getAccelerometer()[0];
        float y = gameEngine.getAccelerometer()[1];
        x = (x/10) * gameEngine.getFrameBufferWidth()/2 + gameEngine.getVirtualScreenWidth()/2;
        y = (y/10) * gameEngine.getFrameBufferWidth()/2 + gameEngine.getVirtualScreenHeight()/2;
        gameEngine.drawBitmap(bob, (int)x-64,(int)y-64);
            */

    }
    public void pause()
    {
        music.pause();
        Log.d("BobScreen","We are Pausing the game");
    }

    public void resume()
    {
        if(userWantsMusic) { music.play();}
        Log.d("BobScreen","We are Resuming the game");
    }

    public void dispose()
    {
        Log.d("BobScreen","We are Disposing the game");
    }
}
