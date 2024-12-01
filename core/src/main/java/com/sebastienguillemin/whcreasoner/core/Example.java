package com.sebastienguillemin.whcreasoner.core;

import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.parser.OntologyParser;
import com.sebastienguillemin.whcreasoner.core.parser.RuleParser;
import com.sebastienguillemin.whcreasoner.core.reasoner.Reasoner;
import com.sebastienguillemin.whcreasoner.core.util.Logger;
import com.sebastienguillemin.whcreasoner.core.util.PropertiesReader;

public class Example {
    private static PropertiesReader propertiesReader = PropertiesReader.getInstance();

    public static void main(String[] args) throws Exception {
        OntologyParser ontologyParser = new OntologyParser();
        OWLOntology ontology = ontologyParser.parseTurtleOntology(propertiesReader.getPropertyValue("ontology.path"));
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
        Set<OWLAxiom> inferredAxioms =  reasoner.triggerRules();
        stop = System.currentTimeMillis();

        String results = inferredAxioms.size() + " axioms inferred:\n";
        results += "-> In : " + ((stop - start) / 1000l) + " second(s)";

        for(Entry<IRI, Integer> entry : reasoner.getInferredAxiomsPerRule().entrySet()) {
            System.out.println("Axioms inferred for rule " + entry.getKey() + " : " + entry.getValue());
        }

        Logger.log(results);

        if (propertiesReader.getPropertyValueBoolean("KB.save"))
            ontologyWrapper.saveOntology(propertiesReader.getPropertyValue("KB.path"));
    }
}