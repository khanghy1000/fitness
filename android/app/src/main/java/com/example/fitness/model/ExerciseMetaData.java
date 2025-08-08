package com.example.fitness.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExerciseMetaData {
    private String name;
    private int imageResourceId;
    private String label;
}
