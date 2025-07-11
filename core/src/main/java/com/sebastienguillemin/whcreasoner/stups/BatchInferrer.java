package com.sebastienguillemin.whcreasoner.stups;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.explainer.Explainer;
import com.sebastienguillemin.whcreasoner.core.parser.OntologyParser;
import com.sebastienguillemin.whcreasoner.core.parser.RuleParser;
import com.sebastienguillemin.whcreasoner.core.reasoner.Reasoner;
import com.sebastienguillemin.whcreasoner.core.util.Logger;
import com.sebastienguillemin.whcreasoner.core.util.PropertiesReader;

public class BatchInferrer {
    private static PropertiesReader propertiesReader = PropertiesReader.getInstance();

    private static List<String> readRules(String filePath) throws IOException {
        List<String> rules = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            for(String line; (line = br.readLine()) != null; ) {
                rules.add(line);
            }
        }

        return rules;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Require 2 arguments: KB name and rule file path");
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
        int ruleCount = 1;
        for (String ruleStr : readRules(args[1])) {
            Rule rule = parser.parseRule("rule_" + (ruleCount++), ruleStr);
            Logger.log("Rule parsed: " + rule + "\n");
            reasoner.addRule(rule);
        }

        // Infer
        reasoner.triggerRules(true, true, true);
        Logger.logInfo("Count of inferred axioms: " + reasoner.getInferredAxioms().size());

        // Save KB
        String newKBPath;
        if (args.length == 3)
            newKBPath = args[2];
        else
            newKBPath = propertiesReader.getPropertyValue("stups.path");

        ontologyWrapper.saveOntology(newKBPath);
        Logger.logInfo("New KB saved (path:" + propertiesReader.getPropertyValue("stups.path") + ").");

        // Save explanations
        Explainer explainer = new Explainer();
        explainer.writeExplanations(reasoner.getSatisfiedAtomCauses(), propertiesReader.getPropertyValue("stups.explanations_path"));
        Logger.logInfo("Explanations saved (path:" + propertiesReader.getPropertyValue("stups.explanations_path") + ").");

    }
}
