/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BSCcore;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Iterator;

/**
 *
 * @author stuckyb
 */
public class QuerySolutionIter extends BSCObjectIter
{
    private QuerySolution qs;
    private PrefixMapping pm;
    private Iterator<String> varnames;

    public QuerySolutionIter(QuerySolution querysolution, PrefixMapping prefixMap)
    {
        qs = querysolution;
        pm = prefixMap;
        varnames = qs.varNames();
    }

    public boolean hasNext()
    {
        return varnames.hasNext();
    }

    public BSCObject next()
    {
        return new BSCObject(qs.get(varnames.next()), pm);
    }

    @Override
    public int getDepth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCObject get(String key)
    {
        return new BSCObject(qs.get(key), pm);
    }
}
