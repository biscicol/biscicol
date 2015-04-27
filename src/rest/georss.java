package rest;

import BSCcore.SettingsManager;
import BSCcore.VirtuosoBSCFilteredModel;
import BSCcore.QueryType;
import Render.KMLRenderer;
import Render.TextRenderer;
import com.hp.hpl.jena.ontology.OntModelSpec;
import org.joda.time.DateTime;
import util.BSCDate;
import util.EncodingUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This service renders maps and returns XML to the client.
 * This is usually formatted as KML for display in Google Maps or Google Earth
 */
@Path("v2/georss")
public class georss {

    static OntModelSpec ontModelSpec; // spec used to build models 
    static SettingsManager sm;
	
    /**
     * Load settings manager, set ontModelSpec. 
     */
	static {
		sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
            String omsSetting = sm.retrieveValue("search_ontModelSpec", "OWL_LITE_MEM_RULES_INF");
    		ontModelSpec = (OntModelSpec)OntModelSpec.class.getField(omsSetting).get(null);
        } catch (Exception e) {e.printStackTrace();}
	}
	

    @GET
    @Produces("text/xml")
    public String getRelations(
            @javax.ws.rs.QueryParam("id") String pSubject,
            @javax.ws.rs.QueryParam("distance") String pDistance,
            @javax.ws.rs.QueryParam("searchType") String pSearchType,
            @javax.ws.rs.QueryParam("dateLastModified") String pDateLastModified
    ) {


          pSubject = EncodingUtil.decodeURIComponent(pSubject);
        //pCollection = EncodingUtil.decodeURIComponent(pCollection);
        pDateLastModified = EncodingUtil.decodeURIComponent(pDateLastModified);
        Integer intDistance = Integer.parseInt(EncodingUtil.decodeURIComponent(pDistance));
        pSearchType = EncodingUtil.decodeURIComponent(pSearchType);


        if (intDistance == null || intDistance < 1) {
            intDistance = 99;
        }



        TextRenderer ren = new KMLRenderer(intDistance);

        // *****************************************
        // Date Filtering
        // *****************************************
        DateTime dt = null;

        if (pDateLastModified != "") {
            if (pDateLastModified.equals("lastday")) dt = BSCDate.lastDay();
            if (pDateLastModified.equals("lastweek")) dt = BSCDate.lastWeek();
            if (pDateLastModified.equals("lastmonth")) dt = BSCDate.lastMonth();
            if (pDateLastModified.equals("lastyear")) dt = BSCDate.lastYear();

            ren.setDateFilter(dt);  // Date Filter on Rendering Class
        }

        // *****************************************
        // Construct the Query against Virtuoso
        // NOTE: interesting to have user pass in the reasoner they want to user here
        // *****************************************
        VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(OntModelSpec.RDFS_MEM, pSubject, dt, QueryType.relations);
        q.configure(sm);
        q.connect();

        // For mapping applications, just search ALL relations
        VirtuosoBSCFilteredModel m = (VirtuosoBSCFilteredModel)q.getRelationsAsModel();
        // Use the renderAll method
        return ren.renderAll(m.getBSCObject(pSubject));



/*        return "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n" +
               "<feed xmlns=\"http://www.w3.org/2005/Atom\"\n" +
               "      xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
               "      xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\n" +
               "      xmlns:georss=\"http://www.georss.org/georss\"\n" +
               "      xmlns:woe=\"http://where.yahooapis.com/v1/schema.rng\"\n" +
               "      xmlns:flickr=\"urn:flickr:\"\n" +
               "      xmlns:media=\"http://search.yahoo.com/mrss/\">\n" +
               "\n" +
               "  <title>BiSciCol GeoRSS</title>\n" +
               "  <subtitle>A test implementation of the BiSciCol GeoRSS</subtitle>\n" +
               "  <updated>2011-05-25T15:42:35Z</updated>\n" +
               "  <generator uri=\"http://www.biscicol.org/\">BiSciCol</generator>\n" +
               "\n" +
               "  <entry>\n" +
               "    <title>subject</title>\n" +
               "    <depth>2</depth>\n" +
               "    <id>subject</id>\n" +
               "    <DateLastModified>2006-08-29T15:42:35Z</DateLastModified>\n" +
               "    <content type=\"html\">Here is some information</content>\n" +
               "    <georss:point>40.746029 -73.979165</georss:point>\n" +
               "    <geo:lat>40.746029</geo:lat>\n" +
               "    <geo:long>-73.979165</geo:long>\n" +
               "    <woe:woeid>23511899</woe:woeid>\n" +
               "  </entry>\n" +
               "</feed>";
  */

          
        /*HTMLObjectRenderer hRenderer = new HTMLObjectRenderer();

        pSubject = EncodingUtil.decodeURIComponent(pSubject);
        pFrom = EncodingUtil.decodeURIComponent(pFrom);

        VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(pFrom);
        q.setSubject(pSubject);
        // Run the filter (in this case, get all descendents and populate a BSCModel)
        BSCModel m = q.getDescendentsAsModel();
        BSCObject obj = m.getBSCObject(pSubject);

        // Recursive function to get all children/children/etc
        BSCObjDescIter desciter = obj.getDescendants(3);
        return hRenderer.renderObjectsHeader() + hRenderer.renderObjects(desciter) ;
        */
    }
}
