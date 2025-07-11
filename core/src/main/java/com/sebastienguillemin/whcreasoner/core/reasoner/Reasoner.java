package com.sebastienguillemin.whcreasoner.core.reasoner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.solr.common.util.Hash;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
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
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;

// TODO : l'affichage de la règle en train d'ête exécutée semble ne pas correspondre à celle qui l'est vraiment
public class Reasoner {
    private static final int MAX_DEPTH = 5;

    public static int skipped = 0;

    private OntologyWrapper ontologyWrapper;

    @Getter
    private Set<Rule> rules;

    @Getter
    private Hashtable<IRI, Integer> inferredAxiomsPerRule;

    @Getter
    private Hashtable<Atom, Set<Atom>> satisfiedAtomCauses;

    @Getter
    private Set<OWLAxiom> inferredAxioms;
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
        this.inferredAxioms = new HashSet<>();
        this.satisfiedAtomCauses = new Hashtable<>();
        this.addingInferredAxiomsTime = 0l;
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
        this.inferredAxiomsPerRule.put(rule.getIRI(), 0);
    }

    public Set<OWLAxiom> triggerRules()
            throws VariableValueException, BindingManagerException, OWLAxiomConversionException {
        return this.processRules(this.rules, false, false, false);
    }

    public Set<OWLAxiom> triggerRules(boolean irreflexiveHead, boolean symmetricHead, boolean transitiveHead)
            throws VariableValueException, BindingManagerException, OWLAxiomConversionException {
        return this.processRules(this.rules, irreflexiveHead, symmetricHead, transitiveHead);
    }

    private Set<OWLAxiom> processRules(Set<Rule> rules, boolean irreflexiveHead, boolean symmetricHead, boolean transitiveHead)
            throws VariableValueException, BindingManagerException, OWLAxiomConversionException {
        if (rules.size() == 0)
            return new HashSet<>();

        // Set of rules that must be reprocessed.
        Set<Rule> ruleToReprocess = new HashSet<>();

        // Set of inferred axioms after having processed all rules.

        // Process each rule
        for (Rule rule : rules) {
            Logger.logInfo("Processing rule : " + rule.getIRI().getFragment());

            // temp = Temp set (will contain set of inferred axioms for an hypotheses)
            // inferredAxiomsForCurrentRule = Set containing all inferred axioms for the
            // current rule
            Set<OWLAxiom> inferredAxiomsForCurrentRule = new HashSet<>();

            // Find hypotheses
            Atom ruleHead = rule.getHead();
            OWLAxiom ruleHeadAxiom, symmetricRuleHeadAxiom = null;
            Set<OWLAxiom> triedToProveAxioms = new HashSet<>();
            BindingManager ruleGoals = new BindingManager(this.findGoals(ruleHead));
            try (ProgressBar pb = new ProgressBar(String.format("[Reasoner] Testing %s hypothesis for rule '%s'", ruleGoals.getTotalIter(), rule.getIRI().getFragment()), ruleGoals.getTotalIter())) {
                // Process each hypothesis
                while (ruleGoals.hasNextBinding()) {
                    ruleGoals.nextBinding();
                    ruleHeadAxiom = ruleHead.toOWLAxiom();

                    if (triedToProveAxioms.contains(ruleHeadAxiom)) {
                        skipped++;
                        pb.step();
                        continue;
                    }
                    if (irreflexiveHead) {
                        OWLObjectPropertyAssertionAxiom ruleHeadAxiomAsProperty = (OWLObjectPropertyAssertionAxiom) ruleHeadAxiom;
                        OWLIndividual subject = ruleHeadAxiomAsProperty.getSubject();
                        OWLIndividual object  = ruleHeadAxiomAsProperty.getObject();
                        if (subject.equals(object)) {
                            triedToProveAxioms.add(ruleHeadAxiom);
                            skipped++;
                            pb.step();
                            continue; 
                        }
                    }

                    triedToProveAxioms.add(ruleHeadAxiom);
                    if (symmetricHead) {
                        symmetricRuleHeadAxiom = ruleHead.toSymmetricOWLAxiom();
                        triedToProveAxioms.add(symmetricRuleHeadAxiom);
                    }

                    // Try to prove hypothesis
                    Logger.logInference("\n####### PROVING  " + ruleHead, 0);

                    // Adding inferred axioms to inferredAxiomsForCurrentRule
                    Set<Atom> causes = new HashSet<>();
                    // if (this.backwardChaining(new TreeSet<>(Arrays.asList(ruleHead)), 0, null, 0,
                    // causes)) {

                    if (this.backwardChaining(new TreeSet<Atom>(rule.getBody()), 0, null, 0, causes)) {

                        Logger.logInference("####### Proven at this iteration : " + ruleHeadAxiom, 0);

                        this.inferredAxioms.add(ruleHeadAxiom);
                        this.satisfiedAtomCauses.put(ruleHead.copy(), causes);
                        inferredAxiomsForCurrentRule.add(ruleHeadAxiom);

                        if (symmetricHead) {
                            this.inferredAxioms.add(symmetricRuleHeadAxiom);
                            inferredAxiomsForCurrentRule.add(symmetricRuleHeadAxiom);
                        }
                        if (transitiveHead) {
                            OWLObjectPropertyAssertionAxiom ruleHeadAxiomAsProperty = (OWLObjectPropertyAssertionAxiom) ruleHeadAxiom;
                            OWLObjectPropertyExpression property = ruleHeadAxiomAsProperty.getProperty();
                            OWLIndividual subject = ruleHeadAxiomAsProperty.getSubject();
                            OWLIndividual object  = ruleHeadAxiomAsProperty.getObject();
                            HashSet<OWLAxiom> toAdd = new HashSet<>();

                            for (OWLAxiom axiom : this.inferredAxioms) {
                                OWLObjectPropertyAssertionAxiom temp = (OWLObjectPropertyAssertionAxiom) axiom;
                                if (!temp.getProperty().equals(property))
                                    continue;
                                if (subject.equals(temp.getSubject())) {
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(object, property,  temp.getObject(), new HashSet<>()));
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(temp.getObject(), property,  object, new HashSet<>()));
                                }
                                if (object.equals(temp.getObject())) {
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(subject, property,  temp.getSubject(), new HashSet<>()));
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(temp.getSubject(), property,  subject, new HashSet<>()));
                                }
                                if (object.equals(temp.getSubject())) {
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(subject, property,  temp.getObject(), new HashSet<>()));
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(temp.getObject(), property,  subject, new HashSet<>()));
                                }
                                if (subject.equals(temp.getObject())) {
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(object, property,  temp.getSubject(), new HashSet<>()));
                                    toAdd.add(new OWLObjectPropertyAssertionAxiomImpl(temp.getSubject(), property,  object, new HashSet<>()));
                                }
                            }
                            this.inferredAxioms.addAll(toAdd);
                            inferredAxiomsForCurrentRule.addAll(toAdd);
                            triedToProveAxioms.addAll(toAdd);
                        }
                    } else {
                        Logger.logInference("####### Nothing proven at the iteration.", 0);
                    }

                    // Unbind rule variables
                    rule.getBody().stream().forEach(a -> a.getVariables().stream().forEach(v -> v.unbind()));

                    pb.step();
                }
            }
            Logger.logInfo(String.format("%s axioms inferred with rule '%s'.\n", inferredAxiomsForCurrentRule.size(),
                    rule.getIRI().getFragment()));

            this.inferredAxiomsPerRule.put(rule.getIRI(),
                    this.inferredAxiomsPerRule.get(rule.getIRI()) + inferredAxiomsForCurrentRule.size());

            // Adding inferred axioms to ontology (done here to avoid proving hypotheses in
            // next rule)
            long start = System.currentTimeMillis();
            this.ontologyWrapper.addInferredAxioms(this.inferredAxioms);
            this.addingInferredAxiomsTime += System.currentTimeMillis() - start;

            // Finding rule to reprocess
            if (inferredAxiomsForCurrentRule.size() > 0)
                ruleToReprocess.addAll(
                        rules.stream().filter(r -> r.containsAtom(rule.getHead()))
                                .collect(Collectors.toSet()));
        }

        // Recursive call
        Logger.logInfo("Rules to rerun : " + ruleToReprocess + "\n");
        this.inferredAxioms.addAll(this.processRules(ruleToReprocess, irreflexiveHead, symmetricHead, transitiveHead));
        
        return this.inferredAxioms;
    }

    /**
     * 
     * @param goals     The set of goals to prove.
     * @param weight    The weight of validated atoms since the last substitution.
     *                  Use weight rather than confidence score to ease computation
     * @param substRule The substitution rule.
     * @param depth     The current depth in the tree search.
     * @return The list of proven goals.
     * 
     * @throws VariableValueException
     * @throws BindingManagerException
     * @throws OWLAxiomConversionException
     */
    private boolean backwardChaining(TreeSet<Atom> goals, float weight, Rule substRule, int depth, Set<Atom> causes)
            throws VariableValueException, BindingManagerException, OWLAxiomConversionException {

        // --- IF NO GOALS TO PROVE THEN RETURNS EMPTY SET
        if (goals.size() == 0)
            return true;

        Logger.skipLineInference();
        Logger.logInference("Goals : " + goals, depth);

        // --- IF FIRST GOAL IS SATISFIED THEN PROVE THE REMAINING GOALS
        Atom goal = goals.pollFirst();
        if (this.satisfied(goal)) {
            Logger.logInference("Satisfied : " + goal, depth);
            causes.add(goal.copy());
            return this.backwardChaining(goals, weight, substRule, depth, causes);
        }
        Logger.logInference("Not satisfied : " + goal, depth);

        // --- TEST CURRENT DEPTH
        if (depth < MAX_DEPTH) {
            // --- TRY TO PROVE GOAL USING VARIABLE SUBSTITUTIONS
            BindingManager variableSubstitutions = new BindingManager(this.findVariableSubstitutions(goal));
            Logger.logInference(variableSubstitutions.getTotalIter() + " binding possibilities", depth);
            while (variableSubstitutions.hasNextBinding()) {
                // Bind goal's variable (goal is directly satisfied after binding) and try to prove remaining goals.
                variableSubstitutions.nextBinding();
                Logger.logInference("Binding variable in goal, becomes : " + goal + "(already satisfied)", depth);

                // If subsitution is proven (i.e., remaining goals are proven) return 'true' otherwise, try next substitution
                if (this.backwardChaining(goals, weight, substRule, depth, causes)) {
                    causes.add(goal.copy());
                    return true;
                }
            }

            // UNBIND VARIABLE BIND AFTER TRYING VARIABLE SUBSTITUTIONS
            Set<Variable> variablesToUnbind = goal.getVariables();
            variablesToUnbind.retainAll(new ArrayList<>(Arrays.asList(variableSubstitutions.getVariables())));
            variablesToUnbind.stream().forEach(v -> v.unbind());

            // --- TRY RULE SUBSTITUTIONS
            for (Rule uSubstRule : this.findRuleSubstitutions(goal)) {
                Logger.logInference(String.format("Substitute %s by rule %s (%s)", goal, uSubstRule, depth), depth);

                // --- IF SUBSTITUTION PROVEN, OTHERWISE TEST NEXT RULE SUBSTITUTION
                if (this.backwardChaining(new TreeSet<>(uSubstRule.getBody()), uSubstRule.getTotalWeight(), uSubstRule, depth + 1, causes)) {
                    causes.add(goal.copy());
                    // --- PROVE REAMAINING GOALS
                    return this.backwardChaining(goals, weight, substRule, depth, causes);
                }
            }
        }

        Logger.logInference(goal + " is not satisfied (" + depth + ")", depth);
        // if (substRule == null || !(goal instanceof DataPropertyAtom) &&
        // !goal.allVariablesBound()) {
        if (substRule == null) {
            Logger.logInference("Stopping as goal : " + goal + " cannot be proven and no rule substitution exists.", depth);
            return false;
        }

        // --- IF GOAL CANNOT BE SATISFIED THEN DECREASE WEIGHT
        float newWeight = weight - goal.getWeight();
        // --> IF REMAINING WEIGHT IS TOO LOW THEN RETURN NULL
        if (newWeight / substRule.getTotalWeight() < substRule.getThreshold()) {
            Logger.logInference("Fail" + ((depth == 0) ? "\n" : ""), depth);
            return false;
        }
        
        // --> OTHERWISE TRY TO PROVE REMAINING GOALS
        return this.backwardChaining(goals, newWeight, substRule, depth, causes);
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

    private HashMap<Variable, Set<OWLPropertyAssertionObject>> findGoals(Atom atom) {
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
                values =
                    this.ontologyWrapper.getObjectPropertiesDomains().get(binaryAtom.getIRI()).stream()
                    .flatMap(d -> d.nestedClassExpressions())
                    .flatMap(c ->
                        this.ontologyWrapper.getIndividualsOfClass(c.asOWLClass().getIRI()).stream())
                    .collect(Collectors.toSet());

                if (values.size() == 0) {
                    values = this.ontologyWrapper.getIndividuals();
                }

                variablesValues.put(binaryAtom.getFirstVariable(), values);

                values =
                    this.ontologyWrapper.getObjectPropertiesRanges().get(binaryAtom.getIRI()).stream()
                    .flatMap(d -> d.nestedClassExpressions())
                    .flatMap(c ->
                    this.ontologyWrapper.getIndividualsOfClass(c.asOWLClass().getIRI()).stream())
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

    private HashMap<Variable, Set<OWLPropertyAssertionObject>> findVariableSubstitutions(Atom goal) {
        HashMap<Variable, Set<OWLPropertyAssertionObject>> variablesSubsitutions = new HashMap<>();

        if (goal instanceof UnaryAtom) {
            Variable variable = ((UnaryAtom) goal).getVariable();

            if (!variable.hasValue()) {
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

            if (!variable1.hasValue()) {
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

            if (!variable2.hasValue()) {
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

                    if (goalVar.hasValue())
                        ruleHeadVar.setValue(goalVar.getValue());
                } else if (goal instanceof BinaryAtom) {
                    BinaryAtom binaryGoal = (BinaryAtom) goal;
                    Variable firstRuleHeadVar = ((BinaryAtom) tempRule.getHead()).getFirstVariable();
                    Variable secondRuleHeadVar = ((BinaryAtom) tempRule.getHead()).getSecondVariable();
                    Variable firstGoalVar = binaryGoal.getFirstVariable();
                    Variable secondGoalVar = binaryGoal.getSecondVariable();

                    if (firstGoalVar.hasValue())
                        firstRuleHeadVar.setValue(firstGoalVar.getValue());

                    if (secondGoalVar.hasValue())
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