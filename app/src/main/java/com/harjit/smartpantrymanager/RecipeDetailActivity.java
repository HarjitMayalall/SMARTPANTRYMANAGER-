package com.harjit.smartpantrymanager;

import android.database.SQLException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.recipeDetailRoot), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    view.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        findViewById(R.id.backToRecipes)
                .setOnClickListener(view -> finish());

        database = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra("recipe_id", -1);

        try {
            Recipe recipe = database.getRecipe(recipeId);

            if (recipe == null) {
                Toast.makeText(this,
                        "Recipe not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            TextView title = findViewById(R.id.recipeTitle);
            TextView ingredients = findViewById(R.id.recipeIngredients);
            TextView method = findViewById(R.id.recipeMethod);

            title.setText(recipe.getName());
            method.setText(recipe.getMethod());

            DecimalFormat format = new DecimalFormat("0.###");
            StringBuilder text = new StringBuilder();

            for (RecipeIngredient ingredient :
                    database.getRecipeIngredients(recipeId)) {

                if (text.length() > 0) {
                    text.append("\n");
                }

                text.append("• ")
                        .append(ingredient.getName())
                        .append(": ")
                        .append(format.format(ingredient.getQuantity()))
                        .append(" ")
                        .append(ingredient.getUnit());
            }

            ingredients.setText(text.toString());

        } catch (SQLException exception) {
            Toast.makeText(this,
                    "Could not load recipe", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}
