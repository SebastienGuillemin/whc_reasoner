package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.sebastienguillemin.whcreasoner.core.entities.BaseEntity;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.AtomWeightException;
import com.sebastienguillemin.whcreasoner.core.exception.OWLAxiomConversionException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import lombok.Getter;

/**
 * The comparable interface is used to order the atom.
 * 
 * @see Variable#getOrderedAtoms
 */
abstract class BaseAtom extends BaseEntity implements Atom {
    @Getter    
    private boolean head;

    @Getter
    private float weight;

    @Getter
    private UUID uniqueID = UUID.randomUUID();

    public BaseAtom(IRI reference) {
        super(reference);
    }
    
    @Override
    public void isHead(boolean isHead) {
        this.head = isHead;
    }

    @Override
    public void setWeight(float weight) throws AtomWeightException {
        if (weight <= 0 || weight > 1)  
            throw new AtomWeightException(weight);
        this.weight = weight;
    }

    @Override
    public Atom copy() throws VariableValueException {
        return this.copy(new HashMap<>(), true);
    }

    @Override
    public String toString() {
        return (!this.isHead() ? this.weight + "*" : "") +
                this.iri.getFragment() +
                "(" + this.variablesToString() +")";
    }

    protected abstract String variablesToString();

    @Override
    public abstract OWLAxiom toOWLAxiom() throws OWLAxiomConversionException;

    @Override
    public OWLAxiom toSymmetricOWLAxiom() throws OWLAxiomConversionException {
        return this.toOWLAxiom();
    }

    protected void copyAttributes(BaseAtom newAtom) {
        newAtom.iri = this.iri;
        newAtom.head = this.head;
        newAtom.weight = this.weight;
        newAtom.uniqueID = this.uniqueID;
    }
}
