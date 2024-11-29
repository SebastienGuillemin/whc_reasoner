package com.sebastienguillemin.whcreasoner.core.entities.variable;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;

public class IVariable extends Variable {
    protected IVariable(IRI reference, Atom atom) {
        super(reference, VariableType.I_VARIABLE, atom);
    }

    @Override
    public IVariable copyVariable(Atom atom) {
        IVariable newVariable = new IVariable(iri, atom);

        super.copyAttributes(newVariable);

        return newVariable;
    }
}
