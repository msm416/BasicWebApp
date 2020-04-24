package com.develogical;

import java.net.URI;
import java.net.URISyntaxException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActualDBController implements DBController {

    public static Connection getConnection() throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        Connection connection = DriverManager.getConnection(dbUrl);

        return connection;
    }

    public String lookupMeal(String meal) {
        String SQL = "SELECT ingredients FROM meals WHERE name = ?";
        String ingredientsAsStr = "";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, meal);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            ingredientsAsStr = rs.getString("ingredients");
            System.out.println("Our ingredients are: " + ingredientsAsStr);
        } catch (SQLException | URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return ingredientsAsStr;
    }

    @Override
    public List<Ingredient> lookupMealIngredients(String meal) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (String ingredientAsStr : lookupMeal(meal).split(",")) {
            if (ingredientAsStr.equals("")) {
                continue;
            }
            ingredients.add(Ingredient.parseIngredient(ingredientAsStr));
        }
        return ingredients;
    }

    @Override
    public double lookupIngredientNutrition(Ingredient ingredient) {
        String SQL = "SELECT nutritional_info FROM ingredients WHERE name = ?";
        double ingredientKcal = 0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, ingredient.name);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            String nutrInfo = rs.getString("nutritional_info");
            ingredientKcal =
                    Integer.parseInt(
                            nutrInfo
                                    .substring(0, nutrInfo.length() - "kcal".length()))
                            * ingredient.weightInGrams
                            / 100;
            System.out.println("Our ingredient has kcals: " + ingredientKcal);
        } catch (SQLException | URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return ingredientKcal;
    }

    @Override
    public String lookupTopMealByComplexity(int dishComplexity) {
        String SQL = "SELECT NAME, INGREDIENTS FROM MEALS WHERE NUM_INGREDIENTS <= ? ORDER BY NUM_INGREDIENTS DESC LIMIT 1;\n";
        String suggestedMeal = "";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, dishComplexity);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            suggestedMeal = rs.getString("name");
            suggestedMeal +=":";
            suggestedMeal += rs.getString("ingredients");
            System.out.println("Our suggested meal is: " + suggestedMeal);
        } catch (SQLException | URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return suggestedMeal;
    }

    @Override
    public void lookupOnApiIngredientDetails(String ingredient) {
        String responseMsg;
        HttpResponse<String> response;
        try {
            response = Unirest.get("https://edamam-food-and-grocery-database.p.rapidapi.com/parser?ingr=" +
                    URLEncoder.encode(ingredient, StandardCharsets.UTF_8))
                    .header("x-rapidapi-host", "edamam-food-and-grocery-database.p.rapidapi.com")
                    .header("x-rapidapi-key", "14e5a268d5mshd3e603cee2da246p162b02jsnb4e5c036d21a")
                    .asString();
            responseMsg = response.getStatus() + "";
        } catch (UnirestException e) {
            e.printStackTrace();
            responseMsg = e.getMessage();
        }
        //System.out.println(responseMsg);
    }
}
