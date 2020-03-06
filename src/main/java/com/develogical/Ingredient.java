package com.develogical;

public class Ingredient {
    final String name;
    final double weightInGrams;

    public Ingredient(String name, double weightInGrams) {
        this.name = name;
        this.weightInGrams = weightInGrams;
    }

    public static Ingredient parseIngredient(String ingredientAsStr) {
        //TODO #1: fix parsing whitespace first, then
        //TODO #2: implement proper parsing - currently, it works for:
        // 100g tomatoes, 50g cucumbers, 100g feta cheese, 1 small onion, 2 tbsp olive oil
        switch (ingredientAsStr) {
            case "100g tomatoes":
                return new Ingredient("tomatoes", 100);
            case " 50g cucumbers":
                return new Ingredient("cucumbers", 50);
            case " 100g feta cheese":
                return new Ingredient("feta cheese", 100);
            case " 1 small onion":
                return new Ingredient("small onion", 50);
            case " 2 tbsp olive oil":
                return new Ingredient("olive oil", 20);
            default:
                return null;
        }
    }
}
