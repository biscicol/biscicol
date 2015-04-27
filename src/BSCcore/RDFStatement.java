/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BSCcore;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author stuckyb
 */
public class RDFStatement {
    private Statement stmt;
    private PrefixMapping pm;

    public RDFStatement(Statement statement, PrefixMapping prefixMap) {
        stmt = statement;
        pm = prefixMap;
    }


    public BSCObject getSubject() {
        return new BSCObject(stmt.getSubject(), pm);
    }

    public String getSubjectString() {
        return pm.shortForm(stmt.getSubject().toString());
    }

    public String getPredicate() {
        return pm.shortForm(stmt.getPredicate().toString());
    }

    public BSCObject getObject() {
        return new BSCObject(stmt.getObject(), pm);
    }

    public String getObjectString() {
        return pm.shortForm(stmt.getObject().toString());
    }
    
    @Override
    public String toString() {
        return getSubject() + " " + getPredicate() + " " + getObject();
    }
}
