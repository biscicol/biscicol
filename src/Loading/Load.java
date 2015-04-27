package Loading;

import BSCcore.FileBSCModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import virtuoso.jena.driver.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.StringTokenizer;


/**
 * Created by IntelliJ IDEA.
 * User: biocode
 * Date: Jul 14, 2011
 * Time: 3:19:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Load extends LoadProperties {

    /**
     * Controls data loading logic
     * propsFile defines key parameters.  Designed to be
     * called from command prompt on system with Virtuoso
     * Server installed
     * TODO: log progress in database or text file
     */
    public Load() {

        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }
        configure(sm);

        int status = NORMAL;
        //Model model = buildVirtuosoModel(user, pass);

        // Main table -- Load Data
        if (status == NORMAL && (option == ALL || option == MAIN))
            status = buildMain();

        /*
        // Relations Table -- Dump to File, Housekeeping, & Load File
        if (status == NORMAL && (option == ALL || option == RELATIONS)) {
            if (status == NORMAL && !buildRelationsFile(model).printSuccess())
                status = RELATIONS_LOAD_ERROR;

            if (status == NORMAL && !loadRelationsFile())
                status = RELATIONS_LOAD_ERROR;
        }

        // GOD Table -- Dump to File, Housekeeping, & Load File
        if (status == NORMAL && (option == ALL || option == GOD)) {
            model = buildVirtuosoModel(user, pass);
            if (status == NORMAL && !buildGodFile(model, godFile).printSuccess())
                status = GOD_LOAD_ERROR;

            if (status == NORMAL && !loadGodFile())
                status = GOD_LOAD_ERROR;
        }
        */

        System.exit(status);
    }

    /**
     * Main method here is important!
     * Designed to be run from command prompt
     *
     * @param args
     */
    public static void main(String args[]) {
/*
        if (args.length != 1) {
            System.out.println("Usage: Load {Load.properties} \n" +
                    "Copy the file Load.properties.template in the distribution to a file called Load.properties.\n" +
                    "Adjust the values in the file.\n" +
                    "You may wish to redirect output to a file to diagnose load behaviours.");
            System.exit(INVALID_ARGS_ERROR);
        } else {
            System.out.println("Initializing load script");
        }

        String propsFile = args[0];
*/
        Load l = new Load();
    }


    /**
     * Send an update statement via JDBC and return results object with message & status
     *
     * @param queryString
     * @return
     */
    private Results updateWithJDBC(String queryString) {
        Results r = new Results();
        Connection conn = null;
        try {
            //System.out.println("attempting: " + queryString);
            Class.forName("virtuoso.jdbc4.Driver");
            //Driver driver = DriverManager.getDriver(url);
            //if (driver instanceof virtuoso.jdbc4.Driver)
            //    System.out.println("    PASSED");
            conn = DriverManager.getConnection(url, user, pass);
            Statement st = conn.createStatement();
            st.execute(queryString);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            r.end();
            r.setSuccess(false);
            r.setMessage("Failed with updateJDBC for " + queryString + "(" + e.getMessage() + ")");
            return r;
        } catch (ClassNotFoundException e) {
            r.end();
            r.setSuccess(false);
            r.setMessage("Failed loading Driver");
            e.printStackTrace();
            return r;
        }
        r.end();
        r.setMessage("Success with updateJDBC for " + queryString);
        return r;
    }

    private static Results update(String queryString, Model model) {
        Results r = new Results();

        try {
            UpdateRequest request = UpdateFactory.create(queryString, Syntax.syntaxSPARQL_11);
            request.add(queryString);
            UpdateAction ua = new UpdateAction();
            ua.execute(request, GraphStoreFactory.create(model));
        } catch (Exception e) {
            r.end();
            r.setSuccess(false);
            r.setMessage("Failed with updateJDBC for " + queryString + "(" + e.getMessage() + ")");
            return r;
        }
        r.end();
        r.setMessage("success with update for " + queryString);
        return r;
    }


    private int buildMain() {
        Results r;

        if (!dropAndCreate(main))
            return MAIN_LOAD_ERROR;

        // Loop the list of graphs specified for loading
        while (graphs.hasNext()) {
            try {
                String graph = graphs.next().toString();
                String load = "DB.DBA.TTLP_MT_LOCAL_FILE ('" + graph + "', '', '" + mainGraph + "', 0)";
                r = updateWithJDBC(load);
                if (!r.printSuccess())
                    return MAIN_LOAD_ERROR;
            } catch (Exception e) {
                return MAIN_LOAD_ERROR;
            }
        }
        return NORMAL;
    }

    private boolean dropAndCreate(String graph) {
        Results r;
        String drop = "SPARQL DROP SILENT GRAPH " + graph;
        r = updateWithJDBC(drop);
        if (!r.printSuccess())
            return false;

        String create = "SPARQL CREATE GRAPH " + graph;
        r = updateWithJDBC(create);
        if (!r.printSuccess())
            return false;

        return true;
    }

    private boolean loadRelationsFile() {
        Results r;
        if (!dropAndCreate(relations))
            return false;

        // 2. Load file
        String load = "DB.DBA.TTLP_MT_LOCAL_FILE ('" + relationsFile + "', '', '" + relationsGraph + "', 0)";
        r = updateWithJDBC(load);
        if (!r.printSuccess())
            return false;

        return true;
    }


    private Model buildFileModel(String file, OntModelSpec ont) {
        FileBSCModel model = null;

        try {
            model = new FileBSCModel(file, ont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model.getModel();
    }

    private Model buildVirtuosoModel(OntModelSpec ont, String user, String pass) {
        Model model = VirtModel.openDatabaseModel(mainGraph, url, user, pass);
        return ModelFactory.createInfModel(ont.getReasoner(), model);
    }

    private Model buildVirtuosoModel(String user, String pass) {
        Model model = VirtModel.openDatabaseModel(mainGraph, url, user, pass);
        return ModelFactory.createModelForGraph(model.getGraph());
    }

    private Results loadGod() {
        VirtDataSource vds = new VirtDataSource(url, user, pass);

        Results r = new Results();
        try {
            String insert = "INSERT INTO GRAPH " + god + "{ ?s ?p ?o } \n" +
                    "WHERE {\n" +
                    " { GRAPH " + main + " \n" +
                    "       { ?s ?p ?o .\n" +
                    "  FILTER (?p= " + datelastmodified + " )\n" +
                    "}}}";
            System.out.println(insert);
            VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(insert, vds);
            vur.exec();

            insert = "INSERT INTO GRAPH " + god + "{ ?s ?p ?o} \n" +
                    "WHERE {\n" +
                    " { GRAPH " + main + " \n" +
                    "       { ?s ?p ?o .\n" +
                    "  FILTER (?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> && " +
                    "?s!= " + leadsto + " && " +
                    "?s != <http://www.w3.org/2002/07/owl#sameAs> )\n" +
                    "}}}";
            System.out.println(insert);

            vur = VirtuosoUpdateFactory.create(insert, vds);
            vur.exec();

        } catch (Exception e) {
            e.printStackTrace();
            r.end();
            r.setSuccess(false);
            r.setMessage("Failure in loading graph " + godGraph + ":" + e.getMessage());
            return r;
        }
        r.end();
        r.setMessage("Success in loading graph " + godGraph);
        return r;
    }

    private boolean loadGodFile() {
        Results r;

        if (!dropAndCreate(god))
            return false;

        // Load file
        String load = "DB.DBA.TTLP_MT_LOCAL_FILE ('" + godFile + "', '', '" + godGraph + "', 0)";
        r = updateWithJDBC(load);
        if (!r.printSuccess())
            return false;

        return true;
    }

    private Results buildGodFile(Model model, String filename) {

        Results r = new Results();

        try {
            // Create file
            PrintWriter out = new PrintWriter(new FileWriter(filename), true);

            // put the rdf prefix here at the top
            String prefix = "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .";
            out.println(prefix);

            // First get dateLastModified
            String queryString = "select ?s ?p ?o\n" +
                    "from " + main + "\n" +
                    "where {\n" +
                    "        ?s ?p ?o .\n" +
                    "  FILTER (?p= " + datelastmodified + " )\n" +
                    "}";

            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();

                    RDFNode s = soln.get("s");       // Get a result variable by name.
                    RDFNode p = soln.get("p");       // Get a result variable by name.
                    Literal o = soln.getLiteral("o");   // Get a result variable - must be a literal
                    String line = "<" + s.toString() + "> <" + p.toString() + "> \"" + o.toString() + "\" .";
                    out.println(line);
                }
            } finally {
                qexec.close();
            }

            queryString = "select ?s ?p ?o\n" +
                    "from " + main + "\n" +
                    "where {\n" +
                    "        ?s ?p ?o .\n" +
                    "  FILTER (?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> && " +
                    "?s != " + leadsto + " && " +
                    "?s != <http://www.w3.org/2002/07/owl#sameAs> )\n" +
                    "}";
            query = QueryFactory.create(queryString);
            qexec = QueryExecutionFactory.create(query, model);
            try {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    RDFNode s = soln.get("s");       // Get a result variable by name.
                    RDFNode o = soln.get("o");       // Get a result variable by name.
                    String line = "<" + s.toString() + "> a <" + o.toString() + "> .";
                    out.println(line);
                }
            } finally {
                qexec.close();
            }

            out.close();
        } catch (Exception e) {//Catch exception if any
            r.end();
            r.setSuccess(false);
            r.setMessage("Error writing God file " + godFile + " :" + e.getMessage());
            return r;
        }
        r.end();
        r.setMessage("Success in building God File " + godFile);
        return r;
    }

    public Results buildRelationsFile2(Model model) {
        Results r = new Results();
        String sparql = "SELECT ?s ?p ?o FROM <http://www.biscicol.org/testbiscicol.rdf#> " +
                " WHERE {?s ?p ?o}";

        System.out.println(sparql);

        QueryExecution qexecCount = QueryExecutionFactory.create(sparql, model);
        ResultSet results = qexecCount.execSelect();
        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            System.out.println(soln.get("s"));
            System.out.println(soln.get("p"));
            System.out.println(soln.get("o"));
        }
        return r;
    }

    /**
     * Load Relations File by looping each subject
     * Inneficient but Virt only reasons sameAs accurately when doing one at a time.
     * TODO: bundle insert statements or use prepared statements?
     *
     * @param model
     */
    public Results buildRelationsFile(Model model) {
        Results r = new Results();

        VirtDataSource vds = new VirtDataSource(url, user, pass);

        // TODO: Explore difference of VirtuosoDataSource vs. VirtDataSource?
        //VirtuosoDataSource v = new VirtuosoDataSource();

        Results rStringCount = new Results();
        // Count the number of results
        String queryStringCount = "SELECT  count(distinct ?sameAs)  \n" +
                "FROM " + main + "\n" +
                "WHERE {\n" +
                "        {?sameAs " + leadsto + "  ?o } \n" +
                "        UNION\n" +
                "         { {?s " + sameas + " ?sameAs} UNION {?sameAs " + sameas + " ?s} }\n" +
                "}\n";
        Query queryCount = QueryFactory.create(queryStringCount, Syntax.syntaxARQ);
        QueryExecution qexecCount = QueryExecutionFactory.create(queryCount, model);
        ResultSet resultsCount = qexecCount.execSelect();
        QuerySolution solnCount = resultsCount.nextSolution();
        Literal o = solnCount.getLiteral("callret-0");
        Integer count = Integer.parseInt(o.getString());

        if (debug) {
            rStringCount.end();
            rStringCount.setMessage("count = " + count + "; query =" + queryStringCount);
            rStringCount.printSuccess();
        }

        Results rString = new Results();
        // Get the list of results
        String queryString = "SELECT  distinct ?sameAs \n" +
                "FROM " + main + "\n" +
                "WHERE {\n" +
                "        {?sameAs " + leadsto + "  ?o } \n" +
                "        UNION\n" +
                "         { {?s " + sameas + " ?sameAs} UNION {?sameAs " + sameas + " ?s} }\n" +
                "}\n";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        if (debug) {
            rString.end();
            rString.setMessage("getting list of results via query =" + queryString);
            rString.printSuccess();
        }

        // Setup a virtgraph for queries to pass into loop
        VirtGraph vg = new VirtGraph(url, user, pass);
        vg.setSameAs(true);

        PrintWriter w = null;
        try {
            w = new PrintWriter(new FileWriter(new File(relationsFile)));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        relationsWriter rwObj = new relationsWriter(conn, w, "bsc:leadsTo");
        rwObj.header();

        // Use a very simple loop example so it is easy to trap exceptions
        try {
            ResultSet results = qexec.execSelect();

            // Loop each object
            while (results.hasNext()) {
                try {
                    String sparql = null;
                    String subject = null;
                    QuerySolution soln = results.next();

                    if (soln != null) {
                        RDFNode s = soln.get("sameAs");
                        subject = s.toString();
                        sparql = "sparql DEFINE input:same-as \"yes\" " +
                                "SELECT ?s ?o FROM " + main + " \n" +
                                "where {?s " + leadsto + " ?o . " +
                                "FILTER (?s=<" + subject + ">)}";
                        rwObj.run(sparql);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            qexec.close();
            w.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            r.end();
            r.setSuccess(false);
            r.setMessage("Failure in building Relations file " + relationsFile + ":" + e.getMessage());
            return r;
        }
        r.end();
        r.setMessage("Success in building Relations file " + relationsFile);
        return r;

    }

    /**
     * Join Threads back to Parent
     *
     * @param t
     * @param counter
     * @param max
     * @return
     */

    private static int joinThreads(Thread[] t, int counter, int max) {
        if (counter >= max) {
            for (int i = 0; i < t.length; i++) {
                try {
                    t[i].join();
                    counter--;
                } catch (InterruptedException ignore) {
                }
            }
        }
        return counter;
    }

    public String insertObj(String subject) {
        String s = "SELECT ?s ?o FROM " + main + " \n" +
                "WHERE {" +
                "{{?s " + leadsto + " ?o} UNION " +
                "{{?s   <http://www.w3.org/2002/07/owl#sameAs> ?sameAs} UNION {?sameAs <http://www.w3.org/2002/07/owl#sameAs> ?s} . " +
                " ?sameAs " + leadsto + " ?o} . " +
                "FILTER (?s=<" + subject + ">) " +
                "}}\n";

        return s;
    }


    public String insertSub(String subject) {
        String s = "SELECT ?s ?o FROM " + main + " \n" +
                "WHERE {" +
                "{{?s " + leadsto + " ?o} UNION\n" +
                "        {{?s  " + sameas + " ?sameAs} UNION {?sameAs " + sameas + " ?s} .\n" +
                "          ?sameAs " + leadsto + " ?o}\n" +
                "    FILTER (?s=<" + subject + ">)\n" +
                "}}\n";
        return s;
    }

}
