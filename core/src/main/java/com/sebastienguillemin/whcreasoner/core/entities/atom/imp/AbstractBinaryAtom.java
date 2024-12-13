package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Sets;
import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BinaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.UnaryAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;

abstract class AbstractBinaryAtom extends BaseAtom implements BinaryAtom {
    protected IVariable firstVariable;
    protected Variable secondVariable;

    public AbstractBinaryAtom(IRI reference) {
        super(reference);
    }

    @Override
    public IVariable getFirstVariable() {
        return this.firstVariable;
    }

    @Override
    public void setFirstVariable(IVariable variable) throws VariableTypeException, URIReferenceException {
        if (variable.getType() != VariableType.I_VARIABLE)
            throw new VariableTypeException(
                    "The first variable of an object property atom must be a " + VariableType.I_VARIABLE);

        if (this.firstVariable != null && !this.firstVariable.getIRI().equals(variable.getIRI()))
            throw new URIReferenceException("The new and the old variable must have the same URI reference. Old : " + this.firstVariable.getIRI() + ", New : " + this.secondVariable.getIRI());

        this.firstVariable = variable;
    }

    @Override
    public Variable getSecondVariable() {
        return this.secondVariable;
    }

    @Override
    public Set<Variable> getVariables() {
        return Sets.newHashSet(this.firstVariable, this.secondVariable);
    }

    @Override
    public String variablesToString() {
        String str = (this.firstVariable.isConstant()) ? this.firstVariable.getValue().toString() : this.firstVariable.toShortString() + ", ";
        str += (this.secondVariable.isConstant()) ? this.secondVariable.getValue().toString() : this.secondVariable.toShortString();

        return str;
    }

    @Override
    public int compareTo(Atom atomCompareTo) {
        if (atomCompareTo instanceof BuiltInAtom)
            if (((BuiltInAtom) atomCompareTo).allVariablesBound())
                return 1;
            else
                return -1;

        // If atomCompareTo is a Class or a Datatype atom and its variable has a value, return 1
        // Else, return 0
        if (atomCompareTo instanceof UnaryAtom) {
            if (((UnaryAtom) atomCompareTo).getVariable().hasValue())
                return 1;
            
            return -1;
        }

        // If atomCompareTo is a Binary atom,
        // return 0 if same IRI and same variables
        // return 1 it has more affected variables
        // Return -1 if this has the same number of affected variable or more affected variable
        else {
            BinaryAtom binaryAtom = (BinaryAtom) atomCompareTo;

            if (this.iri.equals(atomCompareTo.getIRI()) && this.firstVariable.equals(binaryAtom.getFirstVariable()) && this.secondVariable.equals(binaryAtom.getSecondVariable()))
                return 0;

            int thisAffectedVariables = (this.firstVariable.hasValue() ? 1 : 0) + (this.secondVariable.hasValue() ? 1 : 0);
            int otherAffectedVariables = (binaryAtom.getFirstVariable().hasValue() ? 1 : 0) + (binaryAtom.getSecondVariable().hasValue() ? 1 : 0);

            if (thisAffectedVariables == otherAffectedVariables) return -1;

            return otherAffectedVariables - thisAffectedVariables;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AbstractBinaryAtom))
            return false;

        AbstractBinaryAtom bAtom = (AbstractBinaryAtom) other;
        return this.iri.equals(bAtom.getIRI()) && this.firstVariable.equals(bAtom.getFirstVariable()) && this.secondVariable.equals(bAtom.getSecondVariable());
    }

    @Override
    public boolean allVariablesBound() {
        return (this.firstVariable.hasValue()) && (this.secondVariable.hasValue());
    }


    protected void copyAttributes(AbstractBinaryAtom newAtom, HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        super.copyAttributes(newAtom);

        // If first variable does not exist for new atom
        if (!variables.containsKey(this.firstVariable.getIRI())){
            // Copy first variable if needed (i.e., it is a constant or "copyVariable is True)")
            if (this.firstVariable.isConstant() || copyVariable && (this.firstVariable.hasValue()))
                variables.put(this.firstVariable.getIRI(), this.firstVariable.copyVariable(newAtom));

            // Create new IVar
            else if(this.firstVariable.getType() == VariableType.I_VARIABLE)
                variables.put(this.firstVariable.getIRI(), IVariable.createIVariable(this.firstVariable.getIRI(), newAtom));

            // Create new DVar
            else if(this.firstVariable.getType() == VariableType.D_VARIABLE)
                variables.put(this.firstVariable.getIRI(), IVariable.createDVariable(this.firstVariable.getIRI(), newAtom));
        }
        // Set new atom first variable
        newAtom.firstVariable = (IVariable) variables.get(this.firstVariable.getIRI());

        // If second variable does not exist for new atom
        if (!variables.containsKey(this.secondVariable.getIRI())) {
            // Copy second variable if needed (i.e., it is a constant or "copyVariable is True)")
            if (this.secondVariable.isConstant() || (copyVariable && (this.secondVariable.hasValue())))
                variables.put(this.secondVariable.getIRI(), this.secondVariable.copyVariable(newAtom));
    
            // Create new IVar
            else if(this.secondVariable.getType() == VariableType.I_VARIABLE)
                variables.put(this.secondVariable.getIRI(), IVariable.createIVariable(this.secondVariable.getIRI(), newAtom));
    
            // Create new DVar
            else if(this.secondVariable.getType() == VariableType.D_VARIABLE)
                variables.put(this.secondVariable.getIRI(), IVariable.createDVariable(this.secondVariable.getIRI(), newAtom));
        }
        // Set atom second variable
        newAtom.secondVariable = variables.get(this.secondVariable.getIRI());

        // Add new atom to variables 
        variables.get(this.firstVariable.getIRI()).addAtom(newAtom);
        variables.get(this.secondVariable.getIRI()).addAtom(newAtom);
    }
}
