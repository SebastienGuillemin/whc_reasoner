package com.sebastienguillemin.whcreasoner.example;

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
        // Load KB
        OntologyParser ontologyParser = new OntologyParser();
        OWLOntology ontology = ontologyParser.parseTurtleOntology(propertiesReader.getPropertyValue("example.kb"));
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.log("KB loaded. KB base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        // Parse rules
        Rule whc1 = parser.parseRule("whc1", propertiesReader.getPropertyValue("rules.whc_1"));
        Rule whc2 = parser.parseRule("whc2", propertiesReader.getPropertyValue("rules.whc_2"));
        Rule whc3 = parser.parseRule("whc3", propertiesReader.getPropertyValue("rules.whc_3"));
        reasoner.addRule(whc1);
        reasoner.addRule(whc2);
        reasoner.addRule(whc3);

        // Infer
        Long start, stop;
        start = System.currentTimeMillis();
        Set<OWLAxiom> inferredAxioms =  reasoner.triggerRules();
        stop = System.currentTimeMillis();

        // Print results
        System.out.println();
        String results = inferredAxioms.size() + " axioms inferred:\n";

        for (OWLAxiom axiom : inferredAxioms)
            results += axiom + "\n";

        results += "-> In : " + (((float) (stop - start)) / 1000f) + " second(s)";
        
        Logger.log(results);

        System.out.println();

        for(Entry<IRI, Integer> entry : reasoner.getInferredAxiomsPerRule().entrySet()) {
            Logger.log("Axioms inferred for rule " + entry.getKey() + " : " + entry.getValue());
        }

        // Save KB
        if (propertiesReader.getPropertyValueBoolean("example.save"))
            ontologyWrapper.saveOntology(propertiesReader.getPropertyValue("example.path"));
    }
}