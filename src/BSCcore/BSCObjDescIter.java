/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BSCcore;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.LinkedList;

import java.util.NoSuchElementException;

/**
 * @author stuckyb
 */
public class BSCObjDescIter extends BSCObjectIter
{
    private BSCObjBasicIter curr_ci;
    private BSCObject nextobj;
    private PrefixMapping pm;
    private int maxdepth, currdepth;
    private LinkedList<BSCObject> objqueue;
    private boolean ready_for_fetch;

    // this constructor makes BSCObjBasicIter act as a null iterator
    public BSCObjDescIter()
    {
        curr_ci = null;
    }

    public BSCObjDescIter(BSCObjBasicIter child_iter, int depth, PrefixMapping prefixmap)
    {
        pm = prefixmap;
        curr_ci = child_iter;
        maxdepth = depth;
        currdepth = 1;

        objqueue = new LinkedList<BSCObject>();

        // use null BSCObjects to keep track of the current traversal depth
        objqueue.offer(null);
    }

    public int getDepth()
    {
        return currdepth;
    }

    public boolean hasNext()
    {
        if (curr_ci == null)
            return false;

        if (ready_for_fetch)
            // the last descendant is still waiting to be retrieved via next()
            return true;

        // Prefetch the next descendant to make sure we actually have one.
        nextobj = advance();

        if (nextobj == null)
            return false;
        else
        {
            ready_for_fetch = true;
            return true;
        }
    }

    private BSCObject advance()
    {
        BSCObject obj = null;

        // if the current child iterator has no objects in it, move to the next
        // object in the queue until either the child iterator has objects or
        // the queue is empty
        while (!curr_ci.hasNext() && (objqueue.size() != 1))
        {
            // see if we've finishd processing all descendants at the current depth
            if (objqueue.peek() == null)
            {
                // we're moving to the next level in the tree
                currdepth++;

                // move the null pointer to the end of the queue
                objqueue.offer(null);
                objqueue.remove();
            }

            curr_ci = objqueue.remove().getChildren();
        }

        if (curr_ci.hasNext() && (currdepth <= maxdepth))
        {
            obj = curr_ci.next();

            // add this object to the queue so we can process its children later
            objqueue.offer(obj);
        }

        return obj;
    }

    public BSCObject next()
    {
        if (ready_for_fetch)
        {
            // the next sibling is waiting to be returned, so just return it
            ready_for_fetch = false;
            return nextobj;
        }
        else if (hasNext())
        {
            // make sure there is a next sibling; if so, return it
            ready_for_fetch = false;
            return nextobj;
        }
        else
            throw new NoSuchElementException();
    }
}
