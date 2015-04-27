package BSCcore;


import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.iri.IRIFactory;


import java.util.HashMap;


/**
 * Base class for all BiSciCol model types.
 */
public class BSCModel {
    protected Model model;          // Model that we use for storing data, loaded from ontModel
    protected InfModel ontModel;       // Model class to store Reasoner Logic
    protected OntModelSpec ontModelSpec;
    private String defaultURI;

    //public BSCModel() {
    //this(OntModelSpec.OWL_DL_MEM); // no reasoning
    //this(OntModelSpec.OWL_DL_MEM_RULE_INF); // runs out of memory...
    //this(OntModelSpec.OWL_MEM_MICRO_RULE_INF);  // doesn't follow sameAs
    //this(PelletReasonerFactory.THE_SPEC); // pellet reasoner (works best)
    //}

    SettingsManager sm = SettingsManager.getInstance();

    /**
     * Constructs a new, empty BSCModel with the default prefix mapping.
     *
     * @param ont The Jena OntModelSpec to use in constructing the model.
     */
    public BSCModel(OntModelSpec ont) {
        ontModelSpec = ont;
        // Load Reasoner

        ontModel = ModelFactory.createOntologyModel(ont);
        // Build prefix map before we do much else (faster to build while its empty)
        ontModel.setNsPrefixes(getDefaultPrefixMap());
        model = ontModel;
        initSettings();
    }

    /**
     * Creates a new BSCModel from an existing Jena model.
     */
    public BSCModel(Model JENAmodel) {
        model = JENAmodel;
        initSettings();
    }

    /**
     * Initialize Settings.  This is used to set the default defaultURI to handle non-URI Identifiers
     */
    private void initSettings() {
        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }
        defaultURI = sm.retrieveValue("defaultURI", "urn:x-biscicol:");
    }

    /**
     * Get the Jena model used for this BSCModel.
     *
     * @return The Jena model for this BSCModel.
     */
    public Model getModel() {
        return model;
    }

    /**
     * Get the Jena inferencing model used for this BSCModel.
     *
     * @return The Jena model for this BSCModel.
     */
    public InfModel getOntModel() {
        return ontModel;
    }

    /**
     * Use the given Jena model for this BSCModel.  This will replace any
     * previously-loaded data.
     *
     * @param model A Jena model object.
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Add data from another model object to this model.  Redundant objects in
     * the two models will only be represented once in the combined model.
     *
     * @param m2 A BSCModel object.
     */
    public void add(BSCModel m2) {
        this.model = this.getModel().add(m2.getModel());
    }

    /**
     * Retrieve a single BSC object from the model.  The URI for the object
     * can be specified as either a full URI or with a short-form prefix.
     *
     * @param URIstring The URI for an object to get from the model.
     * @return The specified object if it exists, null otherwise.
     */
    public BSCObject getBSCObject(String URIstring) {


        Resource obj = model.createResource(model.expandPrefix(URIstring));

        // **** Begin Temp Section ****
        // Test if this resource object is a valid Object, if not, then we can try with a urn: prefix
        // TODO: verify if we really want to run this test every time.  Assuming we generate valid URIs from
        // triplifier we may not need the overhead here.
        IRIFactory iriFactory = IRIFactory.jenaImplementation();
        boolean includeWarnings = false;
        IRI iri;
        iri = iriFactory.create(URIstring); // always works
        if (iri.hasViolation(includeWarnings)) {
            //System.out.println("violoation! trying " + defaultURI + URIstring);
            obj = model.createResource(model.expandPrefix(defaultURI + URIstring));
        }
        // **** END Temp Section ****

        if (!model.containsResource(obj))
            return null;
        else
            return new BSCObject(obj, model);
    }

    /**
     * Get an iterator for all RDF statements in the model.  The is essentially
     * the same as listStatements() for Jena models, except that subjects and
     * objects of statements are returned as BSCObjects.  This is probably not
     * very useful in most cases because even simple models will include many
     * statements if they reference external RDF sources.
     *
     * @return An iterator for all model statements.
     */
    public RDFStatementIter getRDFStatements() {
        return new RDFStatementIter(model.listStatements(), model);
    }

    private QueryExecution genericQuery(String querystr) {
        com.hp.hpl.jena.query.Query query = QueryFactory.make();
        query.setPrefixMapping(model);

        QueryFactory.parse(query, querystr, null, Syntax.syntaxSPARQL);

        // set up the QueryExecution object
        QueryExecution qe = QueryExecutionFactory.create(query, model);

        return qe;
    }

    /**
     * Run a SELECT SPARQL query on the model.
     *
     * @param querystr A valid SPARQL SELECT query.
     * @return An iterator for the query results.
     */
    public QueryResult selectQuery(String querystr) {
        QueryExecution qe = genericQuery(querystr);

        return new QueryResult(qe, model);
    }

    /**
     * Run a SPARQL DESCRIBE query on the model and return a new BSCModel as the
     * result.
     *
     * @param querystr A valid SPARQL DESCRIBE query.
     * @return A BSCModel with the results of the query.
     */
    public BSCModel describeQuery(String querystr) {
        QueryExecution qe = genericQuery(querystr);
        Model newmod = qe.execDescribe();
        return new BSCModel(newmod);
    }

    /**
     * Builds and returns the default prefix map for BSC models.  Note that
     * RDF data sources can specify their own prefix mappings in addition to
     * the default mapping.
     *
     * @return The default prefix map.
     */
    public HashMap<String, String> getDefaultPrefixMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dwc", "http://rs.tdwg.org/dwc/terms/index.htm#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        map.put("bsc", "http://biscicol.org/terms/index.html#");
        map.put("owl", "http://www.w3.org/2002/07/owl#");
        map.put("biocode", "http://biocode.berkeley.edu/specimens/");
        map.put("biocodeevent", "http://biocode.berkeley.edu/events/");
        map.put("dcterms", "http://purl.org/dc/terms/");
        map.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("biocodeassembly", "http://biocode.berkeley.edu/assembly/");
        map.put("biocodelims", "http://biocode.berkeley.edu/lims/");
        map.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        map.put("flmnh", "http://collections.flmnh.ufl.edu/collection_object/");
        map.put("flmnhalias", "http://collections.flmnh.ufl.edu/collection_objects/alias/");
        map.put("collectorevents", "http://biocode.berkeley.edu/collectorevents/");
        return map;
    }
}
