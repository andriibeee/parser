package org.example.model;

public enum SportKind {
    SOCCER("Soccer"),
    TENNIS("Tennis"),
    HOCKEY("IceHockey"),
    BASKETBALL("Basketball");

    String value;

    SportKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
