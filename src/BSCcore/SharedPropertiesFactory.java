

package BSCcore;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.HashMap;


/**
 * Provides SharedProperties objects for use by BSCObjects.
 * SharedPropertiesFactory ensures that all BSCObjects from the same model
 * share a single instance of an appropriate SharedProperties object, thus
 * avoiding the cost of allocating potentially many thousands of identical
 * SharedProperties objects during long model traversals or searches.  In the
 * language of GoF design patterns, this acts like a flyweight factory.
 */
public class SharedPropertiesFactory {
    // keeps track of which models we've already allocated SharedProperties
    // objects for
    private static final HashMap<Model, SharedProperties> propsByModel = new HashMap<Model, SharedProperties>();

    /**
     * Returns a SharedProperties object for the specified model.  If a
     * SharedProperties object has already been created for this model, then a
     * reference to the previously-created object is returned.  Otherwise, a
     * new SharedProperties object is allocated.  Note that as currently
     * implemented, models are identified by their reference address; thus,
     * content-identical models located at different addresses will not share
     * SharedProperties objects.
     * 
     * @param model The RDF model for which to get the SharedProperties object.
     * @return A reference to the SharedProperties object.
     */
    public static SharedProperties getSharedProperties(Model model)
    {
        if (propsByModel.get(model) == null)
            propsByModel.put(model, new SharedProperties(model));
        //else
        //    System.out.println("**** REUSING SharedProperties ****");

        return propsByModel.get(model);
    }
}
