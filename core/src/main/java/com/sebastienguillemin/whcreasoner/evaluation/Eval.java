package com.sebastienguillemin.whcreasoner.evaluation;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
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
    private static IRI WHC_1_IRI = IRI.create("http://www.sebastienguillemin.com/dogs#whc_1");
    private static IRI WHC_2_IRI = IRI.create("http://www.sebastienguillemin.com/dogs#whc_2");
    private static IRI WHC_3_IRI = IRI.create("http://www.sebastienguillemin.com/dogs#whc_3");

    public static void main(String[] args) throws Exception {
        String KBPath = args[0];

        boolean saveInferredAxioms = false;
        if (args.length > 1)
            saveInferredAxioms = args[1].equals("save_inferred_axioms");

        OntologyParser ontologyParser = new OntologyParser();

        OWLOntology ontology = ontologyParser.parseTurtleOntology(KBPath);
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.log("KB loaded. KB base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        Rule whc1 = parser.parseRule("whc1", propertiesReader.getPropertyValue("rules.whc_1"));
        Rule whc2 = parser.parseRule("whc2", propertiesReader.getPropertyValue("rules.whc_2"));
        Rule whc3 = parser.parseRule("whc3", propertiesReader.getPropertyValue("rules.whc_3"));
        reasoner.addRule(whc1);
        reasoner.addRule(whc2);
        reasoner.addRule(whc3);

        Long start, stop;
        start = System.currentTimeMillis();
        Set<OWLAxiom> inferredAxioms = reasoner.triggerRules();
        stop = System.currentTimeMillis();

        System.out.println();

        Hashtable<IRI, Integer> inferredAxiomsPerRule = reasoner.getInferredAxiomsPerRule();

        String row = KBPath + ", " + (stop - start) + ", " + reasoner.addingInferredAxiomsTime + ", " + inferredAxioms.size() + ", ";
        row += inferredAxiomsPerRule.get(WHC_1_IRI) + ", " + inferredAxiomsPerRule.get(WHC_2_IRI) + ", " + inferredAxiomsPerRule.get(WHC_3_IRI);

        CSVUtil.addToCSV("whc_result.csv",row);

        if (saveInferredAxioms) {
            String KBBasename = KBPath.split("/")[3].split("\\.")[0];
            try (PrintWriter out = new PrintWriter("./inferred_axioms_" + KBBasename + ".txt")) {
                out.println("Axioms inferred in " + KBBasename + ":\n");
                for (OWLAxiom axiom : reasoner.getInferredAxioms())
                    out.println(axiom);
            }
        }

    }
}