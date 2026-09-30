\# Smart Pantry Manager



A Java Android application that manages pantry ingredients and suggests

recipes only when all required ingredients are available in sufficient

quantities.



\## Features

\- Add, view, edit and delete pantry ingredients.

\- Validate ingredient names, positive quantities and whole-number counts.

\- Save pantry data between app sessions.

\- Display matching recipes from 20 database-seeded recipes.

\- View recipe ingredients, quantities and cooking instructions.

\- Match common singular/plural ingredient names.

\- Convert kg to g and litres to ml.

\- Combine duplicate pantry entries with compatible units.

\- Save a preferred default ingredient unit in Settings.



\## Screens

1\. Pantry List

2\. Add/Edit Ingredient

3\. Suggested Recipes

4\. Recipe Detail

5\. Settings



\## Technology

Java, Android XML layouts, SQLiteOpenHelper, ListView, a custom

PantryAdapter, Intents and SharedPreferences.



\## Database choice

SQLite provides persistent local storage without an internet connection,

account or separate database server. It suits this small, offline pantry

application and supports structured queries and CRUD operations.



Tables:

\- pantry: id, name, quantity, unit

\- recipes: id, name, method

\- recipe\_ingredients: id, recipe\_id, name, quantity, unit



Each recipe has multiple ingredient requirements. recipe\_id is a foreign

key referencing recipes.id. Database version 2 adds recipe tables while

preserving existing version 1 pantry data.



\## Setup

1\. Clone this repository or download and extract its ZIP.

2\. Open the project root in Android Studio.

3\. Allow Gradle to sync and install any requested SDK components.

4\. Connect an Android phone with USB debugging enabled, or use an emulator.

5\. Use Android 7.0 / API 24 or later.

6\. Select the device and run the app.



No API keys or external database configuration are required.

The database and 20 recipes are created automatically.



\## Example

Add banana: 1 pcs and milk: 250 ml.

Banana Milkshake becomes available.

Reduce milk to 200 ml and it disappears.

Change milk to 0.25 l and it becomes available again.



\## Matching rules

Every required ingredient must have enough stock.

Weight, volume, pieces and slices remain separate.

Ingredient aliases cover common names used by the seeded recipes.

Tap water for boiling is assumed available.

Rice and pasta quantities are measured dry.

Suggestions are evaluated independently; viewing a recipe does not

deduct pantry stock.



\## Manual checks completed

\- Pantry creation, editing and deletion.

\- Blank-name and zero-quantity validation.

\- Pantry persistence after closing and reopening.

\- Opening recipe details.

\- Hiding a recipe when stock is insufficient.

\- Equivalent litre/millilitre quantities.

\- Saving and restoring the default unit preference.



\## Author

Harjit Mayalall



\## Repository

https://github.com/HarjitMayalall/SMARTPANTRYMANAGER-

