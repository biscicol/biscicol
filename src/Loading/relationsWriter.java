package Loading;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Control Threads/Queue for writing out relations file
 */
public class relationsWriter {
    PrintWriter w;
    String predicate = "";
    Connection conn;
    String output = "";

    public relationsWriter(Connection conn, PrintWriter w, String predicate) {
        this.conn = conn;
        this.predicate = predicate;
        this.w = w;
    }

    public void header() {
        w.println("@prefix bsc: <http://biscicol.org/biscicol.rdf#> .");
    }

    public void run(String sparql) {

        try {
            Statement st = conn.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sparql);
            while (rs.next()) {
                String output = "<" + rs.getString("s") + "> " + predicate + " <" + rs.getString("o") + "> .";
                if (output != null && output != "") {
                    w.println(output);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}






