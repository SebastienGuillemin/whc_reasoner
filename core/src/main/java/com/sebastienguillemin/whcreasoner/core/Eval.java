package com.sebastienguillemin.whcreasoner.core;

import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.parser.OntologyParser;
import com.sebastienguillemin.whcreasoner.core.parser.RuleParser;
import com.sebastienguillemin.whcreasoner.core.reasoner.Reasoner;
import com.sebastienguillemin.whcreasoner.core.util.CSVUtil;
import com.sebastienguillemin.whcreasoner.core.util.Logger;
import com.sebastienguillemin.whcreasoner.core.util.PropertiesReader;

public class Eval {
    private static PropertiesReader propertiesReader = PropertiesReader.getInstance();

    public static void main(String[] args) throws Exception {
        String KB = args[0];

        OntologyParser ontologyParser = new OntologyParser();

        OWLOntology ontology = ontologyParser.parseTurtleOntology(KB);
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.log("Ontology loaded. Ontology base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        Rule rule;
        for (Entry<String, String> ruleEntry : propertiesReader.getRules().entrySet()) {
            rule = parser.parseRule(ruleEntry.getKey(), ruleEntry.getValue());
            reasoner.addRule(rule);
        }

        Long start, stop;
        start = System.currentTimeMillis();
        Set<OWLAxiom> inferredAxioms = reasoner.triggerRules();
        stop = System.currentTimeMillis();

        System.out.println();

        CSVUtil.addToCSV("whc_result.csv", KB + ", " + (stop - start) + ", " + reasoner.addingInferredAxiomsTime + ", " + inferredAxioms.size());

        if (propertiesReader.getPropertyValueBoolean("KB.save"))
            ontologyWrapper.saveOntology(propertiesReader.getPropertyValue("KB.path"));

    }
}