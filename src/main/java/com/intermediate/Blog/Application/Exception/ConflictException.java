package com.intermediate.Blog.Application.Exception;

/**
 * Thrown when a request would conflict with existing data (for example,
 * attempting to use a username or email that is already taken by another user).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

