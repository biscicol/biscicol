package Render;

import BSCcore.BSCObjBasicIter;
import BSCcore.BSCObject;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.joda.time.DateTime;
import util.BSCDate;


/**
 * @author JDeck/Stuckyb
 */
public class JSONRenderer extends TextRenderer {
    private String header = "[";
    //private String footer = "] ";
    private String footer = ", {\"stats\" : \"Application Stats not yet coded\"}]";

    public JSONRenderer() {
        super();
        setHeader(header);
        setFooter(footer);
    }

    public JSONRenderer(int maxdepth, DateTime date) {
        super(maxdepth);
        setHeader(header);
        setFooter(footer);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        String res = "";

        // get the spaces to properly indent this element
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "    ";

        // if this is not the first object at this depth, output a comma
        if (child_cnt != 0)
            res += ",";
        if (depth != 0)
            res += "\n";
        String type = node.getType();
        DateTime objDLM = node.getDateLastModified();
        String dlm = BSCDate.formatDate(objDLM);
        String title = node.toString();
        title += " (";
        if (type != null)
            title += type;
        if (dlm != null) {
            if (type != null)
                title += "; ";
            title += dlm;
        }
        // TODO: Integrate the following better into jstree
        if (BSCDate.isDateBefore(objDLM, dateFilter)) {
            title += " TODO:GrayRow";
        }
        title += ")";
        String href = node.getURI();

        // output the element opening tag and properties
        res += indent + "{\"data\" : \"" + title + "\", \n";
        //res += indent + "  \"attr\" : {\"href\": \"" + href + "\"},\n";
        res += indent + "  \"attr\" : {\"href\": \"" + href + "\"";

        // Output a list of attributes for this object
        /*
        StmtIterator i = node.getProperties();
        while (i.hasNext()) {
            Statement stmt = (Statement) i.next();
            String predicate = stmt.getPredicate().toString();
            String object = stmt.getObject().toString();
            res += ",\"" + predicate + "\":\"" + object + "\"";
        }
        */

        res += indent + "},\n";

        // the jquery.jstree examples have metadata included and  this may be useful,
        // but for now i don't see how it works or if its necessary for us so leaving it out
        //res += indent + "  \"metadata\": {\"id\" : " + this.getTotalVisited() + "},\n";

        // Open root node to begin with
        if (depth == 0) {
            res += indent + "  \"state\": \"open\",\n";
        }

        // Determine if this node has children or not
        res += indent + "  \"children\": [";

        return res;
    }

    @Override
    protected String printLeaveObject(BSCObject node, int depth, int child_cnt) {
        String res = "";

        // get the spaces to properly indent this element
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "    ";

        // close the children list
        res += " ]\n";

        // output the closing tag
        res += indent + "}";

        // if this is the end, output an additional line break
        if (depth == 0)
            res += "\n";

        return res;
    }
}
