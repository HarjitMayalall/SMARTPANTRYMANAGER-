package com.harjit.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQLite stores these records on the device between app sessions.
        db.execSQL(
                "CREATE TABLE pantry (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL CHECK(quantity > 0), " +
                        "unit TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future database changes will be added here without deleting pantry data.
    }

    // Create a pantry record and return its generated ID.
    public long addItem(String name, double quantity, String unit) {
        ContentValues values = createValues(name, quantity, unit);

        return getWritableDatabase().insertOrThrow(
                "pantry", null, values
        );
    }

    // Read all ingredients alphabetically for the pantry screen.
    public ArrayList<PantryItem> getAllItems() {
        ArrayList<PantryItem> items = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                "pantry",
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        )) {
            while (cursor.moveToNext()) {
                items.add(readItem(cursor));
            }
        }

        return items;
    }

    // Find one ingredient when opening the edit screen.
    public PantryItem getItem(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                "pantry",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return readItem(cursor);
            }
        }

        return null;
    }

    // Update only the record matching the selected ID.
    public boolean updateItem(long id, String name,
                              double quantity, String unit) {
        ContentValues values = createValues(name, quantity, unit);

        int rowsUpdated = getWritableDatabase().update(
                "pantry",
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return rowsUpdated > 0;
    }

    // Delete only the selected ingredient.
    public boolean deleteItem(long id) {
        int rowsDeleted = getWritableDatabase().delete(
                "pantry",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return rowsDeleted > 0;
    }

    private ContentValues createValues(String name,
                                       double quantity,
                                       String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter an ingredient name.");
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)
                || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be a positive number."
            );
        }

        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a unit.");
        }

        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());

        return values;
    }

    // Convert a database row into a Java object.
    private PantryItem readItem(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit"))
        );
    }
}