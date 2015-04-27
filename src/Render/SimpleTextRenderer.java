

package Render;

import BSCcore.BSCObject;


/**
 *
 * @author stuckyb
 */
public class SimpleTextRenderer extends TextRenderer
{    
    public SimpleTextRenderer()
    {
        super();
    }
    
    public SimpleTextRenderer(int maxdepth)
    {
        super(maxdepth);
    }
    
    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt)
    {

        String res;

        // get the spaces to properly indent this element
        String indent = "";
        for (int cnt = 0; cnt < depth; cnt++)
            indent += "   |";

        // output the element opening tag and properties
        res = indent + '"' + node + "\": ";
        res += node.getType() + ", " + node.getDateLastModified() + "\n";

        return res;
    }


}
