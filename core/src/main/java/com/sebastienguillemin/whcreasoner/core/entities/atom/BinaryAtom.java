package com.sebastienguillemin.whcreasoner.core.entities.atom;

import com.sebastienguillemin.whcreasoner.core.entities.variable.IVariable;
import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;

public interface BinaryAtom extends Atom {
    IVariable getFirstVariable();

    void setFirstVariable(IVariable variable) throws VariableTypeException, URIReferenceException;

    Variable getSecondVariable();

    void setSecondVariable(Variable variable) throws VariableTypeException, URIReferenceException;
}
