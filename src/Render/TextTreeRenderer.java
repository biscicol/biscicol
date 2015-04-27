package Render;

import BSCcore.BSCObject;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * @author stuckyb
 */
public class TextTreeRenderer extends TextRenderer {
    private int trackDepth = 0;
    String header = "";//"<div id=\"sidetree\">\n" +
    //"<div class=\"treeheader\">&nbsp;</div>\n" +
    //"<div id='sidetreecontrol'><a href='?#'>Collapse All</a> | <a href='?#'>Expand All</a></div>\n";
    String footer = "";//"</div>\n";

    public TextTreeRenderer() {
        super();
        setHeader(header);
        setFooter(footer);
    }

    public TextTreeRenderer(int maxdepth) {
        super(maxdepth);
        setHeader(header);
        setFooter(footer);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        String res = "";
        boolean hasChildren = node.hasChildren();
        String dlm = node.getDateLastModifiedString();
        // get the spaces to properly indent this element
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "\t";

        if (child_cnt == 0) {
            String ulTag = indent + "<ul";
            //if (depth == 0) {
            //    ulTag += " id=\"tree\"";
            //}
            ulTag += ">\n";
            res += ulTag;
            trackDepth = depth;
        }

        // output the element opening tag and properties
        if (!hasChildren) {
            res += indent + "<li class=\"jstree-leaf\">";
        } else {
            res += indent + "<li class=\"jstree-closed\">";
        }
        res += "<ins class=\"jstree-icon\">&nbsp;</ins>";
        res += "<a href=\"#\">";
        res += "<ins class=\"jstree-icon\">&nbsp;</ins>";
        res += "\"" + node + "\": ";
        res += node.getType();
        if (dlm != null) {
            res += " (" + dlm + ")";
        }
        res += "</a>";

        /*
        res += "<ul>";
        StmtIterator i = node.getProperties();
        while (i.hasNext()) {
            Statement stmt = (Statement) i.next();
            String predicate = stmt.getPredicate().toString();
            String object = stmt.getObject().toString();
            res += "<li>" + predicate + "\":\"" + object + "\"</li>";
        }
        res += "</ul>";
        */

        // print out </li> if there is only one, otherwise wait to do it later
        if (!hasChildren) {
            res += "</li>";
        }
        res += "\n";
        return res;
    }

    @Override
    protected String printLeaveObject(BSCObject node, int depth, int child_cnt) {
        String res = "";
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "\t";

        if (depth != trackDepth) {
            res += indent + "\t</ul>\n";
            res += indent + "</li>\n";
            trackDepth = depth;
        }

        return res;
    }
}