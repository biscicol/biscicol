/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BSCcore;

import java.util.NoSuchElementException;
import java.util.HashSet;


/**
 * An iterator for all siblings of a BSCObject.  Siblings are defined as objects
 * that share at least one parent.  In the BiSciCol data model, sibling
 * relationships can be due to simple, explicit relationships (i.e., leadsTo) or
 * due to more complex sameAs relationships among parents.  In either case,
 * multiple pathways can lead to the same sibling object.  To account for this,
 * BSCObjSiblingsIter makes sure that all sibling objects are only returned once
 * via the next() method.
 */
public class BSCObjSiblingsIter extends BSCObjectIter
{
    private BSCObjBasicIter curr_ci;
    private BSCObjBasicIter p_iter;
    private boolean ready_for_fetch;
    private BSCObject sibling;
    
    // for keeping track of which objects we've already seen
    private HashSet<BSCObject> prevobjs;

    /**
     * Construct a new BSCObjSiblingsIter for the given BSCObject.
     * 
     * @param source The BSCObject for which to retrieve siblings.
     */
    public BSCObjSiblingsIter(BSCObject source)
    {
        curr_ci = null;
        p_iter = source.getParents();
        ready_for_fetch = false;
        
        // initialize the HashSet with the source object
        prevobjs = new HashSet<BSCObject>();
        prevobjs.add(source);

        // get the children iterator from the first parent
        if (p_iter.hasNext())
            curr_ci = p_iter.next().getChildren();
    }

    /**
     * See if there are sibling objects left to retrieve.
     * 
     * @return True if there are sibling objects left to retrieve.
     */
    public boolean hasNext()
    {
        if (curr_ci == null)
            return false;

        if (ready_for_fetch)
            // the last sibling is still waiting to be retrieved via next()
            return true;

        // Prefetch the next sibling object to make sure it isn't the
        // same as the original source object or a sibling we've already seen.
        do
        {
            sibling = advance();
        } while ((sibling != null) && prevobjs.contains(sibling));

        if (sibling == null)
            return false;
        else
        {
            ready_for_fetch = true;
            prevobjs.add(sibling);
            
            return true;
        }
    }

    private BSCObject advance()
    {
        BSCObject nextsib = null;

        // If there are no children left in curr_ci, retrieve the children
        // iterator from the next parent(s) until either curr_ci contains
        // objects or no parents are left.
        while (!curr_ci.hasNext() && p_iter.hasNext())
            curr_ci = p_iter.next().getChildren();

        if (curr_ci.hasNext())
            nextsib = curr_ci.next();

        return nextsib;
    }

    /**
     * Get the next sibling BSCObject from this iterator.
     * 
     * @return The next sibling BSCObject.
     */
    public BSCObject next()
    {
        if (ready_for_fetch)
        {
            // the next sibling is waiting to be returned, so just return it
            ready_for_fetch = false;
            return sibling;
        }
        else if (hasNext())
        {
            // make sure there is a next sibling; if so, return it
            ready_for_fetch = false;
            return sibling;
        }
        else
            throw new NoSuchElementException();
    }

    /**
     * The traversal depth relative to the starting object.  Always 0 for
     * BSCObjSiblingsIter.
     * 
     * @return Always returns 0.
     */
    @Override
    public int getDepth() {
        return 0;
    }
}
