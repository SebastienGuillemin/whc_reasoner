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
        if (args.length < 2) {
            System.err.println("Require 2 arguments: KB name and rule name");
            System.exit(1);
        }

        // Load KB
        OntologyParser ontologyParser = new OntologyParser();
        OWLOntology ontology = ontologyParser.parseTurtleOntology(args[0]);
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.logInfo("KB loaded. KB base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        // Parse rules
        Rule rule = parser.parseRule(args[1], propertiesReader.getPropertyValue("rules." + args[1]));
        reasoner.addRule(rule);

        // Infer
        reasoner.triggerRules();
        Logger.logInfo("Count of inferred axioms: " + reasoner.getInferredAxioms().size());

        // Save KB
        String newKBPath;
        if (args.length == 3)
            newKBPath = args[2];
        else
            newKBPath = propertiesReader.getPropertyValue("stups.path");

        ontologyWrapper.saveOntology(newKBPath);
        Logger.logInfo("New KB saved (path:" + propertiesReader.getPropertyValue("stups.path") + ").");
    }
}
