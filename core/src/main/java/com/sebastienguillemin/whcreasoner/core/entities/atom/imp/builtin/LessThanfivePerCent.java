package com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin;

import org.semanticweb.owlapi.model.OWLLiteral;

import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.AbstractBuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.parser.BuiltInReference;


public class LessThanfivePerCent extends AbstractBuiltInAtom {
    public LessThanfivePerCent() {
        super(BuiltInReference.LESS_THAN_FIVE_PERCENT.getIri());
        this.arity = 2;
    }

    @Override
    public boolean isSatisfied() {
        OWLLiteral literal1 = (OWLLiteral) this.variables.get(0).getValue();
        OWLLiteral literal2 = (OWLLiteral) this.variables.get(1).getValue();

        if (literal1 != null && literal2 != null) {
            float val1 = Float.parseFloat(literal1.getLiteral());
            float val2 = Float.parseFloat(literal2.getLiteral());

            return (100f * Math.abs(val1 - val2) / ((val1 + val2) / 2f)) <= 5f;
        }
        return false;
    }

    @Override
    protected AbstractBuiltInAtom copyBuiltin() {
        return new LessThanfivePerCent();
    }
    
    public String toPrettyString() {
        Variable firstVariable = this.variables.get(0);
        Variable secondVariable = this.variables.get(1);

        return 
            (firstVariable.isConstant() ? "" : firstVariable.getIRI().getFragment() + "=") +
            ((OWLLiteral) firstVariable.getValue()).getLiteral() + 
            " and " +
            (secondVariable.isConstant() ? "" : secondVariable.getIRI().getFragment() + "=") +
            ((OWLLiteral) secondVariable.getValue()).getLiteral() +
            " are less than 5% different";
    }
}
