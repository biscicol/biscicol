package BSCcore;

import Render.JSONRendererJiT;
import Render.SimpleTextRenderer;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.joda.time.DateTime;
import org.postgresql.core.Query;

import java.sql.*;

/**
 * Filter results from Postgres.
 * This hits a postgres database setup as an SDB Jena structure but it does not
 * use Jena SDB to get results.  Instead it uses custom iterators to build responses.
 */
public class postgresBSCFilteredModel extends BSCModel implements FilteredModel, Configurable {
    static SettingsManager sm = SettingsManager.getInstance();

    Integer integerRelated_to = -999999;
    Integer integerDepends_on = -999999;
    Integer integerDerives_from = -999999;
    Integer integerAlias_of = -999999;
    Integer integerDigest = -999999;
    String digest = "http://rdvocab.info/RDARelationshipsWEMI/digest";
    Connection conn = null;
    int currDepth = 1;
    int maxDepth;
    protected String subject = null;
    protected DateTime date = null;
    protected QueryType queryType = null;


    public postgresBSCFilteredModel(int maxDepth, Model model, String subject, QueryType queryType) throws SQLException, ClassNotFoundException {
        super(model);

        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }
        SharedProperties props = new SharedProperties(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM));

        Class.forName(sm.retrieveValue("pgsql_class"));
        conn = DriverManager.getConnection(
                sm.retrieveValue("pgsql_url"),
                sm.retrieveValue("pgsql_user"),
                sm.retrieveValue("pgsql_pass"));
        Statement stmt = conn.createStatement();

        //Get Relationship Operators
        String sql;
        sql = "SELECT id,lex \n" +
                "FROM knodes \n" +
                "WHERE lex = '" + props.getDependsOn() + "'\n" +
                "OR lex ='" + props.getRelatedTo() + "'\n" +
                "OR lex = '" + props.getDerivesFrom() + "'\n" +
                "OR lex = '" + props.getAlias_of() + "'\n" +
                "OR lex = '" + digest + "'";
        ResultSet rs = stmt.executeQuery(sql);

        String id, lex;
        while (rs.next()) {
            //Retrieve by column name
            id = rs.getString("id");
            lex = rs.getString("lex");
            if (rs.getString("lex").trim().equals(props.getRelatedTo().toString())) {
                this.integerRelated_to = rs.getInt("id");
            } else if (rs.getString("lex").trim().equals(props.getDependsOn().toString())) {
                this.integerDepends_on = rs.getInt("id");
            } else if (rs.getString("lex").trim().equals(props.getDerivesFrom().toString())) {
                this.integerDerives_from = rs.getInt("id");
            } else if (rs.getString("lex").trim().equals(props.getAlias_of().toString())) {
                this.integerAlias_of = rs.getInt("id");
            } else if (rs.getString("lex").trim().equals(digest)) {
                this.integerDigest = rs.getInt("id");
            }
        }
        rs.close();
        this.maxDepth = maxDepth;
        this.subject = subject;
        this.queryType = queryType;
    }

    public postgresBSCFilteredModel(Model JENAmodel) {
        super(JENAmodel);
    }


    /**
     * Recursive function to get children
     *
     * @param idValue
     * @param depth
     * @throws SQLException
     */
    public void getChildren(int idValue, int depth) throws SQLException {
        // Print the attributes of this node
        getAttributes(idValue);
        if (depth <= maxDepth) {
            // Query for this particular subject
            String sql =
                    "SELECT\n" +
                            "    ns.lex as subject,\n" +
                            "    ns.id as idSubject,\n" +
                            "    np.lex as predicate,\n" +
                            "    np.id as idPredicate,\n" +
                            "    no.lex as object,\n" +
                            "    no.id as idObject\n" +
                            "FROM quads t\n" +
                            "    Join knodes ns On (ns.id = t.s)\n" +
                            "    Join knodes np On (np.id = t.p)\n" +
                            "    Join knodes no On (no.id = t.o)\n" +
                            "WHERE \n";
            if (queryType.equals(QueryType.ancestors)) {
                sql += "    t.o = " + idValue + "\n";
            } else {
                sql += "    t.s = " + idValue + "\n";
            }
            sql += "   AND (t.p = " + integerRelated_to + " OR t.p = " + integerDepends_on + " OR t.p = " + integerDerives_from + " OR t.p = " + integerAlias_of + ")" +
                    " LIMIT 10";
            //System.out.println(sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                //Retrieve by column name
                String s = rs.getString("subject");
                String p = rs.getString("predicate");
                String o = rs.getString("object");
                Integer sid = rs.getInt(("idSubject"));
                Integer oid = rs.getInt(("idObject"));
                Integer pid = rs.getInt(("idPredicate"));

                model.add(makeStatement(s, p, o));

                // Call this function with current object to become subject
                if (queryType.equals(QueryType.ancestors)) {
                    getChildren(sid, depth);
                } else {
                    getChildren(oid, depth);

                }
            }
            rs.close();
            stmt.close();
            depth++;
        }
    }

    /**
     * Close Connection
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        conn.close();
    }

    /**
     * Get anything that is an attribute (not a relationship).
     * Any results are sent to model
     * This is non-recursive... only meant to get attributes of found nodes
     */
    public void getAttributes(int subject) throws SQLException {
        String sql =
                "SELECT\n" +
                        "    ns.lex as subject,\n" +
                        "    np.lex as predicate,\n" +
                        "    no.lex as object\n" +
                        "FROM quads t\n" +
                        "    Join knodes ns On (ns.id = t.s)\n" +
                        "    Join knodes np On (np.id = t.p)\n" +
                        "    Join knodes no On (no.id = t.o)\n" +
                        "WHERE \n" +
                        "    t.s = " + subject + "\n" +
                        "    AND (t.p != " + integerRelated_to + " AND t.p != " + integerDepends_on + " AND t.p != " + integerDerives_from + " AND t.p != " + integerAlias_of + ")\n" +
                        "    AND t.p != " + integerDigest +
                        " LIMIT 10";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            //Retrieve by column name
            String s = rs.getString("subject");
            String p = rs.getString("predicate");
            String o = rs.getString("object");
            model.add(makeStatement(s, p, o));
        }
    }

    /**
     * Make statements for Jena
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    private com.hp.hpl.jena.rdf.model.Statement makeStatement(String s, String p, String o) {
        return model.createStatement(
                model.createResource(s),
                model.createProperty(p),
                model.createResource(o));
    }

    /**
     * Lookup an integer ID IN nodes table to get started.
     *
     * @param id
     * @return
     * @throws SQLException
     */
    private int lookupID(String id) throws SQLException {
        String sql = "select id from knodes where lex = '" + id + "'";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        return rs.getInt("id");
    }

    /**
     * Get All Descendents
     *
     * @param id
     */
    public void getDescendents(String id) throws SQLException {
        getChildren(lookupID(id), currDepth);
    }


    public void configure(SettingsManager sm) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCObject getBSCObject() {
        try {
            this.getDescendents(subject);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return this.getBSCObject(subject);
    }

    public BSCModel getSiblingsAsModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCModel getSameAsModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCModel getRelationsAsModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCModel getAncestorsAsModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BSCModel getDescendentsAsModel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getSubject() {
        return subject;
    }

    public DateTime getDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDateTime(DateTime date) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSubject(String subject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public static void main(String args[]) {
        String id = "ark:/21547/Aa2_C200139E4AE8FE2346AE";
        id = "doi:10.5072/FK2MK6P5Z";
        //id = "ark:/21547/Ab2_F3EB93E896DF831525D4";
        postgresBSCFilteredModel s = null;
        QueryType queryType = QueryType.descendents;
        try {
            s = new postgresBSCFilteredModel(4, ModelFactory.createDefaultModel(), id, queryType);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            s.getDescendents(id);
            s.close();
            //System.out.println(s.model.toString());
            BSCObject obj = s.getBSCObject(id);
            JSONRendererJiT ren = new JSONRendererJiT();
            if (queryType.equals(QueryType.ancestors)) {
                System.out.println(ren.renderObjAncestors(obj));
            } else {
                System.out.println(ren.renderObject(obj));
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

}
