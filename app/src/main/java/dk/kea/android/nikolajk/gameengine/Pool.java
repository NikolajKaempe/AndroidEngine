package dk.kea.android.nikolajk.gameengine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikol_000 on 01-03-2016.
 */
public abstract class Pool<T>
{
    private List<T> items = new ArrayList<>();
    protected abstract T newItem();

    public T obtain()
    {
        int last = items.size() -1;
        if(last == -1)return newItem();
        return items.remove(last);
    }

    public void free(T item) //returns the item to the pool
    {
        items.add(item);
    }
}
