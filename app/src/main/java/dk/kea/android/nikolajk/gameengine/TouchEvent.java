package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 01-03-2016.
 */
public class TouchEvent
{
    public enum TouchEventType
    {
        Down,
        Up,
        Dragged
    }
    public TouchEventType type;

    public int x;
    public int y;
    public int pointer;
}
