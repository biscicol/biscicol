package rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.sun.org.apache.bcel.internal.generic.F2D;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

import util.BSCDate;
import util.EncodingUtil;
import BSCcore.*;
import Render.*;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.util.FileUtils;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("search")
public class search {

    static OntModelSpec ontModelSpec; // spec used to build models 
    static SettingsManager sm;
	@Context static ServletContext context;
		
    /**
     * Load settings manager, set ontModelSpec. 
     */
	static {
		sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
            String omsSetting = sm.retrieveValue("search_ontModelSpec", "OWL_LITE_MEM_RULES_INF").trim();
    		ontModelSpec = (OntModelSpec)OntModelSpec.class.getField(omsSetting).get(null);
        } catch (Exception e) {e.printStackTrace();}
	}
	
    /**
     * Get real path of the uploads folder from context.
     * Needs context to have been injected before. 
     * 
     * @return Real path of the uploads folder with ending slash.
     */
	static String uploadPath() {
		return context.getRealPath("uploads") + File.separator;
	}
		
    /**
     * Get a BiSciCol model defined by parameters. 
     * 
     * @return Model representation defined by outputType parameter.
     * @throws Exception 
     */
    @GET
    // @Produces provides response content type, depending on request "Accept" header, defaults to the first one. 
    // IMPORTANT Google KmlLayer(url) does not seem to have "Accept" header, 
    //           but won't work for "text/html", so the default (first) has to be */xml
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_HTML})  
    public StreamingOutput getRelations( 
            @QueryParam("id") String subject,
            //@QueryParam("collection") String pCollection,
            @QueryParam("dateLastModified") String dateLastModified,
            @QueryParam("searchType") String searchType,
            @QueryParam("distance") String distance,
            @QueryParam("outputType") String outputType,
            @QueryParam("model") Set<String> models) throws Exception {
        System.out.println("using models: " + models);
        subject = EncodingUtil.decodeURIComponent(subject);
        //pCollection = EncodingUtil.decodeURIComponent(pCollection);
        dateLastModified = EncodingUtil.decodeURIComponent(dateLastModified);
        searchType = EncodingUtil.decodeURIComponent(searchType);
        Integer intDistance = distance == null ? null : Integer.parseInt(EncodingUtil.decodeURIComponent(distance));
        outputType = EncodingUtil.decodeURIComponent(outputType);

        if (intDistance == null || intDistance < 1) {
            intDistance = 99;
        }

        // *****************************************
        // Date Filtering
        // *****************************************
        DateTime dt = null;
        if ("lastday".equalsIgnoreCase(dateLastModified))
            dt = BSCDate.lastDay();
        else if ("lastweek".equalsIgnoreCase(dateLastModified))
            dt = BSCDate.lastWeek();
        else if ("lastmonth".equalsIgnoreCase(dateLastModified))
            dt = BSCDate.lastMonth();
        else if ("lastyear".equalsIgnoreCase(dateLastModified))
            dt = BSCDate.lastYear();
        else
            dt = null;


    	String uploadPath = uploadPath();
        QueryType qt = QueryType.valueOfWithDefault(searchType);
        BSCModel model = null;
        if (models.isEmpty() || models.contains("BiSciCol")) {
            models.remove("BiSciCol");
            // *****************************************
            // Construct the Query against Virtuoso
            // NOTE: interesting to have user pass in the reasoner they want to user here
            // *****************************************
            VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(ontModelSpec, subject, dt, qt);
            q.configure(sm);
            q.connect();


            // *****************************************
            // Set Query type, build Model from the Query
            // *****************************************
            if (qt.equals(QueryType.siblings))
                model = q.getSiblingsAsModel();
            else if (qt.equals(QueryType.ancestors))
                model = q.getAncestorsAsModel();
            else if (qt.equals(QueryType.relations))
                model = q.getRelationsAsModel();
            else
                model = q.getDescendentsAsModel();
        } else {
            // build new model from one of the uploaded files
            Iterator<String> iterator = models.iterator();
            model = new FileBSCModel(FileUtils.toURL(uploadPath + iterator.next()), ontModelSpec);
            iterator.remove();
        }

        // add the rest of the uploaded files to the model
        for (String file : models) {
        	model.add(new FileBSCModel(FileUtils.toURL(uploadPath + file), ontModelSpec));
        }

        // find the model subject, return empty output if not found
        BSCObject bscSubject = model.getBSCObject(subject);
        if (bscSubject == null) 
            return new XSLRenderer();


        // set xsl, use 'tree' if outputType is not recognized
        String xsl = XSLRenderer.hasXsl(outputType) ? outputType : "table";

        // render
        return new XSLRenderer(bscSubject, qt, dt, intDistance, xsl);
    }

    /**
     * Upload model from URL. 
     * 
     * @param urlString URL of file to be uploaded.
     * @return Name of the uploaded file created in uploads folder.
     * @throws Exception 
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // JSON (and not text/plain) output lets the client distinguish this from error 500 
    // (javascript cannot access non-XHR response status code)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadModel(@FormParam("url") String urlString,
                              @FormParam("lang") String lang) throws Exception {
    	// assume http if no protocol provided
    	if (!urlString.contains("://"))
    		urlString = "http://" + urlString;
    
    	// open URLConnection
    	URLConnection connection = new URL(urlString).openConnection();
        InputStream inputStream = connection.getInputStream();

        // try to read filename from Content-Disposition header
 /*       String fileName = connection.getHeaderField("Content-Disposition");

        if (fileName != null) {
            int start = fileName.indexOf("filename=\"") + 10;
            int end = fileName.indexOf("\"", start);
            fileName = fileName.substring(start, end);
        } 
        // if above fails, try to read filename from URL
        if (fileName == null || fileName.isEmpty())
            fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
        // if above fails, use some default filename

        if (fileName.isEmpty())
            fileName = "test.txt";
            */
        // NOTE: fileName DOES fail -- this is a lousy idea trying to figure it out, so just set it here
        String fileName = "uploadedfile.txt";

        /*
        // NOTE: Reader seems to crash with other formats aside from langN3??
        // Set default language to N3 unless it is something else we recognize
        if (!lang.equals("RDF/XML") && !lang.equals("TURTLE") && !lang.equals("N-TRIPLE")) {
            lang = FileUtils.langN3;
        }
        */

        fileName = writeModel(inputStream, FileUtils.langN3, fileName);
        // Jackson mapper is not, by default, used to serialize String, 
        // so it has to be done here (which just means wrapping in quotes)
        return "\"" + fileName + "\""; 
    }

    /**
     * Upload model from local file. 
     * 
     * @param inputStream File to be uploaded.
     * @param contentDisposition Form-data content disposition header.
     * @return Name of the uploaded file created in uploads folder.
     * @throws Exception 
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    // JSON (and not text/plain) output lets the client distinguish this from error 500 
    // (javascript cannot access non-XHR response status code)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadModel(
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition contentDisposition) throws Exception {
        // Jackson mapper is not, by default, used to serialize String, 
        // so it has to be done here (which just means wrapping in quotes)
        return "\"" + writeModel(inputStream, FileUtils.langN3, contentDisposition.getFileName()) + "\"";
    }
    
    /**
     * Check which model files exist in upload folder. 
     * 
     * @param models Names of model files to be checked.
     * @return Names of existing model files.
     * @throws Exception 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> checkModels(Set<String> models) {
    	String uploadPath = uploadPath();
    	Iterator<String> iterator = models.iterator();
    	while (iterator.hasNext())
	   		if (!new File(uploadPath + iterator.next()).exists())
	   			iterator.remove();
		return models;
    }

    /**
     * Write model file to disk, validate the model. 
     * 
     * @param inputStream Source of the file.
     * @param fileName Name of the file.
     * @return Name of the new file.
     * @throws Exception 
     */
    String writeModel(InputStream inputStream, String fileName, String lang) throws Exception {
        File file = createUploadFile(fileName);
        //System.out.println(fileName);
        ReadableByteChannel rbc = Channels.newChannel(inputStream);
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.close();
        fileName = file.getName();
        System.out.println("received: " + fileName);

        // validate uploaded file
        new FileBSCModel(file.toURI().toString(), lang, ontModelSpec);

        return fileName;
    }

    /**
     * Create new file in uploads folder, add incremental number to filename if filename already exists. 
     * 
     * @param fileName Name of the file.
     * @return The new file.
     */
    File createUploadFile(String fileName) {
        String path = uploadPath() + FilenameUtils.getBaseName(fileName);
        String ext = FilenameUtils.getExtension(fileName);
        File file = new File(path + "." + ext);
        int i = 1;
        while (file.exists())
            file = new File(path + "." + i++ + "." + ext);
        return file;
    }
}