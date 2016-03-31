package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 01-03-2016.
 */
public class TouchEventPool extends Pool<TouchEvent>
{
    protected TouchEvent newItem()
    {
        return new TouchEvent();
    }
}
