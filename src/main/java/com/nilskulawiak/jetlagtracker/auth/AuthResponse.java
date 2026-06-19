package com.nilskulawiak.jetlagtracker.auth;

public record AuthResponse(String sessionToken, UserDTO user) {}
