package com.sebastienguillemin.whcreasoner.core.entities.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.BaseEntity;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.NAryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.UnaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.DatatypeAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.ObjectPropertyAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.InvalidRuleException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import lombok.Getter;

@Getter
public class Rule extends BaseEntity {
    private Set<Atom> body;
    private float threshold;
    private Atom head;

    // Attributes used as caches
    @Getter
    private Set<Variable> variables;

    @Getter
    private Set<Variable> headVariables;
    private float totalWeight;

    public Rule(IRI reference, Set<Atom> body, Atom head, float threshold) throws InvalidRuleException {
        super(reference);
        this.body = body;
        this.head = head;

        this.headVariables = this.head.getVariables();
        this.variables = this.body.stream().flatMap(a -> a.getVariables().stream())
                .collect(Collectors.toCollection(HashSet::new));

        this.setThreshold(threshold);
        this.totalWeight = (float) this.body.stream().mapToDouble(a -> a.getWeight()).sum();
        this.validateRule();
        this.processVariables();
    }

    public Rule(Rule rule) throws InvalidRuleException, VariableValueException {
        this(rule, true);
    }

    public Rule(Rule rule, boolean copyVariable) throws InvalidRuleException, VariableValueException {
        super(rule.getIRI());
        this.body = new HashSet<>();
        
        HashMap<IRI, Variable> variables = new HashMap<>();
        for (Atom bodyAtom : rule.getBody()) {
            this.body.add(bodyAtom.copy(variables, copyVariable));
        }

        this.head = rule.head.copy(variables, copyVariable);

        this.headVariables = this.head.getVariables();
        this.variables = this.body.stream().flatMap(a -> a.getVariables().stream())
                .collect(Collectors.toCollection(HashSet::new));

        this.setThreshold(rule.threshold);
        this.totalWeight = (float) this.body.stream().mapToDouble(a -> a.getWeight()).sum();
        this.validateRule();
        this.processVariables();

        
    }

    @Override
    public String toString() {
        String str = this.iri.getFragment() + "_" + this.hashCode() + ": ";

        Iterator<Atom> i = this.body.iterator();
        Atom atom;
        while (i.hasNext()) {
            atom = i.next();
            str += atom + (i.hasNext() ? "^" : "");
        }

        str += "->" + this.head + "[" + this.threshold + "]";

        return str;
    }

    public boolean containsAtom(Atom atom) {
        for (Atom bodyAtom : this.body)
            if (bodyAtom.getIRI().equals(atom.getIRI()))
                return true;

        return false;
    }

    private void setThreshold(float threshold) throws InvalidRuleException {
        if (threshold <= 0 || threshold > 1)
            throw new InvalidRuleException("Illegal threshold : " + threshold + ". Threshold must be in ]0; 1].");

        else
            this.threshold = threshold;
    }

    private void validateRule() throws InvalidRuleException {
        // Verify head atom type
        if (this.head instanceof DatatypeAtom)
            throw new InvalidRuleException("A datatype atom cannot be used as the head of a rule");

        if (this.head instanceof BuiltInAtom)
            throw new InvalidRuleException("A built-in atom cannot be used as the head of a rule");

        // Verify that the head variables that are not constant appear in the rule body
        Set<Variable> headVariablesNotConstants = this.headVariables.stream().filter(v -> !v.isConstant())
                .collect(Collectors.toSet());

        if (!this.variables.containsAll(headVariablesNotConstants))
            throw new InvalidRuleException("The head variables must appear in the rule body.");
    }

    private void processVariables() {
        for (Atom atom : this.body) {
            if (atom instanceof UnaryAtom || atom instanceof NAryAtom)
                continue;

            if (atom instanceof ObjectPropertyAtom) {
                ObjectPropertyAtom opAtom = (ObjectPropertyAtom) atom;
                Variable subject = opAtom.getFirstVariable();
                Variable object = opAtom.getSecondVariable();

                subject.getDependsOfObjects().put(object, atom);
                object.getDependsOfSubjects().put(subject, atom);
            }
        }
    }
}
