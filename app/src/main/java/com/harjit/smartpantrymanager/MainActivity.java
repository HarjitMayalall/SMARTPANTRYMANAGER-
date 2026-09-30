package com.harjit.smartpantrymanager;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper database;
    private PantryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    view.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        database = new DatabaseHelper(this);
        adapter = new PantryAdapter(this);

        ListView pantryList = findViewById(R.id.pantryList);
        pantryList.setAdapter(adapter);
        pantryList.setEmptyView(findViewById(R.id.emptyPantry));

        findViewById(R.id.addIngredient).setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this, IngredientActivity.class);
            startActivity(intent);
        });

        pantryList.setOnItemClickListener(
                (parent, view, position, id) ->
                        showItemOptions(adapter.getItem(position)));

        // These buttons will be connected when their screens are added.
        findViewById(R.id.showRecipes).setOnClickListener(view ->
                Toast.makeText(this,
                        "Recipe screen coming next",
                        Toast.LENGTH_SHORT).show());

        findViewById(R.id.showSettings).setOnClickListener(view ->
                Toast.makeText(this,
                        "Settings screen coming next",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && adapter != null) {
            loadPantry();
        }
    }

    private void loadPantry() {
        try {
            adapter.updateItems(database.getAllItems());
        } catch (SQLException exception) {
            Toast.makeText(this,
                    "Could not load pantry. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showItemOptions(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, option) -> {
                    if (option == 0) {
                        Intent intent = new Intent(
                                MainActivity.this, IngredientActivity.class);
                        intent.putExtra("item_id", item.getId());
                        startActivity(intent);
                    } else {
                        confirmDelete(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        boolean deleted = database.deleteItem(item.getId());

                        Toast.makeText(this,
                                deleted ? "Ingredient deleted"
                                        : "Ingredient no longer exists",
                                Toast.LENGTH_SHORT).show();

                        loadPantry();
                    } catch (SQLException exception) {
                        Toast.makeText(this,
                                "Could not delete ingredient. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}