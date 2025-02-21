package com.sebastienguillemin.whcreasoner.core.entities.atom.imp.builtin;

import org.semanticweb.owlapi.model.OWLLiteral;

import com.sebastienguillemin.whcreasoner.core.entities.atom.imp.AbstractBuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.parser.BuiltInReference;
import com.sebastienguillemin.whcreasoner.core.util.MathUtil;

// TODO : to complete
public class DiffValuesIn extends AbstractBuiltInAtom {
    public DiffValuesIn() {
        super(BuiltInReference.DIFF_VALUES_IN.getIri());
        this.arity = 4;
    }

    @Override
    public boolean isSatisfied() {
        float value1 = Float.parseFloat(((OWLLiteral) this.variables.get(0).getValue()).getLiteral());
        float value2 = Float.parseFloat(((OWLLiteral) this.variables.get(1).getValue()).getLiteral());
        float lowerBound = Float.parseFloat(((OWLLiteral) this.variables.get(2).getValue()).getLiteral());
        float upperBound = Float.parseFloat(((OWLLiteral) this.variables.get(3).getValue()).getLiteral());

        float diff = MathUtil.computeDiffPercentage(value1, value2);

        System.out.println(lowerBound + ", " + upperBound);

        return lowerBound <= diff && diff <= upperBound;
    }

    @Override
    public String toPrettyString() {
        float value1 = Float.parseFloat(((OWLLiteral) this.variables.get(0).getValue()).getLiteral());
        float value2 = Float.parseFloat(((OWLLiteral) this.variables.get(1).getValue()).getLiteral());
        float lowerBound = Float.parseFloat(((OWLLiteral) this.variables.get(2).getValue()).getLiteral());
        float upperBound = Float.parseFloat(((OWLLiteral) this.variables.get(3).getValue()).getLiteral());

        float diff = MathUtil.computeDiffPercentage(value1, value2);

        System.out.println("Diff " + value1 + ", " + value2 + " = " + diff);

        return "difference between " + value1 + " and " + value2 + " (" + diff + "%) in [" + lowerBound + "; " + upperBound + "]"; 
    }

    @Override
    protected AbstractBuiltInAtom copyBuiltin() {
        return new DiffValuesIn();
    }

}
