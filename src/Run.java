import BSCcore.*;
import Render.*;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.log4j.Level;
import org.joda.time.DateTime;
import util.BSCDate;

import java.sql.SQLException;


/**
 * @author stuckyb
 */
public class Run {
    static SettingsManager sm = SettingsManager.getInstance();

    static BSCModel buildFileModel(String file) {
        // create the model from the local RDF file
        FileBSCModel model = null;

        try {
            // The following do not do "sameAs" inferencing
            //
            //model = new FileBSCModel(file, OntModelSpec.DAML_MEM_RULE_INF);
            //model = new FileBSCModel(file, OntModelSpec.RDFS_MEM);
            //model = new FileBSCModel(file, OntModelSpec.RDFS_MEM_RDFS_INF);
            //model = new FileBSCModel(file, OntModelSpec.RDFS_MEM_TRANS_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_DL_MEM_TRANS_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_LITE_MEM_TRANS_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_MEM_MICRO_RULE_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_MEM_RDFS_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_MEM_TRANS_INF);

            // The following *do* do "sameAs" inferencing
            //model = new FileBSCModel(file, OntModelSpec.OWL_DL_MEM_RULE_INF);
            model = new FileBSCModel(file, OntModelSpec.OWL_LITE_MEM_RULES_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_MEM_MINI_RULE_INF);
            //model = new FileBSCModel(file, OntModelSpec.OWL_MEM_RULE_INF);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    static VirtuosoBSCFilteredModel buildVirtuosoModel(String subject, String date, QueryType queryType) {
        DateTime dt = BSCDate.parseDate(date);

        VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(OntModelSpec.RDFS_MEM, subject, dt, queryType);
        q.configure(sm);
        q.connect();

        return q;
    }

       static postgresBSCFilteredModel buildPostgresModel(String subject, QueryType queryType) {

           postgresBSCFilteredModel q = null;
           try {
               q = new postgresBSCFilteredModel(3, ModelFactory.createDefaultModel(), subject, queryType);
           } catch (SQLException e) {
               e.printStackTrace();
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
           q.configure(sm);

        return q;
    }

    private static void johnFilteredModelTest(FilteredModel q, TextRenderer ren) {

        //ren.setDateFilter(q.getDate());
        BSCObject obj = q.getBSCObject();

        if (obj == null) {
            System.out.println("obj is null");
        } else {
            if (q.getQueryType().equals(QueryType.descendents)) {
                System.out.println("rendering descendents ... " + obj.toString());
                System.out.println(ren.renderObject(obj));
            } else if (q.getQueryType().equals(QueryType.ancestors)) {
                System.out.println("rendering ancestors ... ");
                System.out.println(ren.renderObjAncestors(obj));
            } else if (q.getQueryType().equals(QueryType.relations)) {
                System.out.println("rendering all..");
                System.out.println(ren.renderAll(obj));
            } else if (q.getQueryType().equals(QueryType.siblings)) {
                // This is not yet tested ...
                System.out.println(ren.renderObjectList(obj.getSiblings()));
            } else {
            }
        }
    }

    private static void ukiVirtTest(String subject, QueryType queryType, String xsl) {
        BSCModel model = buildVirtuosoModel(subject, null, queryType);
        BSCObject bscSubject = model.getBSCObject(subject);
        DateTime dt = null;
        new XSLRenderer(bscSubject, queryType, dt, 10).write(System.out, xsl);
    }

    static void brianTest(BSCModel model) {
        QueryResult qres = null;
        BSCObject obj = null;
        System.out.println("in briantest");

        // print out all statements in the model
        /*System.out.println("\n-- first 100 model statements --");
        int cnt = 0;
        for (RDFStatement stmt : model.getRDFStatements()) {
            System.out.println(stmt.getSubject() + " " + stmt.getPredicate() + " " + stmt.getObject());
            cnt++;
            if (cnt == 100)
                break;
        }*/

        // run a SELECT query and print out all of the results
        //System.out.println("-- all specimens in the model --");
        //qres = model.selectQuery("SELECT ?specname WHERE { ?specname rdf:type dwc:specimen }");
        //for (QuerySolutionIter qs : qres) {
        //    for (BSCObject node : qs)
        //        System.out.println(node);
        //}

        // get a specific object from the model and print all of its children
        System.out.println("\n-- all children of biocodeevent:ID2 --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocodeevent:ID2");
        for (BSCObject child : obj.getChildren())
            System.out.println(child);


        // get a specific object from the model and print all of its descendants
        System.out.println("\n-- all descendants of biocodeevent.ID2 --");
        obj = model.getBSCObject("biocodeevent:ID2");
        BSCObjDescIter desciter = obj.getDescendents(3);
        for (BSCObject child : desciter) {
            System.out.print(desciter.getDepth());
            System.out.print(": ");
            System.out.println(child);
        }

        // get a specific object from the model and print its parents
        System.out.println("\n-- parents of biocode:ID3A --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocode:ID3A");
        //obj = model.getBSCObject("DL328");
        for (BSCObject parent : obj.getParents())
            System.out.println(parent);

        // get a specific object from the model and print its siblings
        System.out.println("\n-- siblings of biocode:ID3A --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocode:ID3A");
        for (BSCObject sib : obj.getSiblings())
            System.out.println(sib);

        // get a specific object from the model and print its relatives
        System.out.println("\n-- relatives of biocodeevent:ID1 --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocodeevent:ID1");
        for (BSCObject rel : obj.getDerivesFrom())
            System.out.println(rel);

        // now try adding a second model to see if combined model inferencing works
        //String basedir = System.getProperty("user.dir");
        //BSCModel newmod = buildFileModel("file://" + basedir + "/sampledata/test2.n3");
        //model.add(newmod);

        // print out all statements in the model
        /*System.out.println("\n-- first 2000 model statements --");
        int cnt = 0;
        for (RDFStatement stmt : model.getRDFStatements()) {
            System.out.println(cnt + ": " + stmt.getSubject() + " " + stmt.getPredicate() + " " + stmt.getObject());
            cnt++;
            if (cnt == 2000)
                break;
        }*/

        // run a SELECT query and print out all of the results
        System.out.println("\n-- all tissues in the model --");
        qres = model.selectQuery("SELECT ?specname WHERE { ?specname a bsc:Tissue }");
        for (QuerySolutionIter qs : qres) {
            for (BSCObject node : qs)
                System.out.println(node);
        }

/*        System.out.println("\n-- all children of biocodeevent:ID2 --");
obj = model.getBSCObject("biocodeevent:ID2");
for (BSCObject child : obj.getChildren())
System.out.println(child);

System.out.println("\n-- all children of biocodeevent:ID6 --");
obj = model.getBSCObject("biocodeevent:ID6");
for (BSCObject child : obj.getChildren())
System.out.println(child);*/


        //

/*        obj = model.getBSCObject("biocodeevent:ID2");

        System.out.println("\n-- biocodeevent:CE2 rendered as simple text --");
        TextRenderer ren = new SimpleTextRenderer();
        System.out.print(ren.renderObject(obj));

        System.out.println("\n-- biocodeevent:CE2 rendered as XML --");
        ren = new KMLRenderer(2);
        System.out.print(ren.renderObject(obj));

        System.out.println("\n-- biocodeevent:CE2 rendered as JSON --");
        ren = new JSONRenderer();
        System.out.print(ren.renderObject(obj));


        System.out.println("\n-- children of biocodeevent:CE2 rendered as simple text --");
        ren = new SimpleTextRenderer();
        System.out.print(ren.renderObjectList(obj.getChildren()));

        System.out.println("\n-- children of biocodeevent:CE2 rendered as XML --");
        ren = new KMLRenderer();
        System.out.print(ren.renderObjectList(obj.getChildren()));

        System.out.println("\n-- children of biocodeevent:CE2 rendered as JSON --");
        ren = new JSONRenderer();
        System.out.print(ren.renderObjectList(obj.getChildren()));


        System.out.println("\n-- biocode:extract31 and its ancestors rendered as simple text --");
        obj = model.getBSCObject("biocode:extract31");
        ren = new SimpleTextRenderer();
        System.out.print(ren.renderObjAncestors(obj));


        // get a specific object from the model and render it as a png image
        obj = model.getBSCObject("biocodeevent:CE2");
        ImageRenderer imgren = new ImageRenderer();
        imgren.renderObject(obj);
        imgren.outputAsPNG();
*/

        /*
        // get a specific object from the model and print all of its parents
        System.out.println("\n-- all parents of dwc:specimen --");
        obj = model.getBSCObject("dwc:occurrence");
        for (BSCObject parent : obj.getParents())
            System.out.println(parent);

        // get a specific object from the model and print all of its siblings
        System.out.println("\n-- all siblings of dwc:specimen --");
        obj = model.getBSCObject("dwc:occurrence");
        for (BSCObject sibling : obj.getSiblingsAsModel())
            System.out.println(sibling);

*/
        // run a DESCRIBE query
        BSCModel dres = model.describeQuery("DESCRIBE biocode:ID6A-2");

        // print out all of the statements in the resulting model
        System.out.println("\n-- describe results --");
        for (RDFStatement stmt : dres.getRDFStatements())
            System.out.println(stmt);


        SettingsManager sm;
        sm = SettingsManager.getInstance();

        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(sm.retrieveValue("virt_pass"));

        // create the Virtuoso-based model
        // VirtuosoBSCModel vmodel = new VirtuosoBSCModel(OntModelSpec.OWL_LITE_MEM_RULES_INF);
        // vmodel.configure(sm);
        // vmodel.connect();

        /*qres = model.selectQuery("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
        for (QuerySolutionIter qs : qres) {
            for (BSCObject node : qs)
                System.out.println(node);
        }

        // print out all statements in the virtuoso-based model
        System.out.println("\n-- first 100 virtuoso model statements --");
        int cnt = 0;
        for (RDFStatement stmt : vmodel.getRDFStatements()) {
            System.out.println(stmt.getSubject() + " " + stmt.getPredicate() + " " + stmt.getObject());
            cnt++;
            if (cnt == 100)
                break;
        }

        // get a specific object from the virtuoso model and print all of its children
        System.out.println("\n-- all children of biocode:MBIO2194 --");
        obj = vmodel.getBSCObject("biocode:MBIO2194");
        for (BSCObject child : obj.getChildren()) {
            if (!child.isLiteral()) {
                System.out.println(child + ":" + child.getType());

                for (BSCObject child2 : child.getChildren()) {
                    System.out.println("  -" + child2 + ":" + child2.getType());

                }

            }

        }
         */
    }

    private static void johnFileTest(BSCModel model) {
        QueryResult qres = null;
        JSONRenderer ren = new JSONRenderer();

        BSCObject obj = null;
        System.out.println("in johnFiletest");

        // run a SELECT query and print out all of the results
        //System.out.println("\n-- all samples in the model --");

        String sparql = "SELECT ?specname ?p ?o WHERE { ?specname a <http://purl.obolibrary.org/obo/OBI_0100051> ." +
                "?specname ?p ?o }";
        // Occurrences that have an identification instance
        sparql = "SELECT distinct ?occurrence \n" +
                "\tWHERE  {\n" +
                "\t\t?identification <http://biscicol.org/terms/index.html#depends_on> ?occurrence .\n" +
               //"\t\t?identification a <http://rs.tdwg.org/dwc/terms/Identification> .\n" +
                "\t\t?identification a <http://rs.tdwg.org/dwc/terms/Identification> .\n" +
                "\t\t?taxon <http://biscicol.org/terms/index.html#depends_on> ?identification .\n" +
                "\t\t?taxon a <http://rs.tdwg.org/dwc/terms/Taxon> .\n" +

                "\t} order by ?occurrence";
        /* sparql = "SELECT ?occurrence ?p ?o \n" +
                "\tWHERE  {\n" +
                "\t\t?occurrence ?p ?o .\n" +
                "\t}"; */
        System.out.println(sparql);

        qres = model.selectQuery(sparql);

        for (QuerySolutionIter qs : qres) {
            for (BSCObject node : qs)
                System.out.print(node + "\t");
            System.out.println("");
        }

        // Occurrences that are just related to taxon
        sparql = "SELECT distinct ?occurrence \n" +
                "\tWHERE  {\n" +
                "\t\t?occurrence <http://biscicol.org/terms/index.html#related_to> ?taxon .\n" +
                "\t\t?taxon a <http://rs.tdwg.org/dwc/terms/Taxon> .\n" +
                "\t} order by ?occurrence";
        System.out.println(sparql);

         qres = model.selectQuery(sparql);

        for (QuerySolutionIter qs : qres) {
            for (BSCObject node : qs)
                System.out.print(node + "\t");
            System.out.println("");
        }
        /*
        System.out.println("\n-- isSourceOf children of biocodeevent:ID3 --");
        obj = model.getBSCObject("biocodeevent:ID3");
        for (BSCObject child : obj.getChildren())
            System.out.println(child);

        // get a specific object from the model and print all of its descendants
        System.out.println("\n-- isSourceOf descendants of biocodeevent.ID3 --");
        obj = model.getBSCObject("biocodeevent:ID3");
        BSCObjDescIter desciter2 = obj.getDescendents(3);
        for (BSCObject child : desciter2) {
            System.out.print(desciter2.getDepth());
            System.out.print(": ");
            System.out.println(child);
        }

        System.out.println("\n-- isRelatedTo children from biocodeevent:ID3 --");
        obj = model.getBSCObject("biocodeevent:ID3");
        for (BSCObject child : obj.getTypedRelationChildren(new SharedProperties(model.getModel()).getRelatedTo()))
            System.out.println(child);

        // get a specific object from the model and print all of its descendants
        System.out.println("\n-- isRelatedTo descendants of biocodeevent.ID3 --");
        obj = model.getBSCObject("biocodeevent:ID3");
        BSCObjDescIter desciter = obj.getTypedRelatedDescendents(3, new SharedProperties(model.getModel()).getRelatedTo());
        for (BSCObject child : desciter) {
            System.out.print(desciter.getDepth());
            System.out.print(": ");
            System.out.println(child);
        }

        System.out.println("\n-- all relatives of biocodeevent:ID3  --");
        obj = model.getBSCObject("biocodeevent:ID3");
        for (BSCObject child : obj.getDerivesFrom())
            System.out.println(child);


        // get a specific object from the model and print its parents
        System.out.println("\n-- parents of biocode:ID3A --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocode:ID3A");
        //obj = model.getBSCObject("DL328");
        for (BSCObject parent : obj.getParents())
            System.out.println(parent);

        // get a specific object from the model and print its siblings
        System.out.println("\n-- siblings of biocode:ID3A --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocode:ID3A");
        for (BSCObject sib : obj.getSiblings())
            System.out.println(sib);

        // get a specific object from the model and print its relatives
        System.out.println("\n-- relatives of biocodeevent:ID1 --");
        //obj = model.getBSCObject("biocodeevent:ID2");
        obj = model.getBSCObject("biocodeevent:ID1");
        for (BSCObject rel : obj.getDerivesFrom())
            System.out.println(rel);

        // run a DESCRIBE query
        BSCModel dres = model.describeQuery("DESCRIBE biocodeevent:ID3");

        // print out all of the statements in the resulting model
        System.out.println("\n-- describe results --");
        for (RDFStatement stmt : dres.getRDFStatements())
            System.out.println(stmt);

        */
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.ERROR);

        String basedir = System.getProperty("user.dir");
        System.out.println(basedir);

        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }

        // NOTE: For building File Models, you may need to set your working directory manually in your IDE
        // In Intellij, this is done in "Edit Configurations"
        //brianTest(buildFileModel("file://" + basedir + "/sampledata/BeefTriplifierTest.n3"));
        //brianTest(buildFileModel("file://" + basedir + "/sampledata/biscicol-model4.n3"));
        //brianTest(buildFileModel("file://" + basedir + "/sampledata/test.n3"));
        //johnFileTest(buildFileModel("file://" + basedir + "/sampledata/test3.n3"));

        johnFileTest(buildFileModel("file://" + basedir + "/sampledata/triplifierPaperSample.ttl"));
        //VirtuosoBSCFilteredModel q = new VirtuosoBSCFilteredModel(OntModelSpec.RDFS_MEM, subject, date, queryType);
        //johnVirtTest(buildSesameModel("<doi:10.5072/FK2MK6P5Z>", null, QueryType.descendents), new SimpleTextRenderer());

        //johnVirtTest(buildVirtuosoModel("<ark:/21547/EF2_36A1854FEFE7FAF2D19A>", null, QueryType.descendents), new JSONRenderer2());
        //johnVirtTest(buildVirtuosoModel("ark:/21547/S2_91", null, QueryType.ancestors), new JSONRendererJiT());

        //johnFilteredModelTest(buildPostgresModel("ark:/21547/Aa2_FBCC7BCDA0E05832E2F6", QueryType.descendents),new JSONRendererJiT());
        //johnVirtTest(buildVirtuosoModel("ark:/21547/EF2_CA53A203EE0D86C5D7BE",null,QueryType.descendents),new JSONRenderer());
        //johnVirtTest(buildVirtuosoModel("<http://biocode.berkeley.edu/collectorevents/GP-Loc-862>"),new JSONRenderer());
        //johnVirtTest(buildVirtuosoModel("<http://collections.flmnh.ufl.edu/collection_objects/alias/UF:Mollusca:400666.rdf>",null,QueryType.descendents),new JSONRenderer());
        //ukiVirtTest("http://biocode.berkeley.edu/events/91", QueryType.siblings, "byType");
        //ukiVirtTest("http://collections.flmnh.ufl.edu/collection_objects/alias/UF:Mollusca:400666.rdf", QueryType.descendents, "byType");
        //ukiVirtTest("<http://biocode.berkeley.edu/specimens/MBIO2925>", QueryType.siblings, "");
        //johnVirtTest(buildVirtuosoModel(args[0],null),new JSONRenderer());
        //johnFileTest(buildFileModel("file://" + basedir + "/sampledata/biocode.all.n3"));        
    }


}


