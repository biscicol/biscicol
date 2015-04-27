


package BSCcore;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.NoSuchElementException;

/**
 * A basic iterator for a list of BSCObjects.  It is used by several methods in
 * BSCObject, including getChildren() and getParents().  This iterator is built
 * from the Jena StmtIterator and can return either the subjects or objects of
 * the triples in a Jena StmtIterator as BSCObjects.
 */
public class BSCObjBasicIter extends BSCObjectIter {
    private StmtIterator si;
    private PrefixMapping pm;
    private boolean return_subjects;

    /**
     * An empty constructor that makes the BSCObjBasicIter act as a null
     * iterator.
     */
    public BSCObjBasicIter() {
        si = null;
    }

    /**
     * Constructs a new iterator that will return the objects of the statements
     * in stmtiter.
     * 
     * @param stmtiter An iterator for a list of Jena model statements.
     * @param prefixmap A prefix mapping to use for constructing BSCObjects.
     */
    public BSCObjBasicIter(StmtIterator stmtiter, PrefixMapping prefixmap) {
        this(stmtiter, prefixmap, false);
    }

    /**
     * Constructs a new iterator that will return either the subjects or the
     * objects of the statements in stmtiter, depending on the value of
     * return_subjects.
     * 
     * @param stmtiter An iterator for a list of Jena model statements.
     * @param prefixmap A prefix mapping to use for constructing BSCObjects.
     * @param return_subjects If true, return the subjects of the Jena
     * statements rather than the objects.
     */
    public BSCObjBasicIter(StmtIterator stmtiter, PrefixMapping prefixmap, boolean return_subjects) {
        this.return_subjects = return_subjects;
        pm = prefixmap;
        si = stmtiter;
    }
    
    /**
     * Find out if this BSCObjBasicIter is returning the subjects or objects
     * of Jena statements.
     * 
     * @return True if the subjects of Jena statements will be returned.
     */
    public boolean getReturnSubjects() {
        return return_subjects;
    }

    /**
     * Specify whether the subjects or objects of Jena statements should be
     * returned.
     * 
     * @param return_subjects If true, then return the subjects of Jena
     * statements.
     */
    public void setReturnSubjects(boolean return_subjects) {
        this.return_subjects = return_subjects;
    }

    /**
     * See if there are BSCObjects left to retrieve.
     * 
     * @return True if there are more BSCObjects to retrieve.
     */
    public boolean hasNext() {
        if (si == null)
            return false;

        boolean hasnext = si.hasNext();

        // close the StmtIterator to free up resources if there are no items left
        if (!hasnext)
            si.close();

        return si.hasNext();
    }

    /**
     * Get the next BSCObject from this iterator.
     * 
     * @return The next BSCObject.
     */
    public BSCObject next() {
        Statement stmt;

        if (si == null)
            throw new NoSuchElementException();

        // get next statement and return the object of the statemnt
        stmt = si.nextStatement();

        if (return_subjects)
            return new BSCObject(stmt.getSubject(), pm);
        else
            return new BSCObject(stmt.getObject(), pm);

    }

    /**
     * The traversal depth relative to the starting object.  Always 1 for
     * BSCObjBasicIter.
     * 
     * @return Always returns 1.
     */
    @Override
    public int getDepth() {
        return 1;
    }
}
