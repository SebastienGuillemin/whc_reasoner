package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;
import java.util.HashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class ClassAtom extends AbstractUnaryAtom {
    public ClassAtom(IRI reference) {
        super(reference);
    }

    @Override
    public void setVariable(Variable variable) throws VariableTypeException, URIReferenceException {
        if (variable.getType() != VariableType.I_VARIABLE)
            throw new VariableTypeException("A variable in a class atom must be a " + VariableType.I_VARIABLE);

        if (this.variable != null && !this.variable.getIRI().equals(variable.getIRI()))
            throw new URIReferenceException("The new and the old variable must have the same URI reference.");

        this.variable = (IVariable) variable;
    }

    @Override
    public OWLAxiom toOWLAxiom() {
        return new OWLClassAssertionAxiomImpl(
                (OWLNamedIndividual) this.getVariable().getValue(),
                new OWLClassImpl(this.iri),
                new HashSet<>());
    }

    @Override
    public Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        ClassAtom newAtom = new ClassAtom(this.iri);
        super.copyAttributes(newAtom, variables, copyVariable);

        return newAtom;
    }

    @Override
    public String toPrettyString() {
        return ((OWLNamedIndividual) this.variable.getValue()).getIRI() + " is a " + this.getIRI().getFragment();
    }
}
