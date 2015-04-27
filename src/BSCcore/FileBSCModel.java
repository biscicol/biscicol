package BSCcore;


import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;

/**
 * Allows BiSciCol models to be created directly from RDF text files.
 */
public class FileBSCModel extends BSCModel {

    /**
     * Construct a new FileBSCModel from the text RDF data at the specified
     * file URI and the given ontology model description. Guess the language
     * from file URI extension defaulting to "N3".
     *
     * @param file The URI of the RDF file to load.
     * @param ont  A Jena ontology model description.
     * @throws Exception
     */
    public FileBSCModel(String file, OntModelSpec ont) throws Exception {
        this(file, FileUtils.guessLang(file, FileUtils.langN3), ont);
    }

    /**
     * Construct a new FileBSCModel from the text RDF data (in the specified
     * language) at the specified file URI and the given ontology model
     * description.
     *
     * @param file The URI of the RDF file to load.
     * @param lang The language of the RDF data (N3, RDF/XML, etc.).
     * @param ont  A Jena ontology model description.
     * @throws Exception
     */
    public FileBSCModel(String file, String lang, OntModelSpec ont) throws Exception {
        super(ont);
        loadFile(file, lang);
    }

    /**
     * Read text RDF data and load it into the model.  Before parsing the RDF
     * data, this method will first attempt to verify that the file contains a
     * valid UTF-8 character stream and throws an exception if it does not.
     * This test is necessary because it appears that the Jena code that handles
     * reading Turtle and N3 data files does not properly handle UTF-8 stream
     * errors.  When a UTF-8 error is encountered, the Jena code will silently
     * fail and return an empty model as if the read had been successful.
     *
     * @param file The URI of the RDF file to load.
     * @param lang The language of the RDF data (N3, RDF/XML, etc.).
     * @throws Exception
     */
    public void loadFile(String file, String lang) throws Exception {
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            System.out.println(e);
        }

        // Assign urn: to any Resource that is not a valid URI
        String defaultURI = sm.retrieveValue("defaultURI", "urn:");

        // Use FileManager to create the input stream and verify that
        // the file exists.
        InputStream in = FileManager.get().open(file);
        if (in == null)
            throw new IOException("Error: file " + file + " not found.");

        // Verify that the file contains valid UTF-8 data.
        //
        // This appears to be necessary because the Jena TurtleReader classes
        // do not detect some (all?) UTF-8 stream errors and cause the Jena
        // model classes to read invalid files without reporting any exceptions.

        // Start by creating a properly-configured CharsetDecoder and
        // InputStreamReader.
        InputStreamReader isr;
        CharsetDecoder chdec = Charset.forName("UTF-8").newDecoder();
        chdec = chdec.onMalformedInput(CodingErrorAction.REPORT);

        isr = new InputStreamReader(in, chdec);

        // Attempt to read each character in the file, throwing any malformed
        // input exceptions on to the caller.
        try {
            while (isr.read() != -1)
                continue;
        } catch (MalformedInputException e) {
            throw new IOException("Wrong file format.");
        } finally {
            in.close();
        }

        // read the RDF file
        // TODO:
        ontModel.read(file, defaultURI, lang);
    }
}
