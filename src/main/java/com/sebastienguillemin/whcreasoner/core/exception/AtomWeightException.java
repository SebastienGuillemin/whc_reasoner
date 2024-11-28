package com.sebastienguillemin.whcreasoner.core.exception;

public class AtomWeightException extends Exception {
    public AtomWeightException(float weight) {
        super("Invalid atom weight : " + weight + ". A weight must be in ]0; 1].");
    }
}
