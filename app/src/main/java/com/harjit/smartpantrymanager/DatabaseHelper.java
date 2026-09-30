package com.harjit.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL)");

        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add recipe tables without deleting existing pantry items.
        if (oldVersion < 2) {
            createRecipeTables(db);
            seedRecipes(db);
        }
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "method TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id) " +
                "ON DELETE CASCADE)");
    }

    // Pantry CRUD operations.

    public long addItem(String name, double quantity, String unit) {
        return getWritableDatabase().insertOrThrow(
                "pantry", null, createValues(name, quantity, unit));
    }

    public ArrayList<PantryItem> getAllItems() {
        ArrayList<PantryItem> items = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                "pantry", null, null, null,
                null, null, "name COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                items.add(readItem(cursor));
            }
        }

        return items;
    }

    public PantryItem getItem(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                "pantry", null, "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {

            if (cursor.moveToFirst()) {
                return readItem(cursor);
            }
        }

        return null;
    }

    public boolean updateItem(
            long id, String name, double quantity, String unit) {

        return getWritableDatabase().update(
                "pantry",
                createValues(name, quantity, unit),
                "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteItem(long id) {
        return getWritableDatabase().delete(
                "pantry", "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    private ContentValues createValues(
            String name, double quantity, String unit) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter an ingredient name.");
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity)
                || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be a positive number.");
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

    private PantryItem readItem(Cursor cursor) {
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(cursor.getColumnIndexOrThrow("unit"))
        );
    }

    // Read recipes and their ingredient requirements.

    public ArrayList<Recipe> getAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                "recipes", null, null, null,
                null, null, "name COLLATE NOCASE ASC")) {

            while (cursor.moveToNext()) {
                recipes.add(readRecipe(cursor));
            }
        }

        return recipes;
    }

    public Recipe getRecipe(long id) {
        try (Cursor cursor = getReadableDatabase().query(
                "recipes", null, "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null)) {

            if (cursor.moveToFirst()) {
                return readRecipe(cursor);
            }
        }

        return null;
    }

    private Recipe readRecipe(Cursor cursor) {
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("method"))
        );
    }

    public ArrayList<RecipeIngredient> getRecipeIngredients(long recipeId) {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                "recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, "id ASC")) {

            while (cursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("unit"))
                ));
            }
        }

        return ingredients;
    }

    private RecipeIngredient ingredient(
            String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private void insertRecipe(SQLiteDatabase db, String name,
                              String method,
                              RecipeIngredient... ingredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("method", method);

        long recipeId = db.insertOrThrow("recipes", null, recipeValues);

        for (RecipeIngredient ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("name", ingredient.getName());
            values.put("quantity", ingredient.getQuantity());
            values.put("unit", ingredient.getUnit());

            db.insertOrThrow("recipe_ingredients", null, values);
        }
    }

    // Called once during database creation or the version 1 upgrade.
    private void seedRecipes(SQLiteDatabase db) {

        insertRecipe(db, "Buttered Toast",
                "1. Toast the bread until golden.\n" +
                        "2. Spread the butter over the warm toast.",
                ingredient("bread", 2, "slices"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "Scrambled Eggs",
                "1. Beat the eggs with the milk.\n" +
                        "2. Melt butter in a pan over low heat.\n" +
                        "3. Add the egg mixture and stir until fully set.",
                ingredient("egg", 2, "pcs"),
                ingredient("milk", 30, "ml"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "Cheese Omelette",
                "1. Beat the eggs and grate the cheese.\n" +
                        "2. Melt butter in a pan and add the eggs.\n" +
                        "3. Add cheese, fold and cook until the egg is fully set.",
                ingredient("egg", 2, "pcs"),
                ingredient("cheese", 40, "g"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "French Toast",
                "1. Beat the egg with the milk.\n" +
                        "2. Dip both sides of the bread in the mixture.\n" +
                        "3. Melt butter in a pan.\n" +
                        "4. Fry the bread on both sides until golden and cooked through.",
                ingredient("bread", 2, "slices"),
                ingredient("egg", 1, "pcs"),
                ingredient("milk", 60, "ml"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Place cheese between the bread slices.\n" +
                        "2. Spread butter on the outside.\n" +
                        "3. Cook in a pan on both sides until golden and melted.",
                ingredient("bread", 2, "slices"),
                ingredient("cheese", 50, "g"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "Banana Oats",
                "1. Combine oats and milk in a saucepan.\n" +
                        "2. Simmer gently, stirring, until the oats soften.\n" +
                        "3. Slice the banana and add it before serving.",
                ingredient("oats", 50, "g"),
                ingredient("milk", 200, "ml"),
                ingredient("banana", 1, "pcs"));

        insertRecipe(db, "Apple Porridge",
                "1. Core and chop the apple.\n" +
                        "2. Combine apple, oats and milk in a saucepan.\n" +
                        "3. Simmer and stir until soft.\n" +
                        "4. Stir in the sugar.",
                ingredient("oats", 50, "g"),
                ingredient("milk", 200, "ml"),
                ingredient("apple", 1, "pcs"),
                ingredient("sugar", 5, "g"));

        insertRecipe(db, "Banana Milkshake",
                "1. Peel and slice the banana.\n" +
                        "2. Blend the banana with milk until smooth.\n" +
                        "3. Serve immediately.",
                ingredient("banana", 1, "pcs"),
                ingredient("milk", 250, "ml"));

        insertRecipe(db, "Banana Yogurt Bowl",
                "1. Spoon yogurt into a bowl.\n" +
                        "2. Peel and slice the banana.\n" +
                        "3. Add the banana and oats to the yogurt.",
                ingredient("yogurt", 150, "g"),
                ingredient("banana", 1, "pcs"),
                ingredient("oats", 20, "g"));

        insertRecipe(db, "Pancakes",
                "1. Mix flour and sugar in a bowl.\n" +
                        "2. Whisk in the egg and milk until smooth.\n" +
                        "3. Heat a little oil in a pan.\n" +
                        "4. Cook small portions on both sides until cooked through.",
                ingredient("flour", 100, "g"),
                ingredient("egg", 1, "pcs"),
                ingredient("milk", 200, "ml"),
                ingredient("sugar", 10, "g"),
                ingredient("oil", 10, "ml"));

        insertRecipe(db, "Tomato Pasta",
                "1. Boil pasta in water until tender, then drain.\n" +
                        "2. Chop the tomato and onion.\n" +
                        "3. Fry onion in oil, add tomato and salt, then simmer.\n" +
                        "4. Toss the pasta in the sauce.",
                ingredient("pasta", 100, "g"),
                ingredient("tomato", 2, "pcs"),
                ingredient("onion", 1, "pcs"),
                ingredient("oil", 10, "ml"),
                ingredient("salt", 2, "g"));

        insertRecipe(db, "Cheesy Pasta",
                "1. Boil pasta in water until tender and drain.\n" +
                        "2. Warm milk and butter over low heat.\n" +
                        "3. Stir in grated cheese until melted.\n" +
                        "4. Mix the pasta into the sauce.",
                ingredient("pasta", 100, "g"),
                ingredient("cheese", 50, "g"),
                ingredient("milk", 50, "ml"),
                ingredient("butter", 10, "g"));

        insertRecipe(db, "Egg Fried Rice",
                "1. Cook the rice in water until tender and drain.\n" +
                        "2. Chop and fry the onion in oil.\n" +
                        "3. Add the egg and stir until fully set.\n" +
                        "4. Add rice and salt, then stir until hot throughout.",
                ingredient("rice", 100, "g"),
                ingredient("egg", 1, "pcs"),
                ingredient("onion", 1, "pcs"),
                ingredient("oil", 10, "ml"),
                ingredient("salt", 2, "g"));

        insertRecipe(db, "Pea Rice",
                "1. Cook rice in water until tender and drain.\n" +
                        "2. Boil peas until tender and drain.\n" +
                        "3. Mix peas, butter and salt into the hot rice.",
                ingredient("rice", 100, "g"),
                ingredient("peas", 80, "g"),
                ingredient("butter", 10, "g"),
                ingredient("salt", 2, "g"));

        insertRecipe(db, "Mashed Potatoes",
                "1. Peel and chop the potatoes.\n" +
                        "2. Boil in water until soft, then drain.\n" +
                        "3. Mash with milk, butter and salt.",
                ingredient("potato", 3, "pcs"),
                ingredient("milk", 60, "ml"),
                ingredient("butter", 15, "g"),
                ingredient("salt", 2, "g"));

        insertRecipe(db, "Potato Wedges",
                "1. Heat the oven to 200 degrees Celsius.\n" +
                        "2. Wash potatoes and cut into wedges.\n" +
                        "3. Toss with oil, salt and pepper.\n" +
                        "4. Bake for 35 to 45 minutes, turning halfway, until tender.",
                ingredient("potato", 3, "pcs"),
                ingredient("oil", 15, "ml"),
                ingredient("salt", 2, "g"),
                ingredient("pepper", 1, "g"));

        insertRecipe(db, "Carrot and Pea Stir-Fry",
                "1. Wash and thinly slice the carrots.\n" +
                        "2. Heat oil in a pan and add carrots.\n" +
                        "3. Add peas and stir-fry until vegetables are tender.\n" +
                        "4. Season with salt.",
                ingredient("carrot", 2, "pcs"),
                ingredient("peas", 100, "g"),
                ingredient("oil", 10, "ml"),
                ingredient("salt", 2, "g"));

        insertRecipe(db, "Spinach Omelette",
                "1. Wash and chop the spinach.\n" +
                        "2. Heat oil in a pan and wilt the spinach.\n" +
                        "3. Beat eggs with salt and pour into the pan.\n" +
                        "4. Cook until fully set, then fold.",
                ingredient("egg", 2, "pcs"),
                ingredient("spinach", 60, "g"),
                ingredient("oil", 10, "ml"),
                ingredient("salt", 1, "g"));

        insertRecipe(db, "Tomato Cheese Toast",
                "1. Slice the tomato and grate the cheese.\n" +
                        "2. Place tomato and cheese on the bread.\n" +
                        "3. Grill until the cheese melts and the bread is toasted.",
                ingredient("bread", 2, "slices"),
                ingredient("tomato", 1, "pcs"),
                ingredient("cheese", 40, "g"));

        insertRecipe(db, "Apple Yogurt Bowl",
                "1. Wash, core and chop the apple.\n" +
                        "2. Spoon yogurt into a bowl.\n" +
                        "3. Add the apple and oats, then stir.",
                ingredient("apple", 1, "pcs"),
                ingredient("yogurt", 150, "g"),
                ingredient("oats", 20, "g"));
    }
}