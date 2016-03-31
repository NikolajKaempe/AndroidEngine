package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 07-03-2016.
 */
public class KeyEventPool extends Pool<MyKeyEvent>
{
    @Override
    protected MyKeyEvent newItem()
    {
        return new MyKeyEvent();
    }
}
