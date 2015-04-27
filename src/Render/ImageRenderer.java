

package Render;


import BSCcore.BSCObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayDeque;


public class ImageRenderer
{
    private int maxdepth;

    // default values for the width and height of the generated image
    private int width = 800;
    private int height = 440;

    private int cellwidth, cellheight;
    private Graphics2D g2;
    private BufferedImage img;

    // the following variables control the layout of the image
    private int fheight;
    private int marginvert = 10;
    private int marginhoriz = 10;
    private int nmarginhoriz = 4;
    private int nmarginvert = 6;
    private int nlinespace = 2;
    private int nodespace = 5;

    // these variables keep track of the size of the tree to be rendered
    private int mdepth, mwidth;

    // tracks the top to bottom position in the tree
    private int curxpos;

    public ImageRenderer()
    {
        // by default, do not limit the traversal depth
        this(-1);
    }

    public ImageRenderer(int maxdepth)
    {
        this.maxdepth = maxdepth;
    }

    public int getMaxDepth()
    {
        return maxdepth;
    }

    public void setMaxDepth(int maxdepth)
    {
        this.maxdepth = maxdepth;
    }

    public void renderObject(BSCObject obj)
    {
        //System.out.println(start);

        mwidth = mdepth = 0;
        mwidth = getWidthAndHeight(obj, 0);
        if (mwidth == 1)
            mwidth = 2;
        //System.out.println("total object depth: " + mdepth);
        //System.out.println("total object width: " + mwidth);

        img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        g2 = (Graphics2D)img.getGraphics();

        FontRenderContext frcontext = g2.getFontRenderContext();
        Font font = g2.getFont();
        // calculate the font height
        Rectangle2D strbounds = font.getStringBounds("A", frcontext);
        fheight = (int)strbounds.getHeight();

        height = marginvert*2 + (fheight*2 + nmarginvert*2 + nlinespace + nodespace) * (mwidth);
        width = 160*(mdepth+1) + marginhoriz*2;

        cellwidth = (width-marginhoriz*2-nmarginhoriz*2) / (mdepth+1);
        cellheight = (height-fheight*2-nlinespace-marginvert*2-nmarginvert*2) / (mwidth-1);

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2 = (Graphics2D)img.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setBackground(new Color(255, 255, 255));
        g2.clearRect(0, 0, width, height);

        curxpos = 0;
        drawObject(obj, 0);
    }

    public void outputAsPNG()
    {
        try {
            ImageIO.write(img, "png", new File("test.png"));
        } catch(Exception e) { System.out.println(e); }
    }

    public void outputToOS(OutputStream os)
    {
        try {
            ImageIO.write(img, "png", os);
        } catch(Exception e) { System.out.println(e); }
    }

    private int drawObject(BSCObject obj, int depth)
    {
        int numchildren = 0;
        int childrendrawn = 0;
        int parentx = curxpos;
        int childx;
        ArrayDeque<Integer> childnodes = new ArrayDeque<Integer>(10);

        for (BSCObject child : obj.getChildren())
        {
            numchildren++;
        }
        //System.out.println("number of children: "+numchildren);

        if (depth != maxdepth)
        {
            for (BSCObject child : obj.getChildren())
            {
                // recursively process each child that has children as long as it
                // won't exceed the maximum traversal depth
                if (child.hasChildren() && (depth != (maxdepth - 1)))
                {
                    childx = drawObject(child, depth + 1);
                    childnodes.push(new Integer(childx));
                } else
                {
                    drawNode(curxpos, depth + 1, child.toString(), "");
                    childnodes.push(new Integer(curxpos));
                    curxpos++;
                }

                childrendrawn++;

                // determine location of this (the parent) node
                if (((numchildren % 2) != 0)
                        && (childrendrawn == (numchildren / 2 + 1)))
                {
                    parentx = curxpos - 1;
                } else if (((numchildren % 2) == 0)
                        && childrendrawn == (numchildren / 2))
                {
                    parentx = curxpos;
                    curxpos++;
                }
            }
        }

        drawNode(parentx, depth, obj.toString(), "");
        while (!childnodes.isEmpty())
        {
            drawConnector(parentx, depth, childnodes.pop().intValue(), depth + 1, obj.toString());
        }

        return parentx;
    }

    private void drawNode(int modx, int mody, String val, String predval)
    {
        //System.out.println("model x: " + modx + "; modely: " + mody + "; value: " + val);

        // get font metrics
        FontRenderContext frcontext = g2.getFontRenderContext();
        Font font = g2.getFont();
        int valtxtwidth = (int)font.getStringBounds(val, frcontext).getWidth();
        int fascent = -(int)font.getStringBounds(val, frcontext).getY();
        int pvaltxtwidth = (int)font.getStringBounds(predval, frcontext).getWidth();
        int txtwidth = (valtxtwidth > pvaltxtwidth) ? valtxtwidth : pvaltxtwidth;

        int startx = mody*cellwidth + marginhoriz;
        int starty = modx*cellheight + marginvert;
        int nodew = txtwidth + nmarginhoriz*2;
        int nodeh = fheight*2 + nmarginvert*2 + nlinespace;

        // draw the node rectangle, border, and shadow
        g2.setColor(new Color(140, 140, 140));
        g2.fillRoundRect(startx-2, starty+3, nodew, nodeh, 8, 8);
        g2.setColor(new Color(200, 200, 200));
        g2.fillRoundRect(startx, starty, nodew, nodeh, 8, 8);
        g2.setColor(new Color(0, 0, 0));
        g2.drawRoundRect(startx, starty, nodew, nodeh, 8, 8);

        // draw the node text
        g2.setColor(new Color(200, 0, 0));
        g2.drawString(predval, startx+nmarginhoriz, starty+fascent+nmarginvert);
        g2.setColor(new Color(0, 0, 0));
        if (predval.equals(""))
            g2.drawString(val, startx+nmarginhoriz, starty+(fheight*2+nmarginvert*2+nlinespace+fascent)/2);
        else
            g2.drawString(val, startx+nmarginhoriz, starty+fheight+fascent+nmarginvert+nlinespace);
    }

    private void drawConnector(int pmodx, int pmody, int cmodx, int cmody, String pval)
    {
        FontRenderContext frcontext = g2.getFontRenderContext();
        Font font = g2.getFont();
        // calculate the width of the node text
        Rectangle2D strbounds = font.getStringBounds(pval, frcontext);
        int txtwidth = (int)strbounds.getWidth();

        int nodeh = fheight*2 + nmarginvert*2 + nlinespace;

        int startx = pmody*cellwidth + marginhoriz + nmarginhoriz*2 + txtwidth;
        int starty = pmodx*cellheight + marginvert + nodeh/2;
        int endx = cmody*cellwidth + marginhoriz;
        int endy = cmodx*cellheight + marginvert + nodeh/2;

        g2.setColor(new Color(0, 0, 0));
        g2.drawLine(startx, starty, endx, endy);
    }

    private int getWidthAndHeight(BSCObject obj, int depth)
    {
        int nodewidth = 1;
        int numchildren = 0;

        // determine the maximum depth reached
        if (depth > mdepth)
            mdepth = depth;

        if (depth != maxdepth)
        {
            for (BSCObject child : obj.getChildren())
            {
                numchildren++;

                // recursively process this child
                nodewidth += getWidthAndHeight(child, depth + 1);
            }
        }

        if ((numchildren % 2) != 0)
            nodewidth -= 1;
        //System.out.println("node width: " + nodewidth);
        return nodewidth;
    }
}
