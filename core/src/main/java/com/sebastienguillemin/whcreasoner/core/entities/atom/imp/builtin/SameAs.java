package com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.AbstractBuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.parser.BuiltInReference;

public class SameAs extends AbstractBuiltInAtom {
    public SameAs() {
        super(BuiltInReference.SAME_AS.getIri());
        this.arity = 2;
    }

    @Override
    protected SameAs copyBuiltin() {
        return new SameAs();
    }

    @Override
    public boolean isSatisfied() {
        OWLNamedIndividual individual1 = (OWLNamedIndividual) this.variables.get(0).getValue();
        OWLNamedIndividual individual2 = (OWLNamedIndividual) this.variables.get(1).getValue();

        return (individual1 != null && individual2 != null && individual1.getIRI().equals(individual2.getIRI()));
    }

    public String toPrettyString() {
        Variable firstVariable = this.variables.get(0);
        Variable secondVariable = this.variables.get(1);

        return 
            ((OWLNamedIndividual) firstVariable.getValue()).getIRI().getFragment() + " and " +
            ((OWLNamedIndividual) secondVariable.getValue()).getIRI().getFragment() + " are the same";
    }
}
