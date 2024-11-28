package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.DVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

public class DatatypeAtom extends AbstractUnaryAtom {

    public DatatypeAtom(IRI reference) {
        super(reference);
    }

    @Override
    public void setVariable(Variable variable) throws VariableTypeException, URIReferenceException {
        if (variable.getType() != VariableType.D_VARIABLE)
            throw new VariableTypeException("A variable in a datatype atom must be a " + VariableType.D_VARIABLE);

        if (this.variable != null && !this.variable.getIRI().equals(variable.getIRI()))
            throw new URIReferenceException("The new and the old variable must have the same URI reference."); 
            
        this.variable = (DVariable) variable;
    }

    @Override
    public OWLAxiom toOWLAxiom() {
        throw new UnsupportedOperationException("Unimplemented method 'toOWLAxiom' for data type atom.");
    }

    @Override
    public Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        DatatypeAtom newAtom = new DatatypeAtom(this.iri);
        super.copyAttributes(newAtom, variables, copyVariable);

        return newAtom;
    }
}
