package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 17-02-2016.
 */
public class BobGame extends GameEngine
{

    @Override
    public Screen createStartScreen()
    {

        return new BobScreen(this);
    }
}
