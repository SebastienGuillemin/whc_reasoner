package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;
import java.util.HashSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.DVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;

public class DataPropertyAtom extends AbstractBinaryAtom {

    public DataPropertyAtom(IRI reference) {
        super(reference);
    }

    @Override
    public void setSecondVariable(Variable variable) throws VariableTypeException, URIReferenceException {
        if (variable.getType() != VariableType.D_VARIABLE)
            throw new VariableTypeException(
                    "The first variable of an object property atom must be a " + VariableType.I_VARIABLE);

        if (this.secondVariable != null && !this.secondVariable.getIRI().equals(variable.getIRI()))
            throw new URIReferenceException("The new and the old variable must have the same URI reference.");

        this.secondVariable = (DVariable) variable;
    }

    @Override
    public OWLAxiom toOWLAxiom() {
        return new OWLDataPropertyAssertionAxiomImpl(
                (OWLNamedIndividual) this.firstVariable.getValue(),
                new OWLDataPropertyImpl(this.iri),
                (OWLLiteral) this.secondVariable.getValue(),
                new HashSet<>());
    }

    @Override
    public Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        DataPropertyAtom newAtom = new DataPropertyAtom(this.iri);
        super.copyAttributes(newAtom, variables, copyVariable);

        return newAtom;
    }

    @Override
    public String toPrettyString() {
        return
            ((OWLNamedIndividual) this.firstVariable.getValue()).getIRI().getFragment() + " " +
            this.getIRI().getFragment() + " " +
            ((OWLLiteral) this.secondVariable.getValue()).getLiteral();
    }
}
