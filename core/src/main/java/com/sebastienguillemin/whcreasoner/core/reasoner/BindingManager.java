package com.sebastienguillemin.whcreasoner.core.reasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.BindingManagerException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableValueException;
import com.sebastienguillemin.whcreasoner.core.util.Logger;

import lombok.Getter;

public class BindingManager {
    private Variable[] variables;
    @Getter
    private List<List<OWLPropertyAssertionObject>> values;
    private int[] pointers;

    @Getter
    private int totalIter;
    @Getter
    private int iterCount;

    public BindingManager(HashMap<Variable, Set<OWLPropertyAssertionObject>> variablesValues)
            throws VariableValueException, BindingManagerException {

        this.variables = variablesValues.keySet().toArray(new Variable[0]);
        this.values = new ArrayList<>();
        this.pointers = new int[this.variables.length];
        this.totalIter = 0;
        this.iterCount = 0;

        if (variablesValues.size() == 0)
            this.totalIter = 0;
        else {
            Set<OWLPropertyAssertionObject> variableValues;
            for (int i = 0; i < this.variables.length; i++) {
                variableValues = variablesValues.get(this.variables[i]);
                if (variableValues == null)
                    variableValues = new HashSet<>();

                this.pointers[i] = -1;
                this.values.add(new ArrayList<>(variableValues));

                if (this.values.get(i).size() != 0)
                    if (this.totalIter == 0)
                        this.totalIter = this.values.get(i).size();
                    else
                        this.totalIter *= this.values.get(i).size();
            }
        }
    }

    /**
     * Indicates if a next binding is available.
     * 
     * @return {@code True} if a next binding is available, {@code False} otherwise.
     */
    public boolean hasNextBinding() {
        return this.iterCount < this.totalIter;
    }

    /**
     * Update internal pointers to the next values and bind variables to these
     * values.
     * 
     * @throws VariableValueException if the value type is incorrect for a variable.
     */
    public synchronized void nextBinding() throws VariableValueException {
        if (!this.hasNextBinding())
            return;

        boolean isZero = false;
        if (this.values.get(0).size() != 0)
            pointers[0] = ++pointers[0] % this.values.get(0).size();
        else
            pointers[0] = 0;

        isZero = pointers[0] == 0;

        for (int j = 1; j < this.variables.length; j++) {
            if (this.values.get(j).size() == 0)
                continue;

            pointers[j] = (pointers[j] + (isZero ? 1 : 0)) % this.values.get(j).size();
            isZero = isZero & pointers[j] == 0;

            if (!isZero)
                break;
        }
        
        this.bind();

        this.iterCount++;
    }

    /**
     * Bind values to variables in {@code variables} attribute.
     * 
     * @throws VariableValueException when an error occurs while binding
     */
    private void bind() throws VariableValueException {
        for (int i = 0; i < this.variables.length; i++) {
            if (this.values.get(i).size() > 0 && !this.variables[i].isConstant()) {
                List<OWLPropertyAssertionObject> variableValues = this.values.get(i);
                int pointer = this.pointers[i];
                
                try {
                    this.variables[i].setValue(variableValues.get(pointer));
                    // Util.logInfo("Bound " + variableValues.get(pointer) + " to " + this.variables[i]);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Logger.logInfo(" i = " + i + ", pointer : " + pointer + ", this.values.get(i) : "
                            + this.values.get(i).size());
                    System.exit(0);
                }
            }
        }
    }
}