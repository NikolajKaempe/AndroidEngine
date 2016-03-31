package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 16-03-2016.
 */
public class BitmapPoints
{
    private int srcX, srcY, width, height;

    public BitmapPoints(int srcX,int srcY,int width, int height)
    {
        this.srcX = srcX;
        this.srcY = srcY;
        this.width = width;
        this.height = height;
    }

    public int getSrcX()
    {
        return srcX;
    }

    public int getSrcY()
    {
        return srcY;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
