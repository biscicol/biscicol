package BSCcore;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import org.joda.time.DateTime;
import sun.font.StrikeMetrics;
import util.BSCDate;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * BSCObject implements a single data "object" in the BiSciCol system (e.g.,
 * specimen, tissue sample, collecting event, etc.).  Each object has several
 * properties associated with it, such as date last modified and type (implemented
 * as RDF literal values), and each object is connected to 0 or more other objects
 * in the underlying model.  BSCObjects are aware of these relationships and
 * can allow traversal of their siblings, ancestors, or descendents.
 */
public class BSCObject {
    private RDFNode node;
    private PrefixMapping pm;
    private SharedProperties props;

    /**
     * Construct a new BSCObject from a Jena RDFNode object.
     *
     * @param node      The underlying Jena RDFNode that holds the data for this BSCObject.
     * @param prefixmap The prefix mapping to use when building short-form strings.
     */
    public BSCObject(RDFNode node, PrefixMapping prefixmap) {
        this.node = node;
        pm = prefixmap;
        props = SharedPropertiesFactory.getSharedProperties(node.getModel());
    }

    /**
     * Tell us if this is object is a literal in the underlying RDF graph.  Note
     * that this generally should not happen unless a literal object is accessed
     * directly in the BSCModel.
     *
     * @return True if this object is a literal, false otherwise.
     */
    public boolean isLiteral() {
        return node.isLiteral();
    }

    /**
     * Tell us what the rdf:type is of this object.
     *
     * @return A string with the literal rdf:type of this object.
     */
    public String getType() {

        Statement s = node.asResource().getProperty(props.getType());

        if (s == null) {
            return "owl:Thing";
        } else {
            RDFStatement rs = new RDFStatement(s, pm);
            return pm.shortForm(rs.getObjectString());
        }
    }


    /**
     * Tell us what the dwc:DateLastModified is of this object.
     *
     * @return A joda DateTime object containing the parsed value of the literal
     *         dwc:DateLastModified for this object.
     */
    public DateTime getDateLastModified() {
        if (node.isLiteral()) {
            return null;
        }
        try {
            Statement s = node.asResource().getProperty(props.getDateLastModified());
            DateTime dt = BSCDate.parseDate(s.getLiteral().toString());
            return dt;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * A convenience method that returns the value of dwc:DateLastModified for
     * this object as a string instead of a DateTime object.  It is essentially
     * equivalent to object.getDateLastModified().toString(format), where format
     * is yyyy-MM-dd.
     *
     * @return A string containing the value of the literal dwc:DateLastModified
     *         for this object in yyyy-MM-dd format.
     */
    public String getDateLastModifiedString() {
        return BSCDate.formatDate(getDateLastModified());
    }

    /**
     * Assuming you already know the DateLastModified
     * @param dt
     * @return
     */
//    public String getDateLastModifiedString(DateTime dt) {
//        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
//        if (dt != null) {
//            return dt.toString(fmt);
//        } else {
//            return null;
//        }
//    }

    /**
     * Get the geographic location of this object. This is a relation in our
     * Model, not really the same as DateLastModified
     * since for now we're treating Geo as URN.  For now, this method is
     * unimplemented. It may be that this method doesn't make sense.
     *
     * @return For now, just returns null.
     */
    public String getGeo() {
        return null;
    }

    /**
     * Gets the full URI for this object.
     *
     * @return A string containing the URI for this object.
     */
    public String getURI() {
        return node.asResource().getURI();
    }

    /**
     * Test if two BSCObjects are logically the same.  If both objects resolve
     * to the same URI, they are considered equal.
     *
     * @param obj The object to test for equality.
     * @return True if the objects are equivalent, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        // make sure the test object is of the correct type
        if (!(obj instanceof BSCObject))
            return false;

        BSCObject testobj = (BSCObject) obj;

        if (isLiteral()) {
            // test if the second object is also literal with the same contents
            return testobj.isLiteral() && getURI().equals(testobj.getURI());
        } else {
            // test if the second object is also a resource with the same contents
            return !testobj.isLiteral() && getURI().equals(testobj.getURI());
        }
    }

    /**
     * Return an integer hash value for this BSCObject.  Values returned will
     * be semantically correct with regard to the equals() function.  That is,
     * equivalent objects will have equivalent hash values.
     *
     * @return An integer hash value for this object.
     */
    @Override
    public int hashCode() {
        return getURI().hashCode();
    }

    /**
     * Test whether or not this BSCObject has any immediate descendents.
     * Immediate descendents are objects that are directly connected to this
     * object via the leadsTo relationship.
     *
     * @return True if this BSCObject has 1 or more children, false otherwise.
     * @see #getChildren()
     */
    public boolean hasChildren() {
        return getChildren().hasNext();
    }

    /**
     * Get children derived from "related_to" statements
     * The intention here is to test the implications of categorizing all relations involving
     * processes (as either subject or target) as a "related_to" and all interactions involving the majority of
     * biological materials as "isSourceOf" -- hence this statement should deliver any children stemming from
     * "related_to" relationships
     *
     * @return
     */
    public BSCObjBasicIter getTypedRelationChildren(Property p) {
        if (isLiteral()) {
            return new BSCObjBasicIter();
        }

        return new BSCObjBasicIter(
                node.asResource().listProperties(p), pm
        );
    }

    /**
     * Get descendents from "related_to" statements.
     * The desired action is to start at an object that is defined as being "relatedTo" some children and then
     * following those children "isSourceOf" relationships.
     */
    public BSCObjDescIter getTypedRelatedDescendents(int depth, Property p) {
        if (isLiteral())
            return new BSCObjDescIter();
        return new BSCObjDescIter(getTypedRelationChildren(p), depth, pm);
    }

    /**
     * Get an iterator for all immediate descendents of this object.  That is,
     * return an iterator to all objects of RDF "relatedTo,derivesFrom,dependsOn" statements for
     * which this BSCObject is the subject.
     *
     * @return An iterator for all children of this BSCObject.
     * @see #hasChildren()
     */
    public BSCObjBasicIter getChildren() {
        if (isLiteral()) {
            return new BSCObjBasicIter();
        }
        // Get all possible relationship operators for the all Children method
        StmtIterator s = node.asResource().listProperties(props.getRelatedTo());
        StmtIterator s1 = node.asResource().listProperties(props.getDependsOn());
        StmtIterator s2 = node.asResource().listProperties(props.getDerivesFrom());
        StmtIterator s3 = node.asResource().listProperties(props.getAlias_of());

        s.andThen(s1).andThen(s2).andThen(s3);

        return new BSCObjBasicIter(s, pm);
    }

    /**
     * Get all of the direct properties of this object
     *
     * @return a StmtIterator
     */
    public StmtIterator getProperties() {
        if (isLiteral()) {
            return null;
        }
        return node.asResource().listProperties();

    }

    /**
     * Geta all of the properties that are not a type of relationship to another object.
     * That is, get properties that talk only about the object itself (sex, color, weight, name)
     *
     * @return a StmtIterator
     */
    public Iterator getAttributeProperties() {
        if (isLiteral()) {
            return null;
        }

        // Note: When dealing with the StmtIterator we get concurrentmodification exception to the
        // current BSCObject.  Thus, it is necessary to construct a new iterator to look at just a
        // subset of properties.
        ArrayList a = new ArrayList();
        StmtIterator i = node.asResource().listProperties();
        boolean foundRelation = false;
        while (i.hasNext()) {
            Statement s = i.next();

            // Search for anything that is a relationship expression here
            if (s.getPredicate().equals(props.getDependsOn()) ||
                    s.getPredicate().equals(props.getRelatedTo()) ||
                    s.getPredicate().equals(props.getAlias_of()) ||
                    s.getPredicate().equals(props.getDerivesFrom())) {
                // We only want ONE relation defined as an attribute of this object
                if (!foundRelation) {
                    a.add(ResourceFactory.createStatement(s.getSubject(), s.getPredicate(), s.getObject()));
                    foundRelation = true;
                }
            } else {
                a.add(s);
            }
        }

        return a.iterator();
    }

    /**
     * Get an iterator for all descendents of this object up to and including
     * the specified traversal depth.  Objects are returned by the iterator in
     * breadth-first traversal order.  Thus, object.getDescendants(1) is
     * semantically equivalent to object.getChildren().
     *
     * @param depth The maximum depth (inclusive) to traverse the graph.
     * @return An iterator for all descendents of this BSCObject.
     * @see #getChildren()
     * @see #visitDescendents(ModelVisitor)
     * @see #getDerivesFrom()
     */
    public BSCObjDescIter getDescendents(int depth) {
        if (isLiteral())
            return new BSCObjDescIter();
        return new BSCObjDescIter(getChildren(), depth, pm);
    }


    /**
     * Get an iterator for all relatives of this object.  In this context,
     * "derives_from" means all objects that are connected to this BSCObject via
     * derives_from statements, either explicitly or via inferred triples.  This
     * method differs from the descendent-based methods in that no notion of
     * the originally-specified object hierarchy is maintained; that is, multi-
     * level relationships are essentially "flattened".
     *
     * @return An iterator for all relatives of this BSCObject.
     * @see #getChildren()
     * @see #getDescendents(int)
     * @see #visitDescendents(ModelVisitor)
     */
    public BSCObjBasicIter getDerivesFrom() {
        if (isLiteral()) {
            return new BSCObjBasicIter();
        }

        return new BSCObjBasicIter(
                node.asResource().listProperties(props.getDerivesFrom()), pm
        );
    }


    /**
     * Test whether or not this BSCObject has any immediate ancestors.
     * Immediate ancestors are objects that are directly connected to this
     * object via the "comesFrom" relationship.
     *
     * @return True if this BSCObject has 1 or more parents, false otherwise.
     * @see #getParent()
     * @see #getParents()
     */
    public boolean hasParents() {
        return getParents().hasNext();
    }

    /**
     * Get the immediate ancestor of this object.  That is, if this BSCObject
     * is the subject of a hasSource RDF statement, return the object of that
     * statement.  It this object has multiple parents, only one of them will
     * be returned.  In this situation, there are no guarantees about which
     * particular parent is returned.  This is a convenience method that is
     * semantically equivalent to object.getParents().next().
     *
     * @return A parent of this BSCObject.
     * @see #hasParents()
     * @see #getParents()
     */
    public BSCObject getParent() {
        if (hasParents())
            return getParents().next();
        else
            return null;

        /*Model model = node.getModel();

        SimpleSelector selector = new SimpleSelector(null, props.getRelatives(), node);

        if (model.listStatements(selector).hasNext())
            return new BSCObject(model.listStatements(selector).next().getSubject(), pm);
        else
            return null;
         */
    }

    /**
     * Get an iterator for all immediate ancestors of this object.  That is,
     * return an iterator to all objects of RDF "comesFrom" statements for
     * which this BSCObject is the subject.
     *
     * @return An iterator for all parents of this BSCObject.
     * @see #hasParents()
     * @see #getParent()
     */
    public BSCObjBasicIter getParents() {
        /*return new BSCObjBasicIter(
                node.asResource().listProperties(props.getComesFrom()), pm
                );*/

        // Note that this method could be implemented using the inferred
        // comesFrom relationship, as in the commented-out code
        // above.  However, this approach would fail any time OWL inferencing
        // is not available.  The method below will produce the correct results
        // even without OWL inferencing.

        Model model = node.getModel();

        //SimpleSelector selector = new SimpleSelector(null, props.getDerivesFrom(), node);
        // JBD: return any of the predicates by specifying null
        SimpleSelector selector = new SimpleSelector(null, null, node);

        return new BSCObjBasicIter(model.listStatements(selector), pm, true);
    }

    /**
     * Get an iterator for all siblings of this object.  Siblings are defined
     * as all children of all parents of this BSCObject that are not equal to
     * this BSCObject.
     *
     * @return An iterator for all siblings of this BSCObject.
     */
    public BSCObjSiblingsIter getSiblings() {
        return new BSCObjSiblingsIter(this);
    }

    /**
     * Use the provided visitor to visit this object and its descendents.  The
     * traversal continues until visitor.visitEnter() returns false; this
     * signals the maximum depth of the traversal.  All descendents at that
     * depth are visited.  Descendents are visited in depth-first order.
     *
     * @param visitor The object to visit the descendents with.
     * @see #getChildren()
     * @see #getDescendents(int)
     * @see #getDerivesFrom()
     */
    public void visitDescendents(ModelVisitor visitor) {
        visitDescendents(visitor, 0, 0);
    }

    /**
     * This method actually implements the traversal of this object's
     * descendents.  Also provides a starting depth and the position of this
     * object among its siblings for keeping track of the current position in
     * the traversal.
     *
     * @param visitor   The object to visit the descendents with.
     * @param currdepth The current depth in the traversal, relative to the starting object.
     * @param child_cnt The position of the object among its siblings.
     */
    private void visitDescendents(ModelVisitor visitor, int currdepth, int child_cnt) {
        // notify that we're entering this node
        if (visitor.visitEnter(this, currdepth, child_cnt)) {
            int child_num = 0;
            // visit each child of this node
            for (BSCObject child : this.getChildren()) {
                child.visitDescendents(visitor, currdepth + 1, child_num);
                child_num++;
            }
        }
        // notify that we're leaving this node
        visitor.visitLeave(this, currdepth, child_cnt);
    }

    /**
     * Use the provided visitor to visit this object and its ancestors.  The
     * traversal continues until visitor.visitEnter() returns false; this
     * signals the maximum depth of the traversal.  All ancestors at that
     * depth are visited.  Ancestors are visited with a depth-first traversal.
     *
     * @param visitor The object to visit the ancestors with.
     */
    public void visitAncestors(ModelVisitor visitor) {
        visitAncestors(visitor, 0, 0);
    }

    /**
     * This method actually implements the traversal of this object's
     * ancestors.  Also provides a starting depth and the position of this
     * object among its siblings for keeping track of the current position in
     * the traversal.  Note that "depth" is simply interpreted as the distance
     * from the starting object.
     *
     * @param visitor    The object to visit the ancestors with.
     * @param currdepth  The current depth in the traversal, relative to the starting object.
     * @param ancstr_cnt The position of the object among its siblings.
     */
    private void visitAncestors(ModelVisitor visitor, int currdepth, int ancstr_cnt) {
        // notify that we're entering this node
        if (visitor.visitEnter(this, currdepth, ancstr_cnt)) {
            int ancstr_num = 0;

            // visit each child of this node
            for (BSCObject child : this.getParents()) {
                child.visitAncestors(visitor, currdepth + 1, ancstr_num);
                ancstr_num++;
            }
        }

        // notify that we're leaving this node
        visitor.visitLeave(this, currdepth, ancstr_cnt);
    }

    /**
     * Get a text representation of the URI of this object.  If possible,
     * a namespace prefix is used to form a short string.
     *
     * @return A text representation of this BSCObject.
     */
    @Override
    public String toString() {
        return pm.shortForm(node.toString());
    }

    /**
     * Get the shared properties of this object
     * @return
     */
    public SharedProperties getProps() {
        return props;
    }
}
