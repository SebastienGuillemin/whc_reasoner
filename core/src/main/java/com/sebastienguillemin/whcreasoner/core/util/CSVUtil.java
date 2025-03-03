package com.sebastienguillemin.whcreasoner.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.parser.OntologyParser;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplFloat;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplInteger;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplString;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * Loads CSV to populate a KB.
 */
public class CSVUtil {
    public static OntologyWrapper dogsCSVToKB(String CSVFileName, String ontologyFileName, String baseIRI) throws OWLOntologyCreationException, FileNotFoundException, IOException {
        String workingDirectory = System.getProperty("user.dir");
        OntologyParser ontoParser = new OntologyParser();
        OWLOntology ontology = ontoParser.parseTurtleOntology(ontologyFileName);
        Logger.log("Ontology parsed.");
        OntologyWrapper wrapper = new OntologyWrapper(ontology);

        String completePath = workingDirectory + "/" + CSVFileName;

        Logger.log("Converting " + completePath + " to KB.");
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(completePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        }

        // Remove CSV header
        records.remove(0);

        OWLClass dogClass = new OWLClassImpl(IRI.create(baseIRI + "Dog"));
        OWLDataProperty hasNameProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasName"));
        OWLDataProperty hasFriendlyRatingProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasFriendlyRating"));
        OWLDataProperty hasLifeSpanProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasLifeSpan"));
        OWLDataProperty hasSizeProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasSize"));
        OWLDataProperty needsHoursOfExercicePerDayProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "needsHoursOfExercicePerDay"));
        OWLDataProperty hasIntelligenceRatingProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasIntelligenceRating"));
        OWLDataProperty hasHealthIssuesRiskProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasHealthIssuesRisk"));
        OWLDataProperty hasAverageWeightProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasAverageWeight"));
        OWLDataProperty hasTrainingDifficultyProperty = new OWLDataPropertyImpl(IRI.create(baseIRI + "hasTrainingDifficulty"));

        OWLObjectProperty originProperty = new OWLObjectPropertyImpl(IRI.create(baseIRI + "origin"));

        OWLIndividual individual;
        for (List<String> record : records) {
            // CSV file can contain a final empty line
            if (record.equals("") || record == null)
                continue;

            // Create individual
            individual = new OWLNamedIndividualImpl(IRI.create(baseIRI + record.get(1)));

            // Class
            ontology.add(new OWLClassAssertionAxiomImpl(individual, dogClass, new HashSet<>()));
            ontology.add(new OWLClassAssertionAxiomImpl(individual, new OWLClassImpl(IRI.create(baseIRI + record.get(3))), new HashSet<>()));

            // Data properties
            if (record.size() >= 2 && record.get(1) != null && !record.get(1).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasNameProperty, new OWLLiteralImplString(record.get(1)), new HashSet<>()));            
            
            if (record.size() >= 5 && record.get(4) != null && !record.get(4).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasFriendlyRatingProperty, new OWLLiteralImplInteger((int) Float.parseFloat(record.get(4))), new HashSet<>()));
            
            if (record.size() >= 6 && record.get(5) != null && !record.get(5).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasLifeSpanProperty, new OWLLiteralImplInteger((int) Float.parseFloat(record.get(5))), new HashSet<>()));
        
            if (record.size() >= 7 && record.get(6) != null && !record.get(6).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasSizeProperty, new OWLLiteralImplString(record.get(6)), new HashSet<>()));
        
            if (record.size() >= 8 && record.get(7) != null && !record.get(7).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, needsHoursOfExercicePerDayProperty, new OWLLiteralImplFloat(Float.parseFloat(record.get(7))), new HashSet<>()));
        
            if (record.size() >= 9 && record.get(8) != null && !record.get(8).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasIntelligenceRatingProperty, new OWLLiteralImplInteger((int) Float.parseFloat(record.get(8))), new HashSet<>()));
        
            if (record.size() >= 10 && record.get(9) != null && !record.get(9).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasHealthIssuesRiskProperty, new OWLLiteralImplString(record.get(9)), new HashSet<>()));
        
            if (record.size() >= 11 && record.get(10) != null && !record.get(10).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasAverageWeightProperty, new OWLLiteralImplFloat(Float.parseFloat(record.get(10))), new HashSet<>()));
        
            if (record.size() >= 12 && record.get(11) != null && !record.get(11).equals(""))
                ontology.add(new OWLDataPropertyAssertionAxiomImpl(individual, hasTrainingDifficultyProperty, new OWLLiteralImplInteger((int) Float.parseFloat(record.get(11))), new HashSet<>()));
            
            // Object properties
            if (record.size() >= 3 && record.get(2) != null && !record.get(2).equals(""))
                ontology.add(new OWLObjectPropertyAssertionAxiomImpl(individual, originProperty, wrapper.getIndividual(IRI.create(baseIRI + record.get(2))), new HashSet<>()));
        }

        // Recreate wrapper to reprocess ontology
        wrapper = new OntologyWrapper(ontology);

        return wrapper;
    }
    public static void convertAndSaveDigCSVToKB(int startingDogsCount, int MAX_I) throws OWLOntologyCreationException, FileNotFoundException, IOException, OWLOntologyStorageException {
        int n = startingDogsCount;
        for(int i = 1; i < MAX_I; i++ ) {
            OntologyWrapper wrapper = CSVUtil.dogsCSVToKB("evaluation/dataset/dogs_" + n + ".csv", "evaluation/dog_breeeds_ontology.ttl", "http://www.sebastienguillemin.com/dogs#");
            
            wrapper.saveOntology("evaluation/KB/dogs_" + n + ".ttl");
            n *= 2;
        }
    }

    public static void toCSV(String fileName, List<String[]> rows) throws FileNotFoundException {
        File csvFile = new File(fileName);

        try (PrintWriter pw = new PrintWriter(csvFile)) {
            for (String[] row : rows) {
                pw.println(String.join(", ", row));
            }
            pw.close();
        }
    }

    public static void addToCSV(String fileName, String row) throws IOException {
        File file = new File(fileName);
        boolean exists = file.exists();
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            if (!exists)
                pw.println("KB,Inferring time (ms), Adding atoms (ms), Inferred axioms, WHC_1, WHC_2, WHC_3");

            pw.println(row);
            pw.close();
        }
    }
}
