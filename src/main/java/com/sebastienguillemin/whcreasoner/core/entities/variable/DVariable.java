package com.sebastienguillemin.whcreasoner.core.entities.variable;

import org.semanticweb.owlapi.model.IRI;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;

public class DVariable extends Variable {

    protected DVariable(IRI reference, Atom atom) {
        super(reference, VariableType.D_VARIABLE, atom);
    }

    @Override
    public DVariable copyVariable(Atom atom) {
        DVariable newVariable = new DVariable(iri, atom);

        super.copyAttributes(newVariable);

        return newVariable;
    }
}
