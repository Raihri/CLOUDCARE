package com.example.cloud.care.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Disease {

    private String name;
    private int level; // Danger level: 1, 2, or 3

    public Disease() {}

    public Disease(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }
}

