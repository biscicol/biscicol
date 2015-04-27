package rest;

import BSCcore.*;
import Render.JSONRendererJiT;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("JiT")
public class query {

    static OntModelSpec ontModelSpec; // spec used to build models 
    static SettingsManager sm;
    @Context
    static ServletContext context;

    /**
     * Load settings manager, set ontModelSpec. 
     */
    static {
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
            String omsSetting = sm.retrieveValue("search_ontModelSpec", "OWL_LITE_MEM_RULES_INF").trim();
            ontModelSpec = (OntModelSpec) OntModelSpec.class.getField(omsSetting).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return JSON-JiT for query
     *
     * @return Model representation defined by outputType parameter.
     * @throws Exception
     */
    @GET
    // @Produces provides response content type, depending on request "Accept" header, defaults to the first one. 
    // IMPORTANT Google KmlLayer(url) does not seem to have "Accept" header, 
    //           but won't work for "text/html", so the default (first) has to be */xml
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
    public String getRelations(
            @QueryParam("guid") String guid,
            @QueryParam("querytype") String strQueryType,
            @QueryParam("graph") String graph) throws Exception {

        postgresBSCFilteredModel q;

        System.out.println("specifiedgraph="+graph+";guid="+guid+";querytype="+strQueryType);


        // Set the QueryType
        QueryType queryType;
        if (strQueryType == null) {
            queryType = QueryType.descendents;
        } else if (strQueryType.equalsIgnoreCase("ancestors")) {
            queryType = QueryType.ancestors;
        } else {
            queryType = QueryType.descendents;
        }

        // Construct the Model
        q = new postgresBSCFilteredModel(4, ModelFactory.createDefaultModel(), guid, queryType);
        //q = new VirtuosoBSCFilteredModel(OntModelSpec.RDFS_MEM, guid, null, queryType);
        /*if (graph == null || graph.equalsIgnoreCase("")) {
            q.configure(sm);
        } else {
            q.configure(sm,graph);
        }
        q.connect();
        */
        BSCObject obj = q.getBSCObject();

        // Construct empty response
        if (obj == null) {
            return "{\"id\":\"0\",\"name\":\"No matching values found on server\"}";
        }

        // Construct the JiT renderer
        JSONRendererJiT ren = new JSONRendererJiT();

        // Return an appropriate response based on QueryType
        if (q.getQueryType().equals(QueryType.ancestors)) {
            return ren.renderObjAncestors(obj);
        } else {
            return ren.renderObject(obj);
        }
    }

}