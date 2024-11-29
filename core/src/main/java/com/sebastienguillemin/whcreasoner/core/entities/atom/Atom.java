package com.sebastienguillemin.whcreasoner.core.entities.atom;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.sebastienguillemin.whcreasoner.core.entities.NamedEntity;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.AtomWeightException;
import com.sebastienguillemin.whcreasoner.core.exception.OWLAxiomConversionException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

// TODO : optimise variables access in an atom.

// TODO : faire en sorte que les fonctions "compareTo" prennent en compte le poid.
public interface Atom extends NamedEntity, Comparable<Atom> {
    void isHead(boolean isHead);

    boolean isHead();

    float getWeight();

    void setWeight(float Weight) throws AtomWeightException;

    Set<Variable> getVariables();

    OWLAxiom toOWLAxiom() throws OWLAxiomConversionException;

    OWLAxiom toSymmetricOWLAxiom() throws OWLAxiomConversionException;

    UUID getUniqueID();

    /**
     * Copy an atom.
     * 
     * @param variables        List of varibales already copied
     * @param copyNullVariable If true copy null-valued variable. If false, create a
     *                         new instance of each null-values variable.
     * @return A new Atom instance.
     */
    Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException;

    boolean allVariablesBound();
}
