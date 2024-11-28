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

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

public class ObjectPropertyAtom extends AbstractBinaryAtom {

    public ObjectPropertyAtom(IRI reference) {
        super(reference);
    }

    @Override
    public void setSecondVariable(Variable variable) throws VariableTypeException, URIReferenceException {
        if (variable.getType() != VariableType.I_VARIABLE)
            throw new VariableTypeException(
                    "The first variable of an object property atom must be a " + VariableType.I_VARIABLE);

        if (this.secondVariable != null && !this.secondVariable.getIRI().equals(variable.getIRI()))
            throw new URIReferenceException("The new and the old variable must have the same URI reference.");

        this.secondVariable = (IVariable) variable;
    }

    @Override
    public OWLAxiom toOWLAxiom() {
        return new OWLObjectPropertyAssertionAxiomImpl(
                (OWLNamedIndividual) this.getFirstVariable().getValue(),
                new OWLObjectPropertyImpl(this.iri),
                (OWLNamedIndividual) this.getSecondVariable().getValue(),
                new HashSet<>());
    }

    @Override
    public OWLAxiom toSymmetricOWLAxiom() {
        return new OWLObjectPropertyAssertionAxiomImpl(
                (OWLNamedIndividual) this.getSecondVariable().getValue(),
                new OWLObjectPropertyImpl(this.iri),
                (OWLNamedIndividual) this.getFirstVariable().getValue(),
                new HashSet<>());
    }

    @Override
    public Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        ObjectPropertyAtom newAtom = new ObjectPropertyAtom(this.iri);
        super.copyAttributes(newAtom, variables, copyVariable);

        return newAtom;
    }
}
