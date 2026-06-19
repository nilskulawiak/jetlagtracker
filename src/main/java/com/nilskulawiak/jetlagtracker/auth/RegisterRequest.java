package com.nilskulawiak.jetlagtracker.auth;

public record RegisterRequest(String email, String displayName, String password) {}
