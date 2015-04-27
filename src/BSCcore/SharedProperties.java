package BSCcore;

import com.hp.hpl.jena.rdf.model.*;


/**
 * Stores Jena property definitions for the common properties shared by all
 * BSCObjects.  By requesting SharedProperties objects from SharedPropertiesFactory,
 * BSCObjects from the same model can all share a single SharedProperties object,
 * thus eliminating the need to create the shared property definitions separately
 * for each BSCObject instance.
 */
public class SharedProperties {
    //private Property relativeOf, isSourceOf;//, comesFrom;
    //private Property relatedTo;
    private Property related_to,depends_on,derives_from,alias_of;

    private Property type;
    private Property dateLastModified;
    private Property agent;
    private Property geo;
    private Property sameas;

    /**
     * Constructs a new SharedProperties object with property definitions for the
     * specified model.
     *
     * @param m The model to create the property definitions for.
     */
    public SharedProperties(Model m) {
        SettingsManager sm;
        sm = SettingsManager.getInstance();

        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }

        type = m.createProperty(sm.retrieveValue("type"));

        related_to = m.createProperty(sm.retrieveValue("related_to"));
        depends_on = m.createProperty(sm.retrieveValue("depends_on"));
        alias_of = m.createProperty(sm.retrieveValue("alias_of"));
        derives_from = m.createProperty(sm.retrieveValue("derives_from"));

        dateLastModified = m.createProperty(sm.retrieveValue("datelastmodified"));
        agent = m.createProperty(sm.retrieveValue("agent"));
        geo = m.createProperty(sm.retrieveValue("geo"));
        sameas = m.createProperty(sm.retrieveValue("sameas"));
    }

    /**
     * Get the "Agent" property definition for the underlying model.
     *
     * @return The "Agent" property definition.
     */
    public Property getAgent() {
        return agent;
    }

    /**
     * Get the "DateLastModified" property definition for the underlying model.
     *
     * @return The "DateLastModified" property definition.
     */
    public Property getDateLastModified() {
        return dateLastModified;
    }

    /**
     * Get the "SpatialThing" property definition for the underlying model.
     *
     * @return The "SpatialThing" property definition.
     */
    public Property getGeo() {
        return geo;
    }

    /**
     * Get the "derives_from" property definition for the underlying model.
     *
     * @return The "derives_from" property definition.
     */
    public Property getDerivesFrom() {
        return derives_from;
    }

    /**
     * Get the "alias_of" property definition for the underlying model
     *
     * @return The "alias_of" property definition
     */
    public Property getAlias_of() {
        return alias_of;
    }

    /**
     * Get the "related_to" property definition for the underlying model.
     *
     * @return The "related_to" property definition.
     */
    public Property getRelatedTo() {
        return related_to;
    }

    /**
     * Get the "depends_on" property definition for the underlying model.
     *
     * @return The "depends_on" property definition.
     */
    public Property getDependsOn() {
        return depends_on;
    }

    /**
     * Get the "type" property definition for the underlying model.
     *
     * @return The "type" property definition.
     */
    public Property getType() {
        return type;
    }

    /**
     * Get the "dcterms:relation" property definition for the underlying model.
     *
     * @return The "relation" property definition.
     */
    /*public Property getRelation() {
        return relation;
    }*/

    public Property getSameas() {
        return sameas;
    }
}
