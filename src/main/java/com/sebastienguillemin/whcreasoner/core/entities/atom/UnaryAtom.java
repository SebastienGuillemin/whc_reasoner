package com.sebastienguillemin.whcreasoner.core.entities.atom;

import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.URIReferenceException;
import com.sebastienguillemin.whcreasoner.core.exception.VariableTypeException;

public interface UnaryAtom extends Atom {
    Variable getVariable();
    void setVariable(Variable variable) throws VariableTypeException, URIReferenceException;
}
