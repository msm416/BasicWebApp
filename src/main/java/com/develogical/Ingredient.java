package com.develogical;

public class Ingredient {
    final String name;
    final double weightInGrams;

    public Ingredient(String name, double weightInGrams) {
        this.name = name;
        this.weightInGrams = weightInGrams;
    }

    public static Ingredient parseIngredient(String ingredientAsStr) {
        return new Ingredient("", 1.1);
    }
}
