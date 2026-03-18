package com.project.gripgravity.exception;

import com.project.gripgravity.model.RouteStatus;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(RouteStatus from, RouteStatus to) {
        super("Invalid state transition from " + from + " to " + to);
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }

}
