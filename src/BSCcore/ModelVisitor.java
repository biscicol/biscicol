

package BSCcore;


/**
 * This interface defines the methods that a class must implement to be able
 * to use the visitor methods in BSCObject (e.g., visitDescendents()).
 */
public interface ModelVisitor
{
    /**
     * Notify the visitor that it is entering obj.
     *
     * @param obj The BSCObject this visitor is entering.
     * @param depth The distance of obj from the BSCObject at which the traversal began.
     * @param obj_cnt The position of obj among its siblings.
     * @return Whether or not to continue the traversal at the next level (e.g.,
     * depth + 1) beyond obj.
     */
    public boolean visitEnter(BSCObject obj, int depth, int obj_cnt);

    /**
     * Notify the visitor that it is leaving obj.
     *
     * @param obj The BSCObject this visitor is leaving.
     * @param depth The distance of obj from the BSCObject at which the traversal began.
     * @param obj_cnt The position of obj among its siblings.
     */
    public void visitLeave(BSCObject obj, int depth, int obj_cnt);
}
