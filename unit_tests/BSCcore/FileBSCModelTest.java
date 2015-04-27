

package BSCcore;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author stuckyb
 */
public class FileBSCModelTest {
    private FileBSCModel model;
    private static String baseuri;

    @BeforeClass
    public static void setUpClass() {
        baseuri = System.getProperty("user.dir");

        baseuri = "file://" + baseuri + "/sampledata/";
    }

    /**
     * Test of loadFile method.
     */
    @Test
    public void testLoadFile() throws Exception {
        // first, make sure a correct model file loads successfully
        model = new FileBSCModel(baseuri + "test.n3", OntModelSpec.OWL_LITE_MEM_RULES_INF);

        // verify that we can actually retrieve data from it
        assertTrue(model.getBSCObject("biocodeevent:ID3").hasChildren());
    }

    /**
     * Tests loading a file with bad RDF syntax and verifies that the expected
     * exception is thrown.
     */
    @Test(expected=TurtleParseException.class)
    public void testBadSyntaxLoadFile() throws Exception {
        // try loading a model with intentionally broken syntax
        model = new FileBSCModel(baseuri + "test-bad_syntax.n3", OntModelSpec.RDFS_MEM);
    }

    /**
     * Tests loading a file with invalid UTF-8 data.  This test is necessary
     * because it appears that the Jena code that handles reading Turtle and N3
     * data files does not properly handle UTF-8 stream errors.  When a UTF-8
     * error is encountered, the Jena code will silently fail and return an
     * empty model as if the read had been successful.  The code in FileBSCModel
     * is designed to guard against this by testing if the UTF-8 stream is
     * valid before attempting to read it with Jena.
     */
    @Test(expected=Exception.class)
    public void testBadUTF8LoadFile() throws Exception {
        // try loading a model with intentionally broken UTF-8
        model = new FileBSCModel(baseuri + "test-bad_utf8.n3", OntModelSpec.RDFS_MEM);
    }

    /**
     * Tests loading a non-existent file and verifies that the expected
     * exception is thrown.
     */
    @Test(expected=IOException.class)
    public void testNoFileLoadFile() throws Exception {
        // try loading a model with intentionally broken syntax
        model = new FileBSCModel(baseuri + "file_doesn't_exist.n3", OntModelSpec.RDFS_MEM);
    }
}