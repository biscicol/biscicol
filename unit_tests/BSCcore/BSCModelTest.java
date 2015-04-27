

package BSCcore;

import com.hp.hpl.jena.ontology.OntModelSpec;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

import java.util.HashSet;


/**
 *
 * @author stuckyb
 */
public class BSCModelTest {
    private FileBSCModel model;
    private static String baseuri;

    @BeforeClass
    public static void setUpClass() {
        baseuri = System.getProperty("user.dir");
        baseuri = "file://" + baseuri + "/sampledata/";
    }

    /**
     * Test of the add method.
     */
    @Test
    public void testAdd() throws Exception {
        BSCObject obj;
        
        model = new FileBSCModel(baseuri + "test.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);
        FileBSCModel model2 = new FileBSCModel(baseuri + "test2.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);

        // test combining the two models
        model.add(model2);
        
        // test an object in the new data with a single child
        obj = model.getBSCObject("biocodeevent:ID9");
        BSCObjBasicIter result = obj.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID9A", result.next().toString());
        assertFalse(result.hasNext());
        
        // test a child object linked in from the new data
        obj = model.getBSCObject("biocode:ID6B");
        result = obj.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID6B-2", result.next().toString());
        assertFalse(result.hasNext());
        
        result = obj.getDerivesFrom();

        assertTrue(result.hasNext());
        //assertEquals("biocode:ID6B-2", result.next().toString());
        //assertFalse(result.hasNext());
        
        // test a "sameAs" object linked in from the new data
        obj = model.getBSCObject("biocodeevent:ID8");
        result = obj.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID6B", result.next().toString());
        assertFalse(result.hasNext());

        // Load a hash set with all expected parent objects from the new
        // sameAs relationship.
        HashSet<String> parents = new HashSet<String>();
        parents.add("biocodeevent:ID6");
        parents.add("biocodeevent:ID7");
        parents.add("biocodeevent:ID8");
        
        obj = model.getBSCObject("biocode:ID6B");
        result = obj.getParents();
        
        // Now get each parent, make sure it is in the HashSet, and then remove
        // it from the HashSet before getting the next parent.
        BSCObject pobj;
        for (int cnt = 0; cnt < 3; cnt++)
        {
            assertTrue(result.hasNext());
            pobj = result.next();
            assertTrue(parents.contains(pobj.toString()));
            parents.remove(pobj.toString());
        }
        
        assertFalse(result.hasNext());
        assertTrue(parents.isEmpty());
        
        // test a duplicate object to make sure it is only included once
        obj = model.getBSCObject("biocode:ID6A");
        
        // first, test via getChildren()
        result = obj.getChildren();

        assertTrue(result.hasNext());
        assertEquals("biocode:ID6A-2", result.next().toString());
        assertFalse(result.hasNext());

        // then, test a sparql query looking for all tissue objects
        HashSet<String> tissues = new HashSet<String>();
        tissues.add("biocode:ID3A-2");
        tissues.add("biocode:ID6A-2");
        tissues.add("biocode:ID6B-2");
        
        QueryResult qres = model.selectQuery("SELECT ?specname WHERE { ?specname a bsc:Tissue }");
        for (QuerySolutionIter qs : qres) {
            for (BSCObject node : qs) {
                assertTrue(tissues.contains(node.toString()));
                tissues.remove(node.toString());
            }
        }
        
        assertTrue(tissues.isEmpty());
    }

    /**
     * Test of the getBSCObject method.
     */
    @Test
    public void testGetBSCObject() throws Exception {
        model = new FileBSCModel(baseuri + "test.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);
        
        // first, test an object that actually exists
        BSCObject obj = model.getBSCObject("biocode:ID6A");
        assertEquals("biocode:ID6A", obj.toString());
        
        // now, test an object that doesn't exist
        assertNull(model.getBSCObject("fake_object"));
        
        // test it again to make sure it didn't accidentally get added
        assertNull(model.getBSCObject("fake_object"));
    }
        
    /**
     * Test of the selectQuery method.
     */
    @Test
    public void testSelectQuery() throws Exception {
        model = new FileBSCModel(baseuri + "test.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);
        
        // Just test a very simple SPARQL query to make sure things work.
        // There is probably no need to test more complicated queries because
        // the queries fall back on the ARQ code anyway.  As long as we can run
        // a query and get the results back, our code should be good.
        HashSet<String> tissues = new HashSet<String>();
        tissues.add("biocode:ID3A-2");
        tissues.add("biocode:ID6A-2");
        
        QueryResult qres = model.selectQuery("SELECT ?obj WHERE { ?obj a bsc:Tissue }");
        for (QuerySolutionIter qs : qres) {
            for (BSCObject obj : qs) {
                assertTrue(tissues.contains(obj.toString()));
                tissues.remove(obj.toString());
            }
        }
        
        assertTrue(tissues.isEmpty());
    }
    
    /**
     * Test of the describeQuery method.
     */
    @Test
    public void testDescribeQuery() throws Exception {
        model = new FileBSCModel(baseuri + "test.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);
        
        // Again, just test a very simple query to make sure things work.
        HashSet<String> stmts = new HashSet<String>();
        stmts.add("biocode:ID6A-2 ma:hasSource biocode:ID6A");
        stmts.add("biocode:ID6A-2 ma:isRelatedTo biocode:ID6A");
        stmts.add("biocode:ID6A-2 bsc:relativeOf biocodeevent:ID7");
        stmts.add("biocode:ID6A-2 bsc:relativeOf biocode:ID6B");
        stmts.add("biocode:ID6A-2 bsc:relativeOf biocodeevent:ID6");
        stmts.add("biocode:ID6A-2 bsc:relativeOf biocode:ID6A-2");
        stmts.add("biocode:ID6A-2 bsc:relativeOf biocode:ID6A");
        stmts.add("biocode:ID6A-2 rdf:type bsc:Tissue");
        stmts.add("biocode:ID6A-2 dcterms:modified 2011-01-01");

        BSCModel dres = model.describeQuery("DESCRIBE biocode:ID6A-2");
        // check all statements in the resulting model
        for (RDFStatement stmt : dres.getRDFStatements()) {
            //System.out.println(stmt);
            assertTrue(stmts.contains(stmt.toString()));
            stmts.remove(stmt.toString());
        }
        
        // make sure all statements were accounted for
        assertTrue(stmts.isEmpty());
    }
}