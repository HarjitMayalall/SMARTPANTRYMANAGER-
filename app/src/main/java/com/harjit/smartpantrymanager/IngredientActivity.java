package com.harjit.smartpantrymanager;

import android.database.SQLException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IngredientActivity extends AppCompatActivity {

    private AutoCompleteTextView inputName;
    private EditText inputQuantity;
    private Spinner inputUnit;
    private DatabaseHelper database;

    // A negative ID means we are adding a new ingredient.
    private long itemId = -1;

    private final String[] units = {
            "g", "kg", "ml", "l", "pcs", "slices"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingredient);

        // Keep the form clear of system bars and the on-screen keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.ingredientRoot), (view, insets) -> {
                    Insets spacing = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.ime()
                    );
                    view.setPadding(
                            spacing.left, spacing.top,
                            spacing.right, spacing.bottom
                    );
                    return insets;
                }
        );

        database = new DatabaseHelper(this);
        inputName = findViewById(R.id.inputName);
        inputQuantity = findViewById(R.id.inputQuantity);
        inputUnit = findViewById(R.id.inputUnit);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        inputUnit.setAdapter(unitAdapter);

        String[] ingredients = {
                "egg", "bread", "milk", "cheese", "butter",
                "tomato", "onion", "potato", "rice", "pasta",
                "oil", "salt", "pepper", "flour", "sugar",
                "banana", "oats", "carrot", "peas", "garlic",
                "spinach", "yogurt", "apple", "beans"
        };

        inputName.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ingredients
        ));

        // The pantry screen passes the selected record's ID through an Intent.
        itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId == -1 && savedInstanceState == null) {
            String preferredUnit = getSharedPreferences(
                    "pantry_settings", MODE_PRIVATE)
                    .getString("default_unit", "g");

            for (int i = 0; i < units.length; i++) {
                if (units[i].equals(preferredUnit)) {
                    inputUnit.setSelection(i);
                    break;
                }
            }
        }
        if (itemId != -1) {
            PantryItem item = database.getItem(itemId);

            if (item == null) {
                Toast.makeText(
                        this, "Ingredient no longer exists.",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
                return;
            }

            TextView title = findViewById(R.id.formTitle);
            title.setText("Edit Ingredient");

            // Only load database values initially so rotation preserves edits.
            if (savedInstanceState == null) {
                inputName.setText(item.getName(), false);
                inputQuantity.setText(String.valueOf(item.getQuantity()));

                for (int i = 0; i < units.length; i++) {
                    if (units[i].equals(item.getUnit())) {
                        inputUnit.setSelection(i);
                        break;
                    }
                }
            }
        }

        findViewById(R.id.saveIngredient).setOnClickListener(
                view -> saveIngredient()
        );

        findViewById(R.id.cancelIngredient).setOnClickListener(
                view -> finish()
        );
    }

    private void saveIngredient() {
        String name = inputName.getText().toString().trim();
        String quantityText = inputQuantity.getText().toString().trim();
        String unit = inputUnit.getSelectedItem().toString();

        if (name.isEmpty()) {
            inputName.setError("Enter an ingredient name.");
            inputName.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException exception) {
            inputQuantity.setError("Enter a valid quantity.");
            inputQuantity.requestFocus();
            return;
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)
                || quantity <= 0) {
            inputQuantity.setError("Quantity must be greater than zero.");
            inputQuantity.requestFocus();
            return;
        }

        // Countable items must use whole quantities.
        if ((unit.equals("pcs") || unit.equals("slices"))
                && quantity != Math.floor(quantity)) {
            inputQuantity.setError("Use a whole number for this unit.");
            inputQuantity.requestFocus();
            return;
        }

        try {
            if (itemId == -1) {
                database.addItem(name, quantity, unit);
            } else {
                boolean updated = database.updateItem(
                        itemId, name, quantity, unit
                );

                if (!updated) {
                    Toast.makeText(
                            this, "Ingredient no longer exists.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }

            Toast.makeText(
                    this, "Ingredient saved.", Toast.LENGTH_SHORT
            ).show();

            // Return to the pantry, which will refresh its list.
            finish();

        } catch (SQLException | IllegalArgumentException exception) {
            Toast.makeText(
                    this,
                    "Could not save ingredient. Check your entry and retry.",
                    Toast.LENGTH_LONG
            ).show();
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