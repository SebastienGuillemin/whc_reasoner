package com.sebastienguillemin.whcreasoner.core.reasoner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import com.sebastienguillemin.whcreasoner.core.entities.OntologyWrapper;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BinaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.UnaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.ClassAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.DataPropertyAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.DatatypeAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.ObjectPropertyAtom;
import com.sebastienguillemin.whcreasoner.core.entities.rule.Rule;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.BindingManagerException;
import com.sebastienguillemin.whcreasoner.core.exception.InvalidRuleException;
import com.sebastienguillemin.whcreasoner.core.exception.OWLAxiomConversionException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;
import com.sebastienguillemin.whcreasoner.core.util.Logger;

import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;

// TODO : l'affichage de la règle en train d'ête exécutée semble ne pas correspondre à celle qui l'est vraiment
public class Reasoner {
    private static final int MAX_DEPTH = 2;

    private OntologyWrapper ontologyWrapper;
    private Set<Rule> rules;

    @Getter
    private Hashtable<IRI, Integer> inferredAxiomsPerRule;
    public long addingInferredAxiomsTime;
    /**
     * 
     * @param OntologyWrapper the ontology wrapper containing the ontology in
     *                        which new facts are inferred.
     */
    public Reasoner(OntologyWrapper OntologyWrapper) {
        this.ontologyWrapper = OntologyWrapper;
        this.rules = new HashSet<>();
        this.inferredAxiomsPerRule = new Hashtable<>();
        this.addingInferredAxiomsTime = 0l;
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
        this.inferredAxiomsPerRule.put(rule.getIRI(), 0);
    }

    public Set<OWLAxiom> triggerRules()
            throws VariableValueException, BindingManagerException, OWLAxiomConversionException {
        return this.processRules(this.rules);
    }

    private Set<OWLAxiom> processRules(Set<Rule> rules) throws VariableValueException, BindingManagerException, OWLAxiomConversionException {
        if (rules.size() == 0)
            return new HashSet<>();

        // Set of rules that must be reprocessed.
        Set<Rule> ruleToReprocess = new HashSet<>();

        // Set of inferred axioms after having processed all rules.
        Set<OWLAxiom> inferredAxioms = new HashSet<>();

        // Process each rule
        for (Rule rule : rules) {
            Logger.logInfo("Processing rule : " + rule);

            // temp = Temp set (will contain set of inferred axioms for an hypotheses)
            // inferredAxiomsForCurrentRule = Set containing all inferred axioms for the current rule
            Set<OWLAxiom> temp, inferredAxiomsForCurrentRule = new HashSet<>();

            // Find hypotheses
            Atom ruleHead = rule.getHead();
            BindingManager ruleGoals = new BindingManager(this.findHypotheses(ruleHead));
            try (ProgressBar pb = new ProgressBar(String.format("[Reasoner] Testing %s hypothesis for rule '%s'",ruleGoals.getTotalIter(), rule.getIRI().getFragment()), ruleGoals.getTotalIter())) {
                
                // Process each htpothesis
                while (ruleGoals.hasNextBinding()) {
                    ruleGoals.nextBinding();

                    // Skip if needed
                    if (this.ontologyWrapper.alreadyInOntology(ruleHead)) {
                        Logger.logInference("####### SKIPPING " + ruleHead + "\n", 0);
                        pb.step();
                        continue;
                    }

                    // Try to prove hypothesis
                    Logger.logInference("####### PROVING  " + ruleHead, 0);
                    temp = this.prove(new TreeSet<>(Arrays.asList(ruleHead)), 0, rule.getTotalWeight(), rule.getThreshold(), 0);

                    // Adding inferred axioms to inferredAxiomsForCurrentRule
                    if (temp != null)
                        inferredAxiomsForCurrentRule.addAll(temp);

                    pb.step();
                }
            }
            Logger.logInfo(String.format("%s axioms inferred with rule '%s'.\n", inferredAxiomsForCurrentRule.size(), rule.getIRI().getFragment()));

            this.inferredAxiomsPerRule.put(rule.getIRI(), this.inferredAxiomsPerRule.get(rule.getIRI()) + inferredAxiomsForCurrentRule.size());

            // Adding inferred axioms to ontology (done here to avoid proving hypotheses in next rule)
            long start = System.currentTimeMillis();
            this.ontologyWrapper.addInferredAxioms(inferredAxioms);
            this.addingInferredAxiomsTime += System.currentTimeMillis() - start;

            // Finding rule to reprocess
            if (inferredAxiomsForCurrentRule.size() > 0)
                ruleToReprocess.addAll(rules.stream().filter(r -> r.containsAtom(rule.getHead())).collect(Collectors.toSet()));

            inferredAxioms.addAll(inferredAxiomsForCurrentRule);
        }

        // Recursive call
        Logger.logInfo("Rules to rerun : " + ruleToReprocess + "\n");
        inferredAxioms.addAll(this.processRules(ruleToReprocess));
        return inferredAxioms;
    }

    private Set<OWLAxiom> prove(TreeSet<Atom> goals, float weight, float totalWeight, float threshold, int depth) throws VariableValueException, BindingManagerException, OWLAxiomConversionException {

        Set<OWLAxiom> res = new TreeSet<>();
        // --- IF NO GOALS TO PROVE THEN RETURNS EMPTY SET
        if (goals.size() == 0)
            return res;

        Logger.logInference("Goals : " + goals, depth);
        
        // --- IF FIRST GOAL IS IN KB CALL THEN PROVE FOR REMAINING GOALS
        Atom goal = this.pop(goals);
        Logger.logInference("Goal : " + goal, depth);
        if (this.satisfied(goal)) {
            return this.prove(goals, weight, totalWeight, threshold, depth);
        }

        // --- IF CURRENT DEPTH > MAX_DEPTH THEN GOAL VARIALBES ARE BOUND TO NOTHING
        if (depth > MAX_DEPTH)
            this.bindRemainingVariablesToNothing(goal);
        else {
            // --- TRY TO BIND UNBOUND VARIABLES AND PROVE REMAING GOALS
            BindingManager newGoalsManager = new BindingManager(this.findVariableSubstitutions(goal));
            Logger.logInference(newGoalsManager.getTotalIter() + " binding possibilities", depth);
            while (newGoalsManager.hasNextBinding()) {
                // Bind goal's variable and infer remaining goals.
                newGoalsManager.nextBinding();
                Logger.logInference("Binding variable in goal, becomes : " + goal + "(already satisfied)", depth);
                res = this.prove(goals, weight, totalWeight, threshold, depth);
    
                // If remining goals are inferred
                if (res != null)
                    return res;
            }
    
            // --- IF NO BINDING WAS TEST PREVIOUSLY THEN TRY TO SUBSTITUTE GOAL BY RULES BODY
            // --- (intermediary proved goals are saved!)
            if (newGoalsManager.getTotalIter() == 0) {
                for (Rule rule : this.findRuleSubstitutions(goal)) {
                    Logger.logInference(String.format("Substitute %s by rule %s (%s)", goal, rule, depth), depth);
    
                    res = this.prove(new TreeSet<>(rule.getBody()), rule.getTotalWeight(), rule.getTotalWeight(),
                            rule.getThreshold(), depth + 1);
                    if (res != null) {
                        res.add(goal.toOWLAxiom());
                        Set<OWLAxiom> goalsInferences = this.prove(goals, weight, totalWeight, threshold, depth);
    
                        if (goalsInferences != null)
                            res.addAll(goalsInferences);
    
                        return res;
                    }
                }
            }
        }

        // --- IF GOAL CANNOT BE SATISFIED THEN DECREASE WEIGHT
        // --> IF REMAINING WEIGHT IS TOO LOW THEN RETURN NULL
        // --> OTHERWISE TRY TO PROVE REMAINING GOALS
        Logger.logInference(goal + " is not satisfied (" + depth + ")", depth);

        float newWeight = weight - goal.getWeight();
        if (newWeight / totalWeight < threshold) {
            Logger.logInference("Fail" + ((depth == 0) ? "\n" : ""), depth);
            return null;
        }

        return this.prove(goals, newWeight, totalWeight, threshold, depth);
    }

    private Atom pop(TreeSet<Atom> goals) {
        Atom goal = goals.first();
        goals.remove(goal);
        
        return goal;
    }

    private boolean satisfied(Atom atom) {
        // All variables must have a value
        if (atom.getVariables().stream().filter(v -> v.getValue() == null).collect(Collectors.toSet()).size() > 0) {
            return false;
        }

        IRI atomIRI = atom.getIRI();
        // Test if goal's variable corresponds to the class or datatype.
        if (atom instanceof UnaryAtom) {
            if (atom instanceof ClassAtom) {
                Set<OWLPropertyAssertionObject> individuals = ontologyWrapper.getIndividualsOfClass(atomIRI);

                return (individuals != null && individuals.size() != 0
                        && individuals.contains(((ClassAtom) atom).getVariable().getValue()));
            } else if (atom instanceof DatatypeAtom) {
                DatatypeAtom datatypeAtom = (DatatypeAtom) atom;

                return ((OWLLiteral) datatypeAtom.getVariable().getValue()).getDatatype().getIRI()
                        .equals(datatypeAtom.getIRI());
            }
        }
        // Test if 'predicate(Subject, Object)' exists
        else if (atom instanceof BinaryAtom) {
            if (atom instanceof ObjectPropertyAtom) {
                ObjectPropertyAtom objectPropertyAtom = (ObjectPropertyAtom) atom;

                Set<OWLPropertyAssertionObject> values = ontologyWrapper.getObjectForSubjectOfObjectProperty(atomIRI,
                        objectPropertyAtom.getFirstVariable().getValue());

                return (values != null && values.size() != 0
                        && values.contains(objectPropertyAtom.getSecondVariable().getValue()));
            } else if (atom instanceof DataPropertyAtom) {
                DataPropertyAtom dataPropertyAtom = (DataPropertyAtom) atom;

                Set<OWLPropertyAssertionObject> values = ontologyWrapper.getObjectForSubjectOfDataProperty(atomIRI,
                        dataPropertyAtom.getFirstVariable().getValue());

                return (values != null && values.size() != 0
                        && values.contains(dataPropertyAtom.getSecondVariable().getValue()));
            }
        } else if (atom instanceof BuiltInAtom)
            return ((BuiltInAtom) atom).isSatisfied();

        return false;
    }

    private void bindRemainingVariablesToNothing(Atom goal) {
        goal.getVariables().stream().filter(v -> !v.isBoundToNothing() && v.getValue() == null)
                .forEach(v -> v.bindToNothing());
    }

    private HashMap<Variable, Set<OWLPropertyAssertionObject>> findVariableSubstitutions(Atom goal) {
        HashMap<Variable, Set<OWLPropertyAssertionObject>> variablesSubsitutions = new HashMap<>();

        if (goal instanceof UnaryAtom) {
            Variable variable = ((UnaryAtom) goal).getVariable();

            if (!variable.isBoundToNothing() && variable.getValue() == null) {
                if (goal instanceof ClassAtom) {
                    variablesSubsitutions.put(
                            variable,
                            this.ontologyWrapper.getIndividualsOfClass(goal.getIRI()));
                } else if (goal instanceof DatatypeAtom) {
                    variablesSubsitutions.put(
                            variable,
                            this.ontologyWrapper.getLiteralsOftype(goal.getIRI()));
                }
            }
        }

        else if (goal instanceof BinaryAtom) {
            BinaryAtom binaryAtom = (BinaryAtom) goal;
            Variable variable1 = binaryAtom.getFirstVariable();
            Variable variable2 = binaryAtom.getSecondVariable();

            if (!variable1.isBoundToNothing() && variable1.getValue() == null) {
                if (goal instanceof ObjectPropertyAtom) {
                    variablesSubsitutions.put(
                            variable1,
                            this.ontologyWrapper.getSubjectForObjectOfObjectProperty(goal.getIRI(),
                                    variable2.getValue()));
                }
                if (goal instanceof DataPropertyAtom) {
                    variablesSubsitutions.put(
                            variable1,
                            this.ontologyWrapper.getSubjectForObjectOfDataProperty(goal.getIRI(),
                                    variable2.getValue()));
                }
            }

            if (!variable2.isBoundToNothing() && variable2.getValue() == null) {
                if (goal instanceof ObjectPropertyAtom) {
                    variablesSubsitutions.put(
                            variable2,
                            this.ontologyWrapper.getObjectForSubjectOfObjectProperty(goal.getIRI(),
                                    variable1.getValue()));
                }
                if (goal instanceof DataPropertyAtom) {
                    variablesSubsitutions.put(
                            variable2,
                            this.ontologyWrapper.getObjectForSubjectOfDataProperty(goal.getIRI(),
                                    variable1.getValue()));
                }

            }
        }

        return variablesSubsitutions;
    }

    private HashMap<Variable, Set<OWLPropertyAssertionObject>> findHypotheses(Atom atom) {
        HashMap<Variable, Set<OWLPropertyAssertionObject>> variablesValues = new HashMap<>();

        if (atom instanceof ClassAtom) {
            variablesValues.put(
                    ((ClassAtom) atom).getVariable(),
                    this.ontologyWrapper.getIndividuals());
        } else if (atom instanceof DatatypeAtom) {
            variablesValues.put(
                    ((DatatypeAtom) atom).getVariable(),
                    this.ontologyWrapper.getAllLitterals());
        } else if (atom instanceof BinaryAtom) {
            BinaryAtom binaryAtom = (BinaryAtom) atom;
            Set<OWLPropertyAssertionObject> values;
            if (binaryAtom instanceof ObjectPropertyAtom) {
                values = this.ontologyWrapper.getObjectPropertiesDomains().get(binaryAtom.getIRI()).stream()
                        .flatMap(d -> d.nestedClassExpressions())
                        .flatMap(c -> this.ontologyWrapper.getIndividualsOfClass(c.asOWLClass().getIRI()).stream())
                        .collect(Collectors.toSet());

                if (values.size() == 0) {
                    values = this.ontologyWrapper.getIndividuals();
                }

                variablesValues.put(binaryAtom.getFirstVariable(), values);

                values = this.ontologyWrapper.getObjectPropertiesRanges().get(binaryAtom.getIRI()).stream()
                        .flatMap(d -> d.nestedClassExpressions())
                        .flatMap(c -> this.ontologyWrapper.getIndividualsOfClass(c.asOWLClass().getIRI()).stream())
                        .collect(Collectors.toSet());

                if (values.size() == 0) {
                    values = this.ontologyWrapper.getIndividuals();
                }

                variablesValues.put(binaryAtom.getSecondVariable(), values);
            } else if (binaryAtom instanceof DataPropertyAtom) {
                values = this.ontologyWrapper.getDataPropertiesDomains().get(binaryAtom.getIRI()).stream()
                        .flatMap(d -> d.nestedClassExpressions())
                        .flatMap(c -> this.ontologyWrapper.getIndividualsOfClass(c.asOWLClass().getIRI()).stream())
                        .collect(Collectors.toSet());

                if (values.size() == 0)
                    values = this.ontologyWrapper.getIndividuals();

                variablesValues.put(binaryAtom.getFirstVariable(), values);

                values = this.ontologyWrapper.getDataPropertiesRanges().get(binaryAtom.getIRI()).stream()
                        .flatMap(d -> d.getDatatypesInSignature().stream())
                        .flatMap(dt -> this.ontologyWrapper.getLiteralsOftype(dt.getIRI()).stream())
                        .collect(Collectors.toSet());

                if (values.size() == 0)
                    values = this.ontologyWrapper.getAllLitterals();

                variablesValues.put(binaryAtom.getSecondVariable(), values);
            }
        }

        return variablesValues;
    }

    private Set<Rule> findRuleSubstitutions(Atom goal) throws VariableValueException {
        if (goal.getVariables().stream().filter(v -> !v.hasValue()).collect(Collectors.toSet()).size() != 0)
            return new HashSet<>();

        Set<Rule> substitutionsRules = new HashSet<>();

        Rule tempRule;
        for (Rule rule : this.rules.stream().filter(r -> r.getHead().getIRI().equals(goal.getIRI()))
                .collect(Collectors.toSet())) {
            try {
                tempRule = new Rule(rule, false);

                if (goal instanceof UnaryAtom) {
                    UnaryAtom unaryGoal = (UnaryAtom) goal;
                    Variable ruleHeadVar = ((UnaryAtom) tempRule.getHead()).getVariable();
                    Variable goalVar = unaryGoal.getVariable();

                    if (goalVar.isBoundToNothing())
                        ruleHeadVar.bindToNothing();
                    else
                        ruleHeadVar.setValue(goalVar.getValue());
                } else if (goal instanceof BinaryAtom) {
                    BinaryAtom binaryGoal = (BinaryAtom) goal;
                    Variable firstRuleHeadVar = ((BinaryAtom) tempRule.getHead()).getFirstVariable();
                    Variable secondRuleHeadVar = ((BinaryAtom) tempRule.getHead()).getSecondVariable();
                    Variable firstGoalVar = binaryGoal.getFirstVariable();
                    Variable secondGoalVar = binaryGoal.getSecondVariable();

                    if (firstGoalVar.isBoundToNothing())
                        firstRuleHeadVar.bindToNothing();
                    else
                        firstRuleHeadVar.setValue(firstGoalVar.getValue());

                    if (secondGoalVar.isBoundToNothing())
                        secondRuleHeadVar.bindToNothing();
                    else
                        secondRuleHeadVar.setValue(secondGoalVar.getValue());
                }

                substitutionsRules.add(tempRule);
            } catch (InvalidRuleException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        return substitutionsRules;
    }
}