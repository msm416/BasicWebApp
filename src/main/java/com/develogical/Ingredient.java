package com.develogical;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ingredient {
    final static private List<Pattern> patterns = new ArrayList() {{
        add(Pattern.compile("\\s*(?<grams>\\d+)g\\s+(?<name>.*)"));
        // "100g tomatoes"
        add(Pattern.compile("\\s*(?<nbOfPieces>\\d+)\\s+(?<size>small)\\s*(?<name>.*)"));
        // "1 small onion"
        add(Pattern.compile("\\s*(?<nbOfPieces>\\d+)\\s+(?<size>medium)\\s*(?<name>.*)"));
        // "4 medium salad"
        add(Pattern.compile("\\s*(?<nbOfPieces>\\d+)\\s+(?<size>large)\\s*(?<name>.*)"));
        // "3 large kale"
        add(Pattern.compile("\\s*(?<nbOfPieces>\\d+)\\s+(?<size>tbsp)\\s*(?<name>.*)"));
        // "2 tbsp olive oil
        add(Pattern.compile("\\s*(?<nbOfPieces>\\d+)\\s+(?<size>slices of)\\s*(?<name>.*)"));
        // 3 slices of loaf
    }};

    final String name;
    final double weightInGrams;

    public Ingredient(String name, double weightInGrams) {
        this.name = name;
        this.weightInGrams = weightInGrams;
    }

    public static Ingredient parseIngredient(String ingredientAsStr) {
        for(int i = 0; i < patterns.size(); i++) {
            Matcher matcher = patterns.get(i).matcher(ingredientAsStr);
            if(matcher.find()) {
                //TODO: make this better
                if(i == 0) {
                    return new Ingredient(matcher.group("name"), Double.parseDouble(matcher.group("grams")));
                }

                int gramsPerPiece;

                switch (matcher.group("size")) {
                    case "small":
                        gramsPerPiece = 50;
                        break;
                    case "medium":
                        gramsPerPiece = 75;
                        break;
                    case "large":
                        gramsPerPiece = 100;
                        break;
                    case "tbsp":
                        // WE ASSUME FOR SIMPLICITY THAT THE INGREDIENTS HAVE SIMILAR DENSITY TO WATER
                        // SO IGNORE THE FACT THAT TBSP IS A MEASURE FOR VOLUME
                        gramsPerPiece = 15;
                        break;
                    default:
                        //case "slices of":
                        gramsPerPiece = 25;
                }
                return new Ingredient(matcher.group("name"),
                        Integer.parseInt(matcher.group("nbOfPieces")) * gramsPerPiece);
            }
        }

        return new Ingredient("INVALID: " + ingredientAsStr, 0); // couldn't parse ingredient
    }
}
