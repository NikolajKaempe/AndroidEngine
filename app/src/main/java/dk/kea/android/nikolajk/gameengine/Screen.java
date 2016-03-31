package dk.kea.android.nikolajk.gameengine;

/**
 * Created by Nikol_000 on 17-02-2016.
 */
public abstract class Screen
{
    protected final GameEngine gameEngine;

    public Screen(GameEngine gameEngine)
    {
        this.gameEngine = gameEngine;
    }

    public abstract void update(float deltaTime);
    public abstract void pause();
    public abstract void resume();
    public abstract void dispose();

}
