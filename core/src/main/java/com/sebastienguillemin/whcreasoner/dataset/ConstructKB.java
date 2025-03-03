package com.sebastienguillemin.whcreasoner.dataset;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.util.CSVUtil;

public class ConstructKB {
    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, IOException, OWLOntologyStorageException {
        String csvFileName = args[0];

        OntologyWrapper ontology = CSVUtil.dogsCSVToKB(csvFileName, "ontologies/dog_breeds_ontology.ttl", "http://www.sebastienguillemin.com/dogs#");
        
        String kbPath = "evaluation/KB/" + csvFileName.split("/")[3].split("\\.")[0] + ".ttl";
        System.out.println("New KB path : " + kbPath);
        ontology.saveOntology(kbPath);
    }
}
