package com.sebastienguillemin;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;

import com.sebastienguillemin.util.CSVUtil;

public class Benchmark {
    private static final IRI otterhoundIRI = IRI.create("http://www.sebastienguillemin.com/dogs#Otterhound");
    private static final IRI englishFoxhoundIRI = IRI.create("http://www.sebastienguillemin.com/dogs#EnglishFoxhound");
    private static final IRI toyIRI = IRI.create("http://www.sebastienguillemin.com/dogs#Toy");
        
    private static void printFramedMessage(String message) {
        System.out.println("#--------------------------------#");
        System.out.println(message);
        System.out.println("#--------------------------------#");
    }

    public static void main(String[] args) throws Exception {
        String KB = args[0];

        printFramedMessage("The max heap size: " +  (Runtime.getRuntime().maxMemory() / 1073741824f) + "GB.\nYou can change this value using 'export MAVEN_OPTS=-Xmx32G' (here for 32GB heap size).\n\nThe current available total memory is: " +  ((Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) / 1073741824f) + "GB.");

        try {
            System.out.println("Loading KB.");
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(KB));

            SWRLRuleEngine swrlRuleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
            
            int axiomsBeforeInference = countClassAssertionAxioms(ontology);

            // swrlRuleEngine.createSWRLRule("test", ":Dog(?x)->:Otterhound(?x)");

            swrlRuleEngine.createSWRLRule("dogs_1", ":origin(?x, :England) ^ :Hound(?x) ^ :hasFriendlyRating(?x, \"7\"^^xsd:integer) ^ :hasSize(?x, \"Large\"^^xsd:string) -> :Otterhound(?x)");
            swrlRuleEngine.createSWRLRule("dogs_2", ":origin(?x, :England) ^ :Hound(?x) ^ :hasFriendlyRating(?x, \"7\"^^xsd:integer) ^ :hasSize(?x, \"Large\"^^xsd:string) ^ :hasHealthIssuesRisk(?x, \"Low\"^^xsd:string) ^ :hasAverageWeight(?x, ?w) ^ swrlb:greaterThanOrEqual(?w, \"25\"^^xsd:float) ^ swrlb:lessThanOrEqual(?w, \"35\"^^xsd:float) -> :EnglishFoxhound(?x)");
            swrlRuleEngine.createSWRLRule("dogs_3", ":origin(?x, France) ^ :hasFriendlyRating(?x, \"9\"^^xsd:integer) ^ :hasLifeSpan(?x, ?l) ^ swrlb:greaterThanOrEqual(?l, \"13\"^^xsd:integer) -> :Toy(?x)");

            System.out.println("Inferring using SWRL rules");
            long start = System.currentTimeMillis();
            swrlRuleEngine.infer();
            long stop = System.currentTimeMillis();

            int axiomsAfterInference = countClassAssertionAxioms(ontology);

            System.out.println("Saving result to CSV file.");
            CSVUtil.addToCSV("swrl_result.csv", KB + ", " + (stop - start) + ", " + (axiomsAfterInference - axiomsBeforeInference));
        } catch(Exception e) {
            System.err.println("Erreur:");
            e.printStackTrace();

            System.exit(0);
        }
        printFramedMessage("The max heap size: " +  (Runtime.getRuntime().maxMemory() / 1000000000f) + "GB.\nYou can change this value using in the pom.xml if large KB are used (need to restart the program with Maven).");

    }

    private static int countClassAssertionAxioms(OWLOntology ontology) {
        int count = 0;
        for (OWLAxiom axiom : ontology.getAxioms()) {
            if (axiom instanceof OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom classAxiom = (OWLClassAssertionAxiom) axiom;

                IRI classAxiomIRI = classAxiom.getClassExpression().asOWLClass().getIRI();
                if (classAxiomIRI.equals(otterhoundIRI) || classAxiomIRI.equals(englishFoxhoundIRI) || classAxiomIRI.equals(toyIRI))
                    count ++;
            }
        }

        return count;
    }
}
