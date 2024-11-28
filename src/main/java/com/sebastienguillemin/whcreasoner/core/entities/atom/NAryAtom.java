package com.sebastienguillemin.whcreasoner.core.entities.atom;

import com.sebastienguillemin.whcreasoner.core.entities.variable.Variable;
import com.sebastienguillemin.whcreasoner.core.exception.VariablesCountException;

public interface NAryAtom extends Atom {
    void addVariable(Variable variable) throws VariablesCountException;

    int getArity();
}
