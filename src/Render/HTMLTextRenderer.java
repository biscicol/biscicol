package Render;

import BSCcore.BSCObject;
import org.joda.time.DateTime;
import util.BSCDate;


/**
 * @author stuckyb
 */
public class HTMLTextRenderer extends TextRenderer {
    private String header = "<div class=\"table\">\n";
    private String footer = "</div>\n";

    public HTMLTextRenderer() {
        super();
        setHeader(header);
        setFooter(footer);
    }

    public HTMLTextRenderer(int maxdepth) {
        super(maxdepth);
        setHeader(header);
        setFooter(footer);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        String output = "";
        DateTime objDLM = node.getDateLastModified();
        String dlm = BSCDate.formatDate(objDLM);

        // Don't print anything if filter is not met
        if (BSCDate.isDateBefore(objDLM, dateFilter)) {
            return "";
        }

        output += "<div class='row'>\n";
        output += "<div id=BSCObjDepth class='cell'>" + depth + "</div>\n";
        output += "<div id=BSCObjSubject class='cell'>" + node.toString() + "</div>\n";
        output += "<div id=BSCObjType class='cell'>" + node.getType() + "</div>\n";

        if (dlm != null)

        {
            output += "<div id=BSCObjDateLastModified class='cell'>" + dlm + "</div>\n";
        } else

        {
            output += "<div id=BSCObjDateLastModified class='cell'>&nbsp;</div>\n";
        }

        output += "</div>\n";
        return output;
    }
}