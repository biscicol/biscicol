
package BSCcore;

import java.io.FileNotFoundException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class SettingsManagerTest
{
    SettingsManager sm;
    
    @Before
    public void setUp() {
        sm = SettingsManager.getInstance("biscicolsettings.template");
    }

    /**
     * Test of getInstance method, of class SettingsManager.
     */
    @Test
    public void testGetInstance() {
        // make sure that we got a valid instance
        assertNotNull(sm);
        
        // make sure that the class works as a singleton
        assertEquals(sm, SettingsManager.getInstance());
    }

    /**
     * Test of getPropertiesFile method, of class SettingsManager.
     */
    @Test
    public void testGetPropertiesFile() throws FileNotFoundException {
        String result = sm.getPropertiesFile();
        assertEquals("biscicolsettings.template", result);
    }

    /**
     * Test of setPropertiesFile method, of class SettingsManager.
     */
    @Test
    public void testSetPropertiesFile() {
        sm.setPropertiesFile("somefile.file");
        
        assertEquals("somefile.file", sm.getPropertiesFile());
        
        sm.setPropertiesFile("biscicolsettings.template");
    }

    /**
     * Test of loadProperties and retrieveValue methods.
     */
    @Test
    public void testRetrieveValue() throws FileNotFoundException {
        String result;
        
        sm.loadProperties();
        
        // verify that an extant key works
        result = sm.retrieveValue("search_ontModelSpec");
        //assertEquals("OWL_LITE_MEM_RULES_INF", result);
        assertEquals("RDFS_MEM", result);
        
        // verify that a default value works with a non-existant key
        result = sm.retrieveValue("this_is_a_fake_key", "default value");
        assertEquals("default value", result);
    }
}
