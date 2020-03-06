package com.develogical;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActualDBController implements DBController {

    //TODO: Measure time in ADBC, log times to heroku, pull from heroku ^ build model
    public static Connection getConnection() throws URISyntaxException, SQLException {
        String dbUrl = System.getenv("JDBC_DATABASE_URL");
        Connection connection = DriverManager.getConnection(dbUrl);

        return connection;
    }

    public String lookupMeal(String meal) {
        String SQL = "SELECT ingredients FROM meals WHERE name = ?";
        String ingredientsAsStr = "" ;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL))
        {
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
        for(String ingredientAsStr : lookupMeal(meal).split(",")) {
            ingredients.add(Ingredient.parseIngredient(ingredientAsStr));
        }
        return ingredients;
    }

    @Override
    public double lookupIngredientNutrition(Ingredient ingredient) {
        return 0;
    }
}
