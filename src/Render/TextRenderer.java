package Render;

import BSCcore.BSCObjSiblingsIter;
import BSCcore.BSCObject;
import BSCcore.BSCObjectIter;
import BSCcore.ModelVisitor;
import org.joda.time.DateTime;


/**
 * A superclass for rendering BiSciCol data into text-based formats.  This
 * class implements functionality common to all text rendering.  Subclasses
 * supporting specific text formats (e.g., XML, JSON, etc.) will need to provide
 * implementations of printEnterObject() and printLeaveObject().
 */
public class TextRenderer implements ModelVisitor {
    private int maxdepth;
    private int totalvisited;
    protected String outputstr = "";
    private String header = "";
    private String footer = "";
    protected DateTime dateFilter = null;
    protected boolean ancestors = false;
    protected boolean descendents = false;

    public DateTime getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(DateTime dateFilter) {
        this.dateFilter = dateFilter;
    }


    public TextRenderer() {
        // by default, do not limit the traversal depth
        this(-1);
    }

    public TextRenderer(int maxdepth) {
        this.maxdepth = maxdepth;
        totalvisited = 0;
        outputstr = "";
        // this.date = date;
    }

    protected void setHeader(String header) {
        this.header = header;
    }

    protected void setFooter(String footer) {
        this.footer = footer;
    }

    public int getMaxDepth() {
        return maxdepth;
    }

    public void setMaxDepth(int maxdepth) {
        this.maxdepth = maxdepth;
    }

    /**
     * Returns the total number of objects visited in a traversal of an object's
     * ancestors or descendents.
     *
     * @return The total number of objects visited during the traversal.
     */
    public int getTotalVisited() {
        return totalvisited;
    }

    public boolean visitEnter(BSCObject obj, int depth, int obj_cnt) {
        // keep track of how many total objects we've visited
        totalvisited++;

        outputstr += printEnterObject(obj, depth, obj_cnt);


        if (depth == maxdepth)
            return false;
        else
            return true;
    }

    public void visitLeave(BSCObject obj, int depth, int obj_cnt) {
        outputstr += printLeaveObject(obj, depth, obj_cnt);
    }

    public String printStats() {
        String ret = "";
        ret += "<br>Objects Visited = " + totalvisited;
        ret += "<br>Date Filter = " + dateFilter;
        ret += "<br>MaxDepth = " + maxdepth;
        return ret;
    }

    /**
     * Generates a textual representation of a BSCObject.  All descendents of
     * the object are included up to the depth specified by maxdepth (as set by
     * the constructor or setMaxDepth()).
     *
     * @param obj The BSCObject to render as text.
     * @return A string containing the specified object rendered as text.
     */
    public String renderObject(BSCObject obj) {
        descendents = true;
        outputstr = header;
        obj.visitDescendents(this);
        outputstr += footer;

        if (totalvisited < 1) {
            return "";
        } else {
            return outputstr;
        }
    }

    /**
     * Generates a textual representation of a BSCObject and its ancestors
     * (e.g., its parents, the parents' parents, etc.).  All ancestors of
     * the object are included up to the depth specified by maxdepth (as set by
     * the constructor or setMaxDepth()).
     *
     * @param obj The BSCObject to render as text (along with its ancestors).
     * @return A string containing the specified object rendered as text.
     */
    public String renderObjAncestors(BSCObject obj) {
        ancestors = true;
        outputstr = header;
        obj.visitAncestors(this);
        outputstr += footer;

        if (totalvisited < 1) {
            return "";
        } else {
            return outputstr;
        }
    }

    /**
     * Render all ancestors, descendents...
     * This method is useful for finding things like available geo coordinates to map
     *
     * @param obj
     * @return
     */
    public String renderAll(BSCObject obj) {
        //BSCObjSiblingsIter siter = obj.getSiblings();
        outputstr = header;
        //String siblings = renderObjectList(siter);
        ancestors = true;
        obj.visitAncestors(this);
        ancestors = false;
        descendents = true;
        obj.visitDescendents(this);

        // outputstr += siblings;
        outputstr += footer;
        return outputstr;
    }

    /**
     * Generates a textual respresentation of a list of BSCObjects accessed by
     * objiter.
     *
     * @param objiter An iterator for the BSCObjects.
     * @return A string containing the objects rendered as text.
     */
    public String renderObjectList(BSCObjectIter objiter) {
        String res = "";
        int child_num = 0;

        for (BSCObject obj : objiter) {
            res += printEnterObject(obj, 0, child_num);
            res += printLeaveObject(obj, 0, child_num);

            child_num++;
        }

        return res;
    }

    /**
     * Generates text for the "opening" of a BSCObject.  This method is intended
     * to be overridden by child classes of TextRenderer.
     *
     * @param obj     The object to render.
     * @param depth   The depth of obj in the traversal (0 = top).
     * @param obj_cnt The position of obj among its siblings (0 = 1st).
     * @return A textual respresentation of the "opening" of obj.
     */
    protected String printEnterObject(BSCObject obj, int depth, int obj_cnt) {
        return "";
    }

    /**
     * Generates text for the "closing" of a BSCObject.  This method is intended
     * to be overridden by child classes of TextRenderer.
     *
     * @param obj     The object to render.
     * @param depth   The depth of obj in the traversal (0 = top).
     * @param obj_cnt The position of obj among its siblings (0 = 1st).
     * @return A textual respresentation of the "closing" of obj.
     */
    protected String printLeaveObject(BSCObject obj, int depth, int obj_cnt) {
        return "";
    }
}
