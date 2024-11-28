package com.sebastienguillemin.whcreasoner.core.parser;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.turtle.parser.TurtleOntologyParser;

import com.sebastienguillemin.whcreasoner.core.util.Logger;

public class OntologyParser {
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLOntologyLoaderConfiguration configuration;

    public OntologyParser() throws OWLOntologyCreationException {
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontology = this.manager.createOntology();
        this.initParsingConfig();
    }

    public OWLOntology parseTurtleOntology(String filePath) throws OWLOntologyCreationException {
        Logger.log("Parsing ontology : " + filePath);
        TurtleOntologyParser parser = new TurtleOntologyParser();
        parser.parse(new FileDocumentSource(new File(filePath)), ontology, this.configuration);

        // If the base/prefix cannot be loaded -> retrieve it using a TBox class IRI.
        if (ontology.isAnonymous()) {
            IRI iri = IRI.create(((OWLClass) ontology.getClassesInSignature().toArray()[0]).getIRI().getNamespace());

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            this.ontology = manager.createOntology(this.ontology.axioms(), iri);
        }

        return this.ontology;
    }

    private void initParsingConfig() {
        this.configuration = new OWLOntologyLoaderConfiguration();
        this.configuration = this.configuration.setLoadAnnotationAxioms(true);
    }
}
