package Loading;

import BSCcore.Configurable;
import BSCcore.SettingsManager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: biocode
 * Date: Jul 19, 2011
 * Time: 11:59:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadProperties implements Configurable {
    static SettingsManager sm = SettingsManager.getInstance();

    protected String user;
    protected String pass;
    protected String url;

    // protected String fromGraph;
    // protected String from;

    protected String relationsGraph;
    protected String relations;

    protected String godGraph;
    protected String god;

    protected String related, leadsto, comesfrom;
    protected String datelastmodified;
    protected String sameas;

    protected String relationsFile;
    protected String godFile;

    protected String mainGraph;
    protected String main;

    protected Integer option;
    protected Integer triplestore;

    protected String filestore;
    protected boolean debug = false;

    protected Iterator<String> graphs;

    // Constants defining Output Error Messages
    final protected static int NORMAL = 0;
    final protected static int RELATIONS_LOAD_ERROR = 1;
    final protected static int GOD_LOAD_ERROR = 2;
    final protected static int INVALID_ARGS_ERROR = 3;
    final protected static int MAIN_LOAD_ERROR = 4;
    final protected static int RELATIONS_FILE_CREATION_ERROR = 5;

    // Constants to Set which options to run in building BiSciCol indexes
    final protected static int ALL = 0;
    final protected static int MAIN = 1;
    final protected static int RELATIONS = 2;
    final protected static int GOD = 3;

    // Constants to Set Reasoner
    final protected static int NONE = 0;
    final protected static int PELLET = 1;
    final protected static int HERMIT = 2;
    final protected static int OWL_MEM_RULE_INF = 3;
    final protected static int JENA_CUSTOM_RULES = 4;

    // what triplestore to use
    final protected static int FILE = 0;
    final protected static int VIRTUOSO = 1;

    protected Integer relationsReasoner = 0;

    public LoadProperties() {
    }

    /**
     * Convert String "true,false" to boolean values
     *
     * @param s
     * @return
     */
    private boolean getBooleanProperty(SettingsManager sm, String s) {
        String prop = sm.retrieveValue(s);
        if (prop.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    /**
     * Parse delimited values and return Iterator
     *
     * @param propName
     * @return
     */
    private Iterator<String> getDelimitedProperty(SettingsManager sm, String propName) {
        String[] a = sm.retrieveValue(propName).split(";");
        List<String> elements = Arrays.asList(a);
        return elements.iterator();
    }


    public void configure(SettingsManager sm) {
        user = sm.retrieveValue("virt_user");
        pass = sm.retrieveValue("virt_pass");
        url = sm.retrieveValue("virt_url");

        mainGraph = sm.retrieveValue("mainGraph");
        main = "<" + mainGraph + ">";
        related = sm.retrieveValue("related");
        comesfrom = sm.retrieveValue("comesfrom");
        leadsto = sm.retrieveValue("leadsto");
        datelastmodified = sm.retrieveValue("datelastmodified");
        sameas = sm.retrieveValue("sameas");

        //relationsFile = sm.retrieveValue("relationsFile");
        //godFile = sm.retrieveValue("godFile");
        graphs = getDelimitedProperty(sm, "graphs");
        option = Integer.parseInt(sm.retrieveValue("option"));
        filestore = sm.retrieveValue("filestore");
        debug = getBooleanProperty(sm, "debug");

        // All the options for reasoning
        relationsReasoner = Integer.parseInt(sm.retrieveValue("relationsReasoner"));
    }
}