/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BSCcore;

import java.util.Iterator;

/**
 *
 * @author stuckyb
 */
public abstract class BSCObjectIter implements Iterable<BSCObject>, Iterator<BSCObject>
{
    public Iterator<BSCObject> iterator()
    {
        return this;
    }

    abstract public boolean hasNext();

    abstract public BSCObject next();

    abstract public int getDepth();

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
