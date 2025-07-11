package com.sebastienguillemin;

import java.io.File;
import java.nio.file.Path;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
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
        boolean STUPS_evaluations = false;

        printFramedMessage("The max heap size: " +  (Runtime.getRuntime().maxMemory() / 1073741824f) + "GB.\nYou can change this value using 'export MAVEN_OPTS=-Xmx32G' (here for 32GB heap size).\n\nThe current available total memory is: " +  ((Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) / 1073741824f) + "GB.");

        if (args.length > 1 && args[1].equals("stups_evaluations"))
            STUPS_evaluations = true;

        try {
            System.out.println("Loading KB.");
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(KB));

            SWRLRuleEngine swrlRuleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
            
            int axiomsBeforeInference = countClassAssertionAxioms(ontology);

            // swrlRuleEngine.createSWRLRule("test", ":Dog(?x)->:Otterhound(?x)");

            if (!STUPS_evaluations) {
                swrlRuleEngine.createSWRLRule("dogs_1", ":origin(?x, :England) ^ :Hound(?x) ^ :hasFriendlyRating(?x, \"7\"^^xsd:integer) ^ :hasSize(?x, \"Large\"^^xsd:string) -> :Otterhound(?x)");
                swrlRuleEngine.createSWRLRule("dogs_2", ":origin(?x, :England) ^ :Hound(?x) ^ :hasFriendlyRating(?x, \"7\"^^xsd:integer) ^ :hasSize(?x, \"Large\"^^xsd:string) ^ :hasHealthIssuesRisk(?x, \"Low\"^^xsd:string) ^ :hasAverageWeight(?x, ?w) ^ swrlb:greaterThanOrEqual(?w, \"25\"^^xsd:float) ^ swrlb:lessThanOrEqual(?w, \"35\"^^xsd:float) -> :EnglishFoxhound(?x)");
                swrlRuleEngine.createSWRLRule("dogs_3", ":origin(?x, France) ^ :hasFriendlyRating(?x, \"9\"^^xsd:integer) ^ :hasLifeSpan(?x, ?l) ^ swrlb:greaterThanOrEqual(?l, \"13\"^^xsd:integer) -> :Toy(?x)");
            }
            if (STUPS_evaluations) {
                // Load util built-ins
                swrlRuleEngine.getSWRLAPIOWLOntology().getSWRLBuiltInLibraryManager()
                    .loadExternalSWRLBuiltInLibraries(Path.of("src/main/java/org/swrlapi/builtins/").toFile());

                // swrlRuleEngine.createSWRLRule("q1", ":Echantillon(?x)^utils:stringsEqual(\"a\", \"a\") -> :Echantillon(?x)");

                swrlRuleEngine.createSWRLRule("stups", ":Echantillon(?x)^:Echantillon(?y)^:tauxTHC(?x, ?thcx)^:tauxTHC(?y, ?thcy)^util:lessThanfivePercent(?thcx, ?thcy)^:tauxCBN(?x, ?cdnx)^:tauxCBN(?y, ?cdny)^util:lessThanfivePercent(?cdnx, ?cdny)^:presentation(?x, ?pres)^:presentation(?y, ?pres)^:aAspectExterne(?x, ?a)^:aAspectExterne(?y, ?a)^:masse(?x, ?max)^:masse(?y, ?may)^util:lessThanfivePercent(?max, ?may)^:longueur(?x, ?lox)^:longueur(?y, ?loy)^util:lessThanfivePercent(?lox, ?loy)^:largeur(?x, ?lax)^:largeur(?y, ?lay)^util:lessThanfivePercent(?lax, ?lay)^:epaisseur(?x, ?epx)^:epaisseur(?y, ?epy)^util:lessThanfivePercent(?epx, ?epy)->:estLieA(?x, ?y)");
            }


            System.out.println("Inferring using SWRL rules(s)");
            long start = System.currentTimeMillis();
            swrlRuleEngine.infer();
            long stop = System.currentTimeMillis();

            int axiomsAfterInference = countClassAssertionAxioms(ontology);

            System.out.println("Saving result to CSV file.");
            if(!STUPS_evaluations)
                CSVUtil.addToCSV("swrl_result_dog.csv", KB + ", " + (stop - start) + ", " + (axiomsAfterInference - axiomsBeforeInference));
            else
                CSVUtil.addToCSV("swrl_result_stups.csv", KB + ", " + (stop - start) + ", " + (axiomsAfterInference - axiomsBeforeInference));
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

                OWLClassExpression classExpression = classAxiom.getClassExpression();

                if (!classExpression.isAnonymous()) {
                    IRI classAxiomIRI = classExpression.asOWLClass().getIRI();
                    if (classAxiomIRI.equals(otterhoundIRI) || classAxiomIRI.equals(englishFoxhoundIRI) || classAxiomIRI.equals(toyIRI))
                        count ++;
                }
                
            }
        }

        return count;
    }
}
