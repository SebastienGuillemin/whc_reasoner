package com.sebastienguillemin.whcreasoner.stups;

import org.semanticweb.owlapi.model.OWLOntology;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.parser.OntologyParser;
import com.sebastienguillemin.whcreasoner.core.parser.RuleParser;
import com.sebastienguillemin.whcreasoner.core.reasoner.Reasoner;
import com.sebastienguillemin.whcreasoner.core.util.Logger;
import com.sebastienguillemin.whcreasoner.core.util.PropertiesReader;

public class BatchInferrer {
    private static PropertiesReader propertiesReader = PropertiesReader.getInstance();

    public static void main(String[] args) throws Exception {
        // Load KB
        OntologyParser ontologyParser = new OntologyParser();
        OWLOntology ontology = ontologyParser.parseTurtleOntology(propertiesReader.getPropertyValue("stups.kb"));
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.log("KB loaded. KB base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        // Parse rules
        Rule lot = parser.parseRule("lot", propertiesReader.getPropertyValue("rules.lot"));
        reasoner.addRule(lot);

        // Infer
        reasoner.triggerRules();

        // Save KB
        ontologyWrapper.saveOntology(propertiesReader.getPropertyValue("stups.path"));
    }
}
