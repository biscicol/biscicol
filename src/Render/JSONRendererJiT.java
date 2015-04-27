package Render;

import BSCcore.BSCObject;
import BSCcore.SharedProperties;
import BSCcore.SharedPropertiesFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.joda.time.DateTime;
import util.BSCDate;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.xml.soap.Node;
import java.util.Iterator;


/**
 * @author JDeck/Stuckyb
 */
public class JSONRendererJiT extends TextRenderer {
    //private String header = "[";
    //private String footer = "] ";
    private String header = "";
    private String footer = "";
    //private String footer = ", {\"stats\" : \"Application Stats not yet coded\"}]";
    //SharedProperties props;
    boolean related_to = false;

    public JSONRendererJiT() {
        super();
        setHeader(header);
        setFooter(footer);
    }

    public JSONRendererJiT(int maxdepth, DateTime date) {
        super(maxdepth);
        setHeader(header);
        setFooter(footer);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        StringBuilder res = new StringBuilder();
        SharedProperties props = node.getProps();

        // get the spaces to properly indent this element
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "    ";

        // if this is not the first object at this depth, output a comma
        //if (!related_to) {
            if (child_cnt != 0)
                res.append(",");
            if (depth != 0)
                res.append("\n");
       // }
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

        // Construct child-node section --- TEST if this is related_to in graph parsing
        StringBuilder childNode = new StringBuilder();
        //related_to = false;
        // output the element opening tag and properties
        childNode.append(indent + "  {\"id\":\"" + href + "\",");
        childNode.append("\"name\": \"" + href + "\",");
        childNode.append("\"data\": {");

        // List the properties associated with this object
        Iterator i = node.getAttributeProperties();
        while (i.hasNext()) {
            Statement stmt = (Statement) i.next();
            Property predicate = stmt.getPredicate();
            String object = stmt.getObject().toString();
            // Declare the relation type for Jit graph

            if (predicate.equals(props.getRelatedTo()) ||
                    predicate.equals(props.getDependsOn()) ||
                    predicate.equals(props.getDerivesFrom()) ||
                    predicate.equals(props.getAlias_of())) {
                if (predicate.equals(props.getRelatedTo())) {
                    //related_to = true;
                }
                childNode.append("\"" + "relation" + "\":\"" + predicate.getLocalName() + "\"");
            } else {
                childNode.append("\"" + predicate.getLocalName() + "\":\"" + object + "\"");
            }
            if (i.hasNext())
                childNode.append(",");
        }

        childNode.append("},\n");

        // Determine if this node has children or not
        childNode.append(indent + "  \"children\": [");

        //if (!related_to) {
            res.append(childNode);
        //}

        return res.toString();
    }

    @Override
    protected String printLeaveObject(BSCObject node, int depth, int child_cnt) {
        StringBuilder res = new StringBuilder();

       // if (!related_to) {
            // get the spaces to properly indent this element
            String indent = "";
            for (int cnt = 0; cnt < depth; cnt++)
                indent += "    ";

            // close the children list
            res.append(" ]\n");

            // output the closing tag
            res.append(indent + "}");

            // if this is the end, output an additional line break
            if (depth == 0)
                res.append("\n");
       // }
        return res.toString();
    }
}
