package Loading;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * NOTE: this class is commented out mainly so i can take HerMIT.jar
 * file out of lib (its pretty big and don't want to lug it around).
 * I'm leaving this class in the Loading package as I found that the
 * HerMIT reasoner showed alot of promise for rapidly reasoning
 * sameAs relationships across an entire dataset.  However, I wasn't able
 * to get HerMIT to work in conjunction with Virtuoso (in an attempt to
 * reason across the entire set)

 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 10-Dec-2007<br><br>
 * This example shows how to generate an ontology containing some inferred
 * information.
 */
public class hermitReasoner {


    public static void main(String[] args) {

    }

    public hermitReasoner(Model m) {
     /*

        try {
            // Create a reasoner factory.  In this case, we will use pellet, but we could also
            // use FaCT++ using the FaCTPlusPlusReasonerFactory.
            // Pellet requires the Pellet libraries  (pellet.jar, aterm-java-x.x.jar) and the
            // XSD libraries that are bundled with pellet: xsdlib.jar and relaxngDatatype.jar
            // make sure these jars are on the classpath
            OWLReasonerFactory reasonerFactory = null;
            // Uncomment the line below
//           reasonerFactory = new PelletReasonerFactory();


            // Load an example ontology - for the purposes of the example, we will just load
            // the pizza ontology.
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            //OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl"));
            OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("/Users/biocode/IdeaProjects/biscicol/sampledata/sameAsTest.nt"));


            // Load the Jena Model into an OntologyDocument by serializing.
            // elegant?  no.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            m.write(baos, "N3");
            InputStream bais = new ByteArrayInputStream(baos.toByteArray());


            // for some reason need to loop inputstream in order for below to work?
            BufferedReader in = new BufferedReader(new InputStreamReader(bais));
            String line = null;
            while ((line = in.readLine()) != null) {
                //  System.out.println(line);
            }

            man.loadOntologyFromOntologyDocument(bais);

            // Create the reasoner and classify the ontology
            //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
            Reasoner hermit = new Reasoner(ont);

            hermit.precomputeInferences(InferenceType.SAME_INDIVIDUAL);

            // To generate an inferred ontology we use implementations of inferred axiom generators
            // to generate the parts of the ontology we want (e.g. subclass axioms, equivalent classes
            // axioms, class assertion axiom etc. - see the org.semanticweb.owlapi.util package for more
            // implementations).
            // Set up our list of inferred axiom generators
            List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
            //gens.add(new InferredSubClassAxiomGenerator());
            gens.add(new InferredPropertyAssertionGenerator());

            // Put the inferred axioms into a fresh empty ontology - note that there
            // is nothing stopping us stuffing them back into the original asserted ontology
            // if we wanted to do this.
            OWLOntology infOnt = man.createOntology();

            // Now get the inferred ontology generator to generate some inferred axioms
            // for us (into our fresh ontology).  We specify the reasoner that we want
            // to use and the inferred axiom generators that we want to use.
            InferredOntologyGenerator iog = new InferredOntologyGenerator(hermit, gens);
            iog.fillOntology(man, infOnt);

            // Save the inferred ontology. (Replace the URI with one that is appropriate for your setup)
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            //man.saveOntology(infOnt, new OWLFunctionalSyntaxOntologyFormat(), IRI.create("file:///tmp/inferredont.owlapi"));
            man.saveOntology(infOnt, new RDFXMLOntologyFormat(), baos);
            System.out.println(baos.toString());
        }
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        */
    }
}
