package Render;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.BSCDate;
import BSCcore.*;

public class XSLRenderer implements ModelVisitor, StreamingOutput {

	static final String xslPath = Thread.currentThread().getContextClassLoader().getResource("xsl").getFile() + File.separator; // path to xsl folder
	static DocumentBuilder documentBuilder; // to build XML documents
	
	BSCObject subject = null;
	int maxDepth = -1;
	DateTime dateFilter = null;
	
	Document document = null; // XML representation of the model
	String xsl = null; // XSL stylesheet, null performs no transformation 
	
	private Element currentElement = null; // current XML element to append to when visiting
	
    /**
     * Create documentBuilder.
     */
	static {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    factory.setValidating(false);
	    factory.setIgnoringElementContentWhitespace(true);
	    factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
			"http://www.w3.org/2001/XMLSchema");
	    try {
			documentBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
    /**
     * Check if XSL stylesheet exists in xsl folder. 
     * 
     * @param xsl XSL stylesheet, '.xsl' extension is added.
     * @return true if XSL stylesheet exists, false otherwise.
     */
	public static boolean hasXsl(String xsl) {
		return new File(xslPath + xsl + ".xsl").exists();
	}
	
	/**
     * Blank instance, won't write/render anything.
     */
	public XSLRenderer() {}
	
	/**
     * Create XML representation of the model with given parameters.
     * XSL transformation will be performed only 
     * if XSL stylesheet is given in 'write' method.
     *
     * @param subject 
     * @param queryType 
     * @param dateFilter 
     * @param maxDepth 
     */
	public XSLRenderer(BSCObject subject, QueryType queryType, DateTime dateFilter, int maxDepth) {
		this(subject, queryType, dateFilter, maxDepth, null);
	}
	
    /**
     * Create XML representation of the model with given parameters.
     * If xsl is null, XML is rendered without transformation.
     *
     * @param subject 
     * @param queryType 
     * @param dateFilter 
     * @param maxDepth 
     * @param xsl XSL stylesheet, must be in xsl folder, '.xsl' extension is added.
     */
	public XSLRenderer(BSCObject subject, QueryType queryType, DateTime dateFilter, int maxDepth, String xsl) {
		this.xsl = xsl;
		this.maxDepth = maxDepth;
		this.dateFilter = dateFilter;

		document = documentBuilder.newDocument();

		// add root xml element with attributes
		Element root = document.createElement("biscicol");
	    root.setAttribute("queryType", queryType.toString());
	    if (dateFilter != null)
	    	root.setAttribute("dateFilter", BSCDate.formatDate(dateFilter));
	    root.setAttribute("maxDepth", ""+maxDepth);
		document.appendChild(root);
		currentElement = root;
		
        visitEnter(subject, 0, 0); // add subject node
        this.subject = subject; // this prevents subject from being added anymore
		
        if (QueryType.relations.equals(queryType)) {
            addRelation(QueryType.ancestors);
            addRelation(QueryType.descendents);
            addRelation(QueryType.siblings);
        } else
            addRelation(queryType);
        
        this.subject = null; // allow leaving of subject
        visitLeave(subject, 0, 0);

        this.subject = subject; // set subject, not really needed
	}
	
    void addRelation(QueryType relation) {
    	// add group xml element
        Element element = document.createElement("group");
		element.setAttribute("relation", relation.toString());
		currentElement.appendChild(element);
		currentElement = element;
		
		switch(relation) {
			case descendents: 
				subject.visitDescendents(this);
				break;
			case ancestors:
				subject.visitAncestors(this);
		        break;
			case siblings:
		        addList(subject.getSiblings());
		        break;		
		}
		goUp(); // set currentElement to parent of group (subject node)
    }

	public boolean visitEnter(BSCObject obj, int depth, int obj_cnt) {
		if (!obj.equals(subject)) {
			Element element = document.createElement("node");
			element.setAttribute("id", obj.toString());
			element.setAttribute("uri", obj.getURI());
			element.setAttribute("type", obj.getType());
			String dlm = obj.getDateLastModifiedString();
			if (dlm != null)
				element.setAttribute("dateModified", dlm);
			if (BSCDate.isDateBefore(obj.getDateLastModified(), dateFilter))
				element.setAttribute("expired", "");
			//if ("geo:SpatialThing".equals(obj.getType()))
			//	element.setAttribute("parent", obj.getParent().toString());
			currentElement.appendChild(element);
			currentElement = element;
		}
		return depth != maxDepth;
	}

	public void visitLeave(BSCObject obj, int depth, int obj_cnt) {
		if (!obj.equals(subject)) 
			goUp();
	}
	
    /**
     * Transform XML document using XSL stylesheet and write to output.
     * If xsl is null, XML is rendered without transformation.
     *
     * @param output Output to write to.
     * @param xsl XSL stylesheet, must be in xsl folder, '.xsl' extension is added.
     */
    public void write(OutputStream output, String xsl) {
        Source xmlSource = new DOMSource(document);
        Result result = new StreamResult(output);
		try {
			if (xsl == null || xsl.isEmpty())
				TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
			else
				{
				Source xslSource = new StreamSource(xslPath + xsl + ".xsl");
				TransformerFactory.newInstance().newTransformer(xslSource).transform(xmlSource, result);
				}
		} catch (TransformerException e) {
			e.printStackTrace(new PrintStream(output));
		}
    }
    
    /**
     * Transform XML document and write to output.
     * XSL transformation will be performed only 
     * if XSL stylesheet was given at instantiation.
     *
     * @param output Output to write to.
     */
    public void write(OutputStream output) {
		if (document != null)
			write(output, xsl);
    }
    
    void addList(BSCObjectIter objiter) {
        int child_num = 0;
        for (BSCObject obj : objiter) {
            visitEnter(obj, 0, child_num);
            visitLeave(obj, 0, child_num);
            child_num++;
        }
    }
    
    void goUp() {
		currentElement = (Element) currentElement.getParentNode();
    }

}
