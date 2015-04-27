/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BSCcore;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Iterator;

/**
 *
 * @author stuckyb
 */
public class RDFStatementIter implements Iterable<RDFStatement>, Iterator<RDFStatement>
{
    private StmtIterator si;
    private PrefixMapping pm;

    public RDFStatementIter(StmtIterator stmtiter, PrefixMapping prefixMap)
    {
        si = stmtiter;
        pm = prefixMap;
    }

    public Iterator<RDFStatement> iterator()
    {
        return this;
    }

    public boolean hasNext()
    {
        boolean hasnext = si.hasNext();

        // if there are no more results, close the QueryExecution to free up resources
        if (!hasnext)
            si.close();

        return hasnext;
    }

    public RDFStatement next()
    {
        return new RDFStatement(si.next(), pm);
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
