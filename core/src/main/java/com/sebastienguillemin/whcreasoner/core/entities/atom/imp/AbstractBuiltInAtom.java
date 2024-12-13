package com.sebastienguillemin.whcreasoner.core.entities.atom.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;
import com.sebastienguillemin.whcreasoner.core.entities.atom.BuiltInAtom;
import com.sebastienguillemin.whcreasoner.core.entities.variable.DVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.VariableType;
import com.sebastienguillemin.whcreasoner.core.exception.OWLAxiomConversionException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;
import com.sebastienguillemin.whcreasoner.core.exception.VariablesCountException;

// TODO : conserve variables order
public abstract class AbstractBuiltInAtom extends BaseAtom implements BuiltInAtom {
    // To allow the same variable several times and keep order (cannot use a Set)
    protected List<Variable> variables; 
    protected int arity;

    public AbstractBuiltInAtom(IRI reference) {
        super(reference);
        this.variables = new ArrayList<>();
    }

    @Override
    public int getArity() {
        return this.arity;
    }

    @Override
    public void addVariable(Variable variable) throws VariablesCountException {
        if (variables.size() == arity)
            throw new VariablesCountException(this, this.variables.size(), this.arity);
        this.variables.add(variable);
    }

    @Override
    public Set<Variable> getVariables() {
        return new HashSet<>(this.variables);
    }

    @Override
    public OWLAxiom toOWLAxiom() throws OWLAxiomConversionException {
        throw new OWLAxiomConversionException("Cannot convert built-in atom to OWL axiom.");
    }

    @Override
    public String variablesToString() {
        String str = "";

        Iterator<Variable> i = this.variables.iterator();
        Variable variable;
        while (i.hasNext()) {
            variable = i.next();
            str += (variable.isConstant()) ? variable.getValue().toString() : variable.toShortString() + ((i.hasNext()) ? ", " : "");
        }

        return str;
    }

    @Override
    public int compareTo(Atom atomCompareTo) {
        if (this.iri.equals(atomCompareTo.getIRI())) {
            BuiltInAtom builtInAtom = (BuiltInAtom) atomCompareTo;

            if (this.getVariables().size() == builtInAtom.getVariables().size()) {
                Set<Variable> diff = new HashSet<Variable>(this.variables);
                diff.removeAll(builtInAtom.getVariables());
                
                if(diff.size() == 0)
                    return 0;
            }
        }

        if (!this.allVariablesBound())
            return 1;

        return -1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BuiltInAtom))
            return false;

        return this.compareTo(((BuiltInAtom) other)) == 0;
    }


    @Override
    public Atom copy(HashMap<IRI, Variable> variables, boolean copyVariable) throws VariableValueException {
        AbstractBuiltInAtom newAtom = this.copyBuiltin();
        super.copyAttributes(newAtom);

        List<Variable> newVariables = new ArrayList<>();
        Variable newVariable;
        // For each atom's variable
        for (Variable variable : this.variables) {
            // If variable does not exist for new atom
            if (!variables.containsKey(variable.getIRI())) {
                // Copy variable if needed (i.e., it is a constant or "copyVariable is True)")
                if (variable.isConstant() || copyVariable && (variable.hasValue())) {
                    newVariable = variable.copyVariable(newAtom);
                    variables.put(variable.getIRI(), newVariable);
                }

                // Create new IVar
                else if(variable.getType() == VariableType.I_VARIABLE) {
                    newVariable = IVariable.createIVariable(variable.getIRI(), newAtom);
                    variables.put(variable.getIRI(), newVariable);
                }

                // Create new DVar
                else if(variable.getType() == VariableType.D_VARIABLE) {
                    newVariable = DVariable.createIVariable(variable.getIRI(), newAtom);
                    variables.put(variable.getIRI(), DVariable.createDVariable(variable.getIRI(), newAtom));
                }
            }
            
            // Add new variable to variables table
            newVariables.add(variables.get(variable.getIRI()));

            // Add new atom to variable 
            variables.get(variable.getIRI()).addAtom(newAtom);
        }
        // Set new atom variable
        newAtom.variables = newVariables;

        return newAtom;
    }

    @Override
    public boolean allVariablesBound() {
        for (Variable variable : this.variables) {
            if (!variable.hasValue())
                return false;
        }
        
        return true;
    }

    protected abstract AbstractBuiltInAtom copyBuiltin();
}
