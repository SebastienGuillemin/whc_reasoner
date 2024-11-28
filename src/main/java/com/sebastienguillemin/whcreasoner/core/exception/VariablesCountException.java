package com.sebastienguillemin.whcreasoner.core.exception;

import com.sebastienguillemin.whcreasoner.core.entities.atom.Atom;

public class VariablesCountException extends Exception {
    public VariablesCountException(Atom atom, int currentCount, int allowedMaxCount) {
        super("Too many vartiable for atom " + atom.getIRI() + ". Max nuymber of variables allowed : " + allowedMaxCount + ", current count : " + currentCount);
    }
}
