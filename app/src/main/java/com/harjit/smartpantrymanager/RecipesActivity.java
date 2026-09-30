package com.harjit.smartpantrymanager;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class RecipesActivity extends AppCompatActivity {

    private DatabaseHelper database;
    private ArrayAdapter<String> adapter;
    private final ArrayList<Recipe> matchingRecipes = new ArrayList<>();
    private TextView recipeCount;
    private TextView emptyRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipes);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.recipesRoot), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    view.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        database = new DatabaseHelper(this);
        recipeCount = findViewById(R.id.recipeCount);
        emptyRecipes = findViewById(R.id.emptyRecipes);

        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                new ArrayList<>()) {

            @Override
            public View getView(
                    int position, View convertView, ViewGroup parent) {

                View row = super.getView(position, convertView, parent);
                TextView title = row.findViewById(android.R.id.text1);
                title.setTextColor(0xFF234B35);
                title.setTextSize(18);
                return row;
            }
        };

        ListView recipeList = findViewById(R.id.recipeList);
        recipeList.setAdapter(adapter);
        recipeList.setEmptyView(emptyRecipes);

        recipeList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Recipe recipe = matchingRecipes.get(position);

                    Intent intent = new Intent(
                            RecipesActivity.this,
                            RecipeDetailActivity.class);

                    intent.putExtra("recipe_id", recipe.getId());
                    startActivity(intent);
                });

        findViewById(R.id.backToPantry)
                .setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (database != null) {
            loadMatchingRecipes();
        }
    }

    private void loadMatchingRecipes() {
        matchingRecipes.clear();
        adapter.clear();

        try {
            ArrayList<PantryItem> pantry = database.getAllItems();
            ArrayList<Recipe> allRecipes = database.getAllRecipes();

            for (Recipe recipe : allRecipes) {
                if (RecipeMatcher.canMake(
                        pantry,
                        database.getRecipeIngredients(recipe.getId()))) {

                    matchingRecipes.add(recipe);
                    adapter.add(recipe.getName());
                }
            }

            recipeCount.setText(
                    matchingRecipes.size() + " of "
                            + allRecipes.size() + " recipes available");

            emptyRecipes.setText(
                    "No recipes match your pantry yet.\n"
                            + "Add the required ingredients and quantities, "
                            + "then check again.");

        } catch (SQLException exception) {
            matchingRecipes.clear();
            adapter.clear();

            recipeCount.setText("Recipes unavailable");

            emptyRecipes.setText(
                    "Could not load recipes. "
                            + "Return to the pantry and try again.");

            Toast.makeText(this,
                    "Could not load recipes",
                    Toast.LENGTH_LONG).show();
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