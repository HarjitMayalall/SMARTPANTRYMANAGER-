package com.harjit.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private final String[] units = {"g", "kg", "ml", "l", "pcs", "slices"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.settingsRoot), (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    view.setPadding(
                            bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        Spinner defaultUnit = findViewById(R.id.defaultUnit);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        defaultUnit.setAdapter(adapter);

        SharedPreferences preferences =
                getSharedPreferences("pantry_settings", MODE_PRIVATE);

        String savedUnit = preferences.getString("default_unit", "g");

        if (savedInstanceState == null) {
            for (int i = 0; i < units.length; i++) {
                if (units[i].equals(savedUnit)) {
                    defaultUnit.setSelection(i);
                    break;
                }
            }
        }

        findViewById(R.id.saveSettings).setOnClickListener(view -> {
            String selectedUnit = defaultUnit.getSelectedItem().toString();

            preferences.edit()
                    .putString("default_unit", selectedUnit)
                    .apply();

            Toast.makeText(
                    this, "Settings saved", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.settingsBack)
                .setOnClickListener(view -> finish());
    }
}