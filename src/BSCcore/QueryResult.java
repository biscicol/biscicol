/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BSCcore;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Iterator;

/**
 *
 * @author stuckyb
 */
public class QueryResult implements Iterable<QuerySolutionIter>, Iterator<QuerySolutionIter>
{
    private QueryExecution qe;
    private ResultSet rs;
    private PrefixMapping pm;
    private QuerySolution curr;

    public QueryResult(QueryExecution queryex, PrefixMapping prefixMap)
    {
        qe = queryex;
        rs = qe.execSelect();
        pm = prefixMap;
    }

    public Iterator<QuerySolutionIter> iterator()
    {
        return this;
    }

    public boolean hasNext()
    {
        boolean hasnext = rs.hasNext();

        // if there are no more results, close the QueryExecution to free up resources
        if (!hasnext)
            qe.close();

        return hasnext;
    }

    public QuerySolutionIter next()
    {
        return new QuerySolutionIter(rs.next(), pm);
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public String[] getVarNames()
    {
        return rs.getResultVars().toArray(new String[0]);
    }

    public int getRowNumber()
    {
        return rs.getRowNumber();
    }

    @Override
    public String toString()
    {
        return ResultSetFormatter.asText(rs);
    }
}
