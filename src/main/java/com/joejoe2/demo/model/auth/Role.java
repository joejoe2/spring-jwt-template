package com.joejoe2.demo.model.auth;

public enum Role {
    ADMIN("ADMIN"), MODERATE("MODERATE"), NORMAL("NORMAL");
    private final String value;
    Role(String role) {
        this.value = role;
    }

    public String toString(){
        return value;
    }
}
