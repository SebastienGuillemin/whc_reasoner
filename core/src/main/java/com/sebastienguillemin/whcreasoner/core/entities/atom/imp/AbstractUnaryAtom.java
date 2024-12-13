package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.UnaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

import lombok.Getter;

public abstract class AbstractUnaryAtom extends BaseAtom implements UnaryAtom {
    @Getter
    protected Variable variable;

    public AbstractUnaryAtom(IRI reference) {
        super(reference);
    }

    @Override
    public String variablesToString() {
        return this.variable.toShortString();
    }

    @Override
    public Set<Variable> getVariables() {
        return Sets.newHashSet(this.variable);
    }

    @Override
    public boolean allVariablesBound() {
        return this.variable.hasValue();
    }

    @Override
    public int compareTo(Atom atomCompareTo) {
        if (atomCompareTo instanceof BuiltInAtom)
            if (((BuiltInAtom) atomCompareTo).allVariablesBound())
                return 1;
            else
                return -1;

        if (!this.variable.hasValue())
            return 1;
            
        // If atomCompareTo is an Unary atom, has the same IRI and same variable, return 0
        if (atomCompareTo instanceof UnaryAtom) {
            UnaryAtom uAtom = (UnaryAtom) atomCompareTo;

            if (this.iri.equals(uAtom.getIRI()) && this.variable.equals(uAtom.getVariable()))
                return 0;
        }
        
        // If this has no value, return 1.
        if (!this.variable.hasValue())
            return 1;

        return -1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnaryAtom))
            return false;

            UnaryAtom uAtom = (UnaryAtom) other;
        return this.iri.equals(uAtom.getIRI()) && this.variable.compareTo(uAtom.getVariable()) == 0;
    }

    protected void copyAttributes(AbstractUnaryAtom newAtom, HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        super.copyAttributes(newAtom);

        // If variable does not exist for new atom
        if (!variables.containsKey(variable.getIRI())) {
            // Copy variable if needed (i.e., it is a constant or "copyVariable is True)")
            if (this.variable.isConstant() || copyVariable && (this.variable.hasValue()))
                variables.put(variable.getIRI(), variable.copyVariable(newAtom));

            // Create new IVar
            else if(variable.getType() == VariableType.I_VARIABLE)
                variables.put(variable.getIRI(), IVariable.createIVariable(variable.getIRI(), newAtom));

            // Create new DVar
            else if(variable.getType() == VariableType.D_VARIABLE)
                variables.put(variable.getIRI(), IVariable.createDVariable(variable.getIRI(), newAtom));
        }

        // Set new atom variable
        newAtom.variable = variables.get(this.variable.getIRI());
        
        // Add new atom to variable 
        variables.get(this.variable.getIRI()).addAtom(newAtom);
    }
}
