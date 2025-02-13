package com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin;

import org.semanticweb.owlapi.model.OWLLiteral;

import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.AbstractBuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.parser.BuiltInReference;

public class LessThanOrEqual extends AbstractBuiltInAtom {
    public LessThanOrEqual() {
        super(BuiltInReference.LESS_THAN_EQUAL.getIri());
        this.arity = 2;
    }

    @Override
    public boolean isSatisfied() {
        OWLLiteral literal1 = (OWLLiteral) this.variables.get(0).getValue();
        OWLLiteral literal2 = (OWLLiteral) this.variables.get(1).getValue();

        return (literal1 != null && literal2 != null && Float.parseFloat(literal1.getLiteral()) <= Float.parseFloat(literal2.getLiteral()));
    }

    @Override
    protected AbstractBuiltInAtom copyBuiltin() {
        return new LessThanOrEqual();
    }

    public String toPrettyString() {
        Variable firstVariable = this.variables.get(0);
        Variable secondVariable = this.variables.get(1);

        return 
            (firstVariable.isConstant() ? "" : firstVariable.getIRI().getFragment() + "=") +
            ((OWLLiteral) firstVariable.getValue()).getLiteral() + 
            " <= " +
            (secondVariable.isConstant() ? "" : secondVariable.getIRI().getFragment() + "=") +
            ((OWLLiteral) secondVariable.getValue()).getLiteral();
    }
}
