package BSCcore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author stuckyb
 */
public class SharedPropertiesTest extends TestCore {

    /**
     * Test of the SharedProperties class.
     */
    @Test
    public void testSharedProperties() {
        SharedProperties props = new SharedProperties(model.getModel());

        // verify that all property definitions were constructed correctly
        assertEquals("http://biscicol.org/biscicol.rdf#Agent", props.getAgent().getURI());
        assertEquals("http://purl.org/dc/terms/modified", props.getDateLastModified().getURI());
        assertEquals("http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing", props.getGeo().getURI());
        assertEquals("http://biscicol.org/biscicol.rdf#relativeOf", props.getDerivesFrom().getURI());
        assertEquals("http://www.w3.org/ns/ma-ont#isSourceOf", props.getDependsOn().getURI());
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", props.getType().getURI());
    }

    /**
     * Test of SharedPropertiesFactory.
     */
    @Test
    public void testSharedPropertiesFactory() {
        SharedProperties props1 = SharedPropertiesFactory.getSharedProperties(model.getModel());
        SharedProperties props2 = SharedPropertiesFactory.getSharedProperties(model.getModel());

        // If the factory is returning references correctly, props1 and props2
        // should refer to the same object since they derive from the same model.
        assertEquals(props1, props2);
    }
}
