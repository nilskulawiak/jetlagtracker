package com.nilskulawiak.jetlagtracker.auth;

import java.util.UUID;

public record UserDTO(UUID id, String email, String displayName) {}
