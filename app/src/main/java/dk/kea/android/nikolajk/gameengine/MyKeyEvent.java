package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 07-03-2016.
 */
public class MyKeyEvent
{
    public enum KeyEventType
    {
        Down,
        Up
    }

    public KeyEventType type;
    public int keyCode;
    public char character;
}
