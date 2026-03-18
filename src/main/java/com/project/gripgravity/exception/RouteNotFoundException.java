package com.project.gripgravity.exception;

/**
 * Thrown when a route with the requested ID does not exist.
 */
public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(Long id) {
        super("Route not found with id: " + id);
    }

}
