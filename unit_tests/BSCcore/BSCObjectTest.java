package BSCcore;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashSet;


/**
 * @author stuckyb
 */
public class BSCObjectTest extends TestCore {

    BSCObject obj;

    @Before
    @Override
    public void setUp() {
        obj = model.getBSCObject("biocodeevent:ID1");
    }

    /**
     * Test of isLiteral method of class BSCObject.
     */
    @Test
    public void testIsLiteral() {
        assertFalse(obj.isLiteral());
    }

    /**
     * Tests the property lookup methods of BSCObject.  To streamline the test
     * code, tests for all methods are gathered into a single test method.
     */
    @Test
    public void testGetProperties() {
        //System.out.println(obj.getType());
        assertEquals(obj.getType(), "dwc:Event");

        // getGeo() is not yet implemented
        //assertEquals(obj.getGeo(), "");

        // NOTE: something weird about this test-- when Brian runs test it passes with 07:00 on the end
        // but when i run it it passes with 08:00 on the end.
        //DateTime expResult = new DateTime("2011-01-01T00:00:00.000-07:00");
        //DateTime unexpResult = new DateTime("2011-01-02T00:00:00.000-07:00");
        DateTime expResult = new DateTime("2011-01-01T00:00:00.000-08:00");
        DateTime unexpResult = new DateTime("2011-01-02T00:00:00.000-08:00");

        // this should test both that we are getting a valid DateTime object
        // and that it is set to the expected date/time
        assertEquals(obj.getDateLastModified().compareTo(expResult), 0);
        assertEquals(obj.getDateLastModified().compareTo(unexpResult), -1);
    }

    /**
     * Test of getURI method of class BSCObject.
     */
    @Test
    public void testGetURI() {
        //System.out.println(obj.getURI());
        assertEquals(obj.getURI(), "http://biocode.berkeley.edu/events/ID1");
    }

    /**
     * Test of equals method of class BSCObject.
     */
    @Test
    public void testEquals() {
        BSCObject obj1 = model.getBSCObject("biocodeevent:ID1");
        BSCObject obj2 = model.getBSCObject("biocodeevent:ID2");

        // basic functionality tests
        assertTrue(obj.equals(obj1));
        assertFalse(obj.equals(obj2));

        // Now test special cases of equals() that are detailed in the
        // documentation for the Java Object base class.

        // test reflexivity
        assertTrue(obj.equals(obj));

        // test symmetry
        assertTrue(obj.equals(obj1));
        assertTrue(obj1.equals(obj));

        // test null reference
        assertFalse(obj.equals(null));
    }

    /**
     * Test of hashCode method of class BSCObject.
     */
    @Test
    public void testHashCode() {
        BSCObject obj1 = model.getBSCObject("biocodeevent:ID1");
        BSCObject obj2 = model.getBSCObject("biocodeevent:ID2");

        // make sure hashCode is returning non-zero values
        assertFalse(obj.hashCode() == 0);
        assertFalse(obj1.hashCode() == 0);
        assertFalse(obj2.hashCode() == 0);

        // Verify that hashCode resturns identical values for equivalent
        // BSCObjects, and different values for different objects.
        assertEquals(obj.hashCode(), obj1.hashCode());
        assertFalse(obj.hashCode() == obj2.hashCode());
    }

    /**
     * Test of getChildren method of class BSCObject.
     */
    @Test
    public void testGetChildren() {
        // objects with no siblings
        BSCObject obj2 = model.getBSCObject("biocode:ID6B");
        BSCObjBasicIter result = obj2.getChildren();

        assertFalse(result.hasNext());

        // objects with a single child
        obj2 = model.getBSCObject("biocode:ID3A");
        result = obj2.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID3A-2", result.next().toString());
        assertFalse(result.hasNext());

        obj2 = model.getBSCObject("biocodeevent:ID7");
        result = obj2.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID6B", result.next().toString());
        assertFalse(result.hasNext());

        // objects with multiple explicit siblings
        obj2 = model.getBSCObject("biocodeevent:ID6");
        result = obj2.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID6B", result.next().toString());
        assertEquals("biocode:ID6A", result.next().toString());
        assertFalse(result.hasNext());

        // objects with multiple inferred siblings
        HashSet<String> children = new HashSet<String>();
        children.add("biocode:ID1A");
        children.add("biocode:ID3B");
        children.add("biocode:ID3A");
        children.add("biocode:ID2A");

        result = obj.getChildren();
        for (BSCObject child : result) {
            //System.out.println(rel.toString());
            assertTrue(children.contains(child.toString()));
            children.remove(child.toString());
        }

        assertTrue(children.isEmpty());
        assertFalse(result.hasNext());
    }

    /**
     * Test of hasChildren method of class BSCObject.
     */
    @Test
    public void testHasChildren() {
        // test of object with siblings
        assertTrue(obj.hasChildren());

        // test of object with no siblings
        BSCObject obj1 = model.getBSCObject("biocode:ID3A-2");

        assertFalse(obj1.hasChildren());
    }

    /**
     * Test of getDescendants method of class BSCObject.
     */
    @Test
    public void testGetDescendents() {
        // first, test only going one level deep
        // this should be the same as getChildren()
        HashSet<String> descendents = new HashSet<String>();
        descendents.add("biocode:ID1A");
        descendents.add("biocode:ID3B");
        descendents.add("biocode:ID3A");
        descendents.add("biocode:ID2A");

        BSCObjDescIter result = obj.getDescendents(1);
        for (BSCObject desc : result) {
            //System.out.println(rel.toString());
            assertTrue(descendents.contains(desc.toString()));
            descendents.remove(desc.toString());
        }

        assertTrue(descendents.isEmpty());
        assertFalse(result.hasNext());

        // now test a multi-level siblings traversal
        descendents = new HashSet<String>();
        descendents.add("biocode:ID1A");
        descendents.add("biocode:ID3B");
        descendents.add("biocode:ID3A");
        descendents.add("biocode:ID2A");

        result = obj.getDescendents(2);
        BSCObject desc;
        for (int cnt = 0; cnt < 4; cnt++) {
            desc = result.next();
            //System.out.println(rel.toString());
            assertEquals(1, result.getDepth());
            assertTrue(descendents.contains(desc.toString()));
            descendents.remove(desc.toString());
        }

        assertTrue(descendents.isEmpty());
        assertTrue(result.hasNext());
        assertEquals("biocode:ID3A-2", result.next().toString());
        assertEquals(2, result.getDepth());
        assertFalse(result.hasNext());
    }

    /**
     * Test of getRelatives method of class BSCObject.
     */
    @Test
    public void testGetRelatives() {
        // The order returned by Jena is not always consistent.  So, we use a
        // HashSet to allow relatedTo objects to be returned in arbitrary
        // order.  The same approach is used in testGetParents(),
        // testGetChildren(), and testGetDescendents().
        HashSet<String> rels = new HashSet<String>();
        rels.add("biocodeevent:ID1");
        rels.add("biocode:ID1A");
        rels.add("biocode:ID3B");
        rels.add("biocode:ID3A");
        rels.add("biocode:ID3A-2");
        rels.add("biocode:ID2A");
        rels.add("biocodeevent:ID2");
        rels.add("biocodeevent:ID3");
        rels.add("biocodeevent:ID4");

        BSCObjBasicIter result = obj.getDerivesFrom();

        for (BSCObject rel : result) {
            //System.out.println(rel.toString());
            assertTrue(rels.contains(rel.toString()));
            rels.remove(rel.toString());
        }

        assertTrue(rels.isEmpty());
        assertFalse(result.hasNext());
    }

    /**
     * Test of hasParents method of class BSCObject.
     */
    @Test
    public void testHasParents() {
        // object with parent
        BSCObject obj1 = model.getBSCObject("biocode:ID3A");

        assertTrue(obj1.hasParents());

        // object with no parent
        assertFalse(obj.hasParents());
    }

    /**
     * Test of getParent method of class BSCObject.
     */
    @Test
    public void testGetParent() {
        assertNull(obj.getParent());

        // Test getParent with an object that has only one parent
        BSCObject obj1 = model.getBSCObject("biocode:ID3A-2");
        assertEquals("biocode:ID3A", obj1.getParent().toString());

        // object with multiple inferred parents
        // getParent does not guarantee the correct parent if there are multiple
        // parents.  Hence, this test only checks to see if one of the possible correct
        // parents is returned (in this situation, it is better to use getParents()
        HashSet<String> parents = new HashSet<String>();
        parents.add("biocodeevent:ID1");
        parents.add("biocodeevent:ID2");
        parents.add("biocodeevent:ID3");
        parents.add("biocodeevent:ID4");

        obj1 = model.getBSCObject("biocode:ID1A");
        BSCObject res = obj1.getParent();
        assertTrue(parents.contains(res.toString()));
    }

    /**
     * Test of getParents method of class BSCObject.
     */
    @Test
    public void testGetParents() {
        // object with no parent
        assertFalse(obj.getParents().hasNext());

        // object with a single parent
        BSCObject obj1 = model.getBSCObject("biocode:ID3A-2");
        BSCObjBasicIter res = obj1.getParents();
        assertTrue(res.hasNext());
        assertEquals("biocode:ID3A", res.next().toString());
        assertFalse(res.hasNext());

        obj1 = model.getBSCObject("biocode:ID6A");
        res = obj1.getParents();
        assertTrue(res.hasNext());
        assertEquals("biocodeevent:ID6", res.next().toString());
        assertFalse(res.hasNext());

        // object with multiple explicit parents
        obj1 = model.getBSCObject("biocode:ID6B");
        res = obj1.getParents();
        assertTrue(res.hasNext());
        assertEquals("biocodeevent:ID7", res.next().toString());
        assertEquals("biocodeevent:ID6", res.next().toString());
        assertFalse(res.hasNext());

        // object with multiple inferred parents

        // This is tricky, because the order returned by Jena is not always
        // consistent.  So, we use a HashSet to allow parents to be returned
        // in arbitrary order.  Sort of inelegant, but it works.
        HashSet<String> parents = new HashSet<String>();
        parents.add("biocodeevent:ID1");
        parents.add("biocodeevent:ID2");
        parents.add("biocodeevent:ID3");
        parents.add("biocodeevent:ID4");

        obj1 = model.getBSCObject("biocode:ID1A");
        res = obj1.getParents();

        // Now get each parent, make sure it is in the HashSet, and then remove
        // it from the HashSet before getting the next parent.
        BSCObject pobj;
        for (int cnt = 0; cnt < 4; cnt++) {
            assertTrue(res.hasNext());
            pobj = res.next();
            assertTrue(parents.contains(pobj.toString()));
            parents.remove(pobj.toString());
        }

        assertFalse(res.hasNext());
        assertTrue(parents.isEmpty());
    }

    /**
     * Test of getSiblingsAsModel method of class BSCObject.
     */
    @Test
    public void testGetSiblings() {
        // object is root-level, so has no siblings (as currently implemented)
        BSCObjSiblingsIter result = obj.getSiblings();
        assertFalse(result.hasNext());

        // object with a parent, but no siblings
        BSCObject obj1 = model.getBSCObject("biocode:ID3A-2");
        result = obj1.getSiblings();
        assertFalse(result.hasNext());

        // object with explicit siblings
        obj1 = model.getBSCObject("biocode:ID6A");
        result = obj1.getSiblings();
        assertTrue(result.hasNext());
        assertEquals("biocode:ID6B", result.next().toString());
        assertFalse(result.hasNext());

        // object with inferred siblings
        HashSet<String> siblings = new HashSet<String>();
        siblings.add("biocode:ID3B");
        siblings.add("biocode:ID3A");
        siblings.add("biocode:ID2A");

        obj1 = model.getBSCObject("biocode:ID1A");
        result = obj1.getSiblings();
        for (BSCObject sib : result) {
            //System.out.println(rel.toString());
            assertTrue(siblings.contains(sib.toString()));
            siblings.remove(sib.toString());
        }

        assertTrue(siblings.isEmpty());
        assertFalse(result.hasNext());
    }

    /**
     * Test of visitDescendents method of class BSCObject.
     */
    @Test
    public void testVisitDescendents() {
        TestVisitor visitor = new TestVisitor();

        // Visit the descendents of obj and make sure the resulting string
        // built by the TestVisitor is correct.
        obj.visitDescendents(visitor);

        // There are a bunch of possibilities that make sense here, i added some of them here
        // to get initial tests running.  Likely there is a more elegant way to solve this.
        HashSet<String> possibilities = new HashSet<String>();
        possibilities.add("biocodeevent:ID1(biocode:ID1A,biocode:ID3B,biocode:ID3A(biocode:ID3A-2),biocode:ID2A)");
        possibilities.add("biocodeevent:ID1(biocode:ID1A,biocode:ID3B,biocode:ID2A,biocode:ID3A(biocode:ID3A-2))");
        possibilities.add("biocodeevent:ID1(biocode:ID1A,biocode:ID2A,biocode:ID3B,biocode:ID3A(biocode:ID3A-2))");
        possibilities.add("biocodeevent:ID1(biocode:ID2A,biocode:ID1A,biocode:ID3B,biocode:ID3A(biocode:ID3A-2))");

        assertTrue(
                possibilities.contains(visitor.getVisitStr().toString())
        );

        // Verify that the proper number of objects were visited.
        assertEquals(6, visitor.getTotalVisited());
    }

    /**
     * Test of visitAncestors method of class BSCObject.
     */
    @Test
    public void testVisitAncestors() {
        TestVisitor visitor = new TestVisitor();
        BSCObject obj1 = model.getBSCObject("biocode:ID6A-2");

        // Visit the ancestors of obj and make sure the resulting string
        // built by the TestVisitor is correct.
        obj1.visitAncestors(visitor);
        assertEquals(
                "biocode:ID6A-2biocode:ID6A(biocodeevent:ID6())",
                visitor.getVisitStr()
        );

        // Verify that the proper number of objects were visited.
        assertEquals(3, visitor.getTotalVisited());
    }

    /**
     * Test of toString method of class BSCObject.
     */
    @Test
    public void testToString() {
        //System.out.println(obj.toString());
        assertEquals(obj.toString(), "biocodeevent:ID1");
    }
}
