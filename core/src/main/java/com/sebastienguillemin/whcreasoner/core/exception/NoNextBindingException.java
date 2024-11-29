package com.sebastienguillemin.whcreasoner.core.exception;

public class NoNextBindingException extends Exception {
    public NoNextBindingException() {
        super("No next binding can be computed.");
    }
}
