package com.joejoe2.demo.exception;

public class ControllerAuthConstraintViolation extends Exception{
    private final int rejectStatus;
    private final String rejectMessage;

    public ControllerAuthConstraintViolation(int rejectStatus, String rejectMessage) {
        this.rejectStatus = rejectStatus;
        this.rejectMessage = rejectMessage;
    }

    public int getRejectStatus() {
        return rejectStatus;
    }

    public String getRejectMessage() {
        return rejectMessage;
    }
}
