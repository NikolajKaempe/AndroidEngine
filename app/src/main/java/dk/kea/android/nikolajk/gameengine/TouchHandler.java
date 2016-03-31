package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 01-03-2016.
 */
public interface TouchHandler
{
    boolean isTouchDown(int pointer);
    int getTouchX(int pointer);
    int getTouchY(int pointer);
}
