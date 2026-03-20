package com.intermediate.Blog.Application.Exception;

public class AccessControlException extends RuntimeException{

    public AccessControlException(String message){
        super(message);
    }
}
