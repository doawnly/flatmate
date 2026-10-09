package com.flatmate.controller;

public record ApiError (
        int status,
        String error,
        String message
){
}
