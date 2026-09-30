package com.harjit.smartpantrymanager;

public class Recipe {

    private final long id;
    private final String name;
    private final String method;

    public Recipe(long id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }
}
