package com.sebastienguillemin.whcreasoner.core.exception;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;

public class AtomTypeException extends Exception {
    public AtomTypeException(Atom atom, boolean mustBeHead) {
        super("Wrong type for atom " + atom + ((mustBeHead) ? ", must be head atom." : " must be body atom." ));
    }
}
