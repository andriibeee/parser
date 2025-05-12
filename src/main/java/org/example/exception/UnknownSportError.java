package org.example.exception;

public class UnknownSportError extends Exception {

    public UnknownSportError(String sportName) {
        super("Sport " + sportName + " not found");
    }
}
