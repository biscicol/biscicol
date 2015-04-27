package Render;

import BSCcore.BSCObject;
import org.joda.time.DateTime;

/**
 * @author JDeck
 */
public class JUnitRenderer extends TextRenderer {
    public JUnitRenderer() {
        super();
    }

    public JUnitRenderer(int maxdepth, DateTime date) {
        super(maxdepth);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        // output the element opening tag and properties
        return  "{" + node;
    }

    @Override
    protected String printLeaveObject(BSCObject node, int depth, int child_cnt) {
        // output the closing tag
       return "}";
    }
}

