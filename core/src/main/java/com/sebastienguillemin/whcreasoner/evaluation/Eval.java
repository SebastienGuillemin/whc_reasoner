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
    private static IRI STUPS_RULE = IRI.create("http://www.sebastienguillemin.com/dogs#stups_rule");

    public static void main(String[] args) throws Exception {
        if (args.length < 3)
            throw new Exception("Need at least two arguments : 'KBPath', 'STUPS_evaluation' and 'saveInferredAxioms' .");

        String KBPath = args[0];
        boolean STUPS_evaluation = args[1].equals("stups_evaluation");
        boolean saveInferredAxioms = args[2].equals("save_inferred_axioms");
  

        Long start, stop;
        Set<OWLAxiom> inferredAxioms;

        OntologyParser ontologyParser = new OntologyParser();

        OWLOntology ontology = ontologyParser.parseTurtleOntology(KBPath);
        OntologyWrapper ontologyWrapper = new OntologyWrapper(ontology);

        Logger.log("KB loaded. KB base IRI : " + ontologyWrapper.getBaseIRI() + "\n");

        Reasoner reasoner = new Reasoner(ontologyWrapper);
        RuleParser parser = new RuleParser(ontologyWrapper);

        if (!STUPS_evaluation) {
            Rule whc1 = parser.parseRule("whc_1", propertiesReader.getPropertyValue("rules.whc_1"));
            Rule whc2 = parser.parseRule("whc_2", propertiesReader.getPropertyValue("rules.whc_2"));
            Rule whc3 = parser.parseRule("whc_3", propertiesReader.getPropertyValue("rules.whc_3"));
            reasoner.addRule(whc1);
            reasoner.addRule(whc2);
            reasoner.addRule(whc3);
            start = System.currentTimeMillis();
            inferredAxioms = reasoner.triggerRules();
            stop = System.currentTimeMillis();
        }
        else {
            Rule stups_rule = parser.parseRule("stups_rule", propertiesReader.getPropertyValue("rules.stups.lot_cannabis"));
            reasoner.addRule(stups_rule);
            start = System.currentTimeMillis();
            inferredAxioms = reasoner.triggerRules(true, true, true);
            stop = System.currentTimeMillis();
        }

        System.out.println();

        Hashtable<IRI, Integer> inferredAxiomsPerRule = reasoner.getInferredAxiomsPerRule();

        String row = KBPath + ", " + (stop - start) + ", " + reasoner.addingInferredAxiomsTime + ", " + inferredAxioms.size() + ", ";

        if (!STUPS_evaluation)
            row += inferredAxiomsPerRule.get(WHC_1_IRI) + ", " + inferredAxiomsPerRule.get(WHC_2_IRI) + ", " + inferredAxiomsPerRule.get(WHC_3_IRI);
        else
            row += inferredAxiomsPerRule.get(STUPS_RULE);


        System.out.println("Skipped : " + Reasoner.skipped);

        if (!STUPS_evaluation)
            CSVUtil.addToCSV("whc_result_dogs.csv",row);
        else
            CSVUtil.addToCSV("whc_result_stups.csv",row);

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