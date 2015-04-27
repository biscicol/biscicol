package BSCcore;

import com.hp.hpl.jena.ontology.OntModelSpec;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author stuckyb
 */
public class TestCore {
    protected static BSCModel model;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // load the test model
        // basedir should be the root of the source tree
        String basedir = System.getProperty("user.dir");

        model = new FileBSCModel(
                "file://" + basedir + "/sampledata/test.n3",
                OntModelSpec.OWL_LITE_MEM_RULES_INF);
                //OntModelSpec.RDFS_MEM);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}