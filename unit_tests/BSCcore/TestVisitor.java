
package BSCcore;


/**
 * A class that implements the ModelVisitor interface, intended for use
 * in writing/running unit tests for the BSCCore code.  The general approach
 * is to keep a text list of all visited objects, in the order they are visited,
 * in visitstr.  The string is designed to test both visitEnter and visitLeave,
 * as well as the obj and obj_cnt parameters of those methods.  Thus, it builds
 * a string that represents nesting with parentheses and uses commas to separate
 * child lists.  This string can then be checked against the expected outcome.
 */
public class TestVisitor implements ModelVisitor {
    private int maxdepth;
    private int totalvisited;
    protected String visitstr = "";
    protected boolean ancestors = false;
    protected boolean descendents = false;

    public TestVisitor() {
        // by default, do not limit the traversal depth
        this(-1);
    }

    public TestVisitor(int maxdepth) {
        this.maxdepth = maxdepth;
        totalvisited = 0;
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

    public String getVisitStr() {
        return visitstr;
    }

    public boolean visitEnter(BSCObject obj, int depth, int obj_cnt) {
        // keep track of how many total objects we've visited
        totalvisited++;

        if (obj_cnt > 0)
            visitstr += ",";
        
        visitstr += obj.toString();

        if (obj.hasChildren())
            visitstr += "(";

        if (depth == maxdepth)
            return false;
        else
            return true;
    }

    public void visitLeave(BSCObject obj, int depth, int obj_cnt) {
        if (obj.hasChildren())
            visitstr += ")";
    }
}
