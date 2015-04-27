package BSCcore;

import Loading.Results;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import org.joda.time.DateTime;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * VirtuosoBSCFilteredModel filters data from a Virtuoso Database for use within
 * BiSciCol so we can reasonably traverse objects relating to a particular subject.
 * This class uses SPARQL DESCRIBE queries to return BSCmodels.
 * <p/>
 * Sample usage:
 * VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(
 * OntModelSpec.RDFS_MEM,
 * "<http://biocode.berkeley.edu/events/91>",
 * null,
 * QueryType.descendents);
 * q.configure(sm);
 * q.connect();
 */
public class VirtuosoBSCFilteredModel extends BSCModel implements FilteredModel, Configurable {

    protected String define = "DEFINE input:same-as \"no\"\n" +
            "DEFINE input:inference \"http://biscicol.org/inference/property_rules1\"\n";
    //protected String define = "DEFINE input:same-as \"no\"\n";
    public String prefix;
    protected String subject = null;
    protected DateTime date = null;
    protected QueryType queryType = null;
    // Reference graphs
    protected String defaultFrom;
    protected String relations;
    protected String god;

    // Connection parameters
    protected VirtGraph graph;
    protected String from;
    private String user;
    private String pass;
    private String url;
    protected String mainGraph;

    SharedProperties props;

    /**
     * This is the default constructor
     *
     * @param ont
     * @param pSubject
     * @param pDate
     * @param pQueryType
     */
    public VirtuosoBSCFilteredModel(OntModelSpec ont, String pSubject, DateTime pDate, QueryType pQueryType) {
        super(ont);
        prefix = getPrefixString(this.getDefaultPrefixMap());
        setSubject(pSubject);
        setDateTime(pDate);
        this.queryType = pQueryType;
    }

    /**
     * Constructor for turning sameAs on for virtuoso
     * (this will slow down queries but is useful for testing)
     *
     * @param sameAs
     * @param ont
     * @param subject
     * @param date
     * @param queryType
     */
    public VirtuosoBSCFilteredModel(boolean sameAs, OntModelSpec ont, String subject, DateTime date, QueryType queryType) {
        this(ont, subject, date, queryType);

        if (sameAs) {
            define = "DEFINE input:same-as \"no\"\n";
        }
    }

    /**
     * Return a BSCObject from Virtuoso.  The QueryType determines the scope of the object, whether it is object plus
     * descendents, object plus ancestors, or all relations to this object.
     *
     * @return
     */
    public BSCObject getBSCObject() {
        this.connect();
        if (this.getQueryType().equals(QueryType.descendents)) {
            return getDescendentsAsModel().getBSCObject(subject);
        } else if (this.getQueryType().equals(QueryType.ancestors)) {
            return getAncestorsAsModel().getBSCObject(subject);
        } else if (this.getQueryType().equals(QueryType.relations)) {
            return getRelationsAsModel().getBSCObject(subject);
        } else if (this.getQueryType().equals(QueryType.siblings)) {
            // This is not yet tested ...
            return getSiblingsAsModel().getBSCObject(subject);
        } else {
            return null;
        }
    }

    /**
     * Filter all siblings and return as a BSCModel
     *
     * @return
     */
    public BSCModel getSiblingsAsModel() {
        String query = define + prefix;
        query += "DESCRIBE * \n";
        query += from;
        query += " WHERE  ";
        query += "{?sibling " + props.getDependsOn() + " <" + subject + "> . ";
        query += "?sibling " + props.getDependsOn() + " ?object . ";
        query += "FILTER (?sibling !=  <" + subject + ">) . ";
        query += "FILTER (?object !=  <" + subject + ">) . ";
        if (date != null) {
            query += "            OPTIONAL {\n";
            query += "                ?sibling " + props.getDateLastModified() + " ?date . \n";
            if (date != null) {
                query += "                FILTER( !bif:isNull(xsd:date(?date)) && xsd:date(?date)>=xsd:date(\"" + this.date + "\") )\n";
            }
            query += "            }\n";
        }
        query += " ?object a ?type\n";
        query += "}";
        //System.out.println(query);
        return describeQuery(query);
    }

    /**
     * Get objects declared the same as subject and return as a model.
     *
     * @return
     */
    public BSCModel getSameAsModel() {
        String query = define + prefix;
        query += "DESCRIBE * \n";
        query += from;
        query += " WHERE {?object " + props.getSameas() + " ?sameAs . ";
        query += " FILTER (?object=" + subject + ")\n";
        query += "}";
        return describeQuery(query);
    }

    /**
     * Get all relations by adding ancestors and descendents
     * to each other
     *
     * @return
     */
    public BSCModel getRelationsAsModel() {
        BSCModel a = describeQuery(buildAncestorsDescendents(QueryType.ancestors));
        BSCModel d = describeQuery(buildAncestorsDescendents(QueryType.descendents));
        //BSCModel s = getSiblingsAsModel();

        a.add(d);
        //a.add(s);
        this.model = a.getModel();
        this.queryType = QueryType.relations;
        return this;
    }

    /**
     * Filter ancestors of subject
     *
     * @return
     */
    public BSCModel getAncestorsAsModel() {
        this.queryType = QueryType.ancestors;
        return describeQuery(buildAncestorsDescendents(QueryType.ancestors));
    }

    /**
     * Filter descendents of subject
     *
     * @return
     */
    public BSCModel getDescendentsAsModel() {
        this.queryType = QueryType.descendents;
        return describeQuery(buildAncestorsDescendents(QueryType.descendents));
    }

    /**
     * Build SPARQL syntax for constructing ancestor or dependent models.
     *
     * @param type
     * @return
     */
    private String buildAncestorsDescendents(QueryType type) {
        String query = "";
        query += define + prefix;
        query += "DESCRIBE * \n";
        query += from;
        query += "  WHERE\n";
        query += "    {\n";
        query += "     GRAPH <" + mainGraph + "> {\n";
        query += "      {\n";
        query += "        SELECT ?s ?p ?o \n";
        query += "        WHERE\n";
        query += "          {\n";
        if (type.equals(QueryType.ancestors)) {
            //query += "            ?o <" + props.getDependsOn() + "> ?s \n";
            query += "            ?o ?p ?s \n";
            //query += "            ?o <http://biscicol.org/terms/index#relies> ?s \n";
        } else {
            //query += "            ?s <" + props.getDependsOn() + "> ?o \n";
            query += "            ?s ?p ?o \n";
            //query += "            ?s <http://biscicol.org/terms/index#relies> ?o \n";
        }
        query += "            OPTION (TRANSITIVE,t_distinct, t_in(?s), t_out(?o), t_min (0), t_max (20), t_step ('step_no') as ?dist) .\n";
        query += "          }\n";
        query += "      }\n";
        query += "      FILTER (?s=<" + subject + ">) .\n";
        //query += "      FILTER(?p=<http://biscicol.org/terms/index.html#depends_on>) .\n";
        query += "    }\n";
        if (date != null) {
            query += "    ?object " + props.getDateLastModified() + " ?date . \n";
            query += "    ?object a ?type . \n";
            query += "    FILTER( !bif:isNull(xsd:date(?date)) && xsd:date(?date)>=xsd:date(\"" + this.date + "\") )\n";
        }
        query += "    }";


        query = define + prefix;
        query += "DESCRIBE * \n";
        query += from;
        query += "  WHERE\n";
        //query += "   {<" + subject + "> bsc:depends_on ?p}";
        query += "   {?s ?p <" + subject + ">}";
        System.out.println(query);

        return query;
    }


    /**
     * The describeQuery method overrides the BSCModel describeQuery and uses VirtuosoQueryExecution
     * since this implements some features not available in the standard QueryExecutionFactory
     * (e.g. DEFINE "sameAs")
     *
     * @param query
     * @return
     */
    public BSCModel describeQuery(String query) {

        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, graph);
        // The SLOW step
        Results r = new Results();
        this.model = vqe.execDescribe(ontModel);
        r.closeAndPrint("execDescribe executed");

        return this;
    }

    /**
     * Return prefixMapping as String
     *
     * @return
     */
    private String getPrefixString(Map<String, String> map) {
        String retValue = "";


        ArrayList<String> values = new ArrayList<String>();
        values.addAll(map.values());

        for (String v : values) {
            for (String k : map.keySet()) {
                if (map.get(k) == v) { // which have this value
                    retValue += ("prefix " + k + ":<" + v + ">\n");
                }
            }
        }
        return retValue;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getSubject() {
        return subject.replaceAll("[<>]", "");
    }

    public DateTime getDate() {
        return date;
    }

    public void setDateTime(DateTime date) {
        this.date = date;
    }

    public void setSubject(String pSubject) {
        subject = pSubject.trim();
        subject = subject.replaceAll("[<>]", "");
    }

    /**
     * Configure settings from SettingsManager
     *
     * @param sm The SettingsManager this object should use to configure itself.
     */
    public void configure(SettingsManager sm) {
        configure(sm, sm.retrieveValue("mainGraph"));
    }

    public void configure(SettingsManager sm, String strMainGraph) {
        user = sm.retrieveValue("virt_user", user);
        pass = sm.retrieveValue("virt_pass", pass);
        url = sm.retrieveValue("virt_url", url);

        props = SharedPropertiesFactory.getSharedProperties(ontModel);

        mainGraph = strMainGraph;
        setGod(sm.retrieveValue("virt_objects", god));
        setRelations(sm.retrieveValue("virt_relations", relations));

        setFrom();
    }

    public void setGod(String god) {
        this.god = god;
        setFrom();
    }

    public void setRelations(String relations) {
        this.relations = relations;
        setFrom();
    }

    private void setFrom() {
        defaultFrom = relations;
        //from = "FROM <" + relations + "> \n";
        //from += "FROM <" + god + ">\n";
        from = "FROM <" + mainGraph + ">\n";
    }

    /**
     * Must call connect to create a database connection
     */
    public void connect() {
        // Creating NSPrefixes before building the VirtModel makes things run faster
        model.setNsPrefixes(getDefaultPrefixMap());

        graph = new VirtGraph(url, user, pass);

        Reasoner reasoner = null;
        if (ontModel != null) reasoner = ontModelSpec.getReasoner();

        if (reasoner != null) {
            Model vm = VirtModel.openDatabaseModel(defaultFrom, url, user, pass);
            model = ModelFactory.createInfModel(reasoner, vm);
        } else {
            model = ModelFactory.createModelForGraph(graph);//.createRDFSModel(vm);
        }
    }

    public String getUrl() {
        return "url";
    }

}
