package com.nutritrack.model;

public record User(long id, String name, String email, String passwordHash) {
}
