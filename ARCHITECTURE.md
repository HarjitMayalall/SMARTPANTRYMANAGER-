\# Application Architecture



\## Screens and navigation

\- MainActivity displays the pantry using PantryAdapter.

\- IngredientActivity adds or edits an ingredient.

\- RecipesActivity displays recipes that match current pantry stock.

\- RecipeDetailActivity displays ingredients and cooking instructions.

\- SettingsActivity saves the preferred default ingredient unit.



Explicit Intents open screens. The item\_id extra identifies an ingredient

being edited. The recipe\_id extra identifies the selected recipe.



\## Data storage

DatabaseHelper extends SQLiteOpenHelper and manages three tables:

\- pantry: stored ingredient names, quantities and units.

\- recipes: recipe names and methods.

\- recipe\_ingredients: requirements linked to recipes by recipe\_id.



One recipe has many ingredient requirements.

Database version 2 adds recipe tables without deleting existing pantry data.

SharedPreferences stores the default unit separately.



\## Recipe matching

RecipeMatcher normalizes supported ingredient aliases and converts kg to g

and l to ml. It combines duplicate entries with compatible units.



A recipe appears only when every requirement has sufficient stock.

Pieces, slices, weight and volume are not interchangeable.

Each recipe is evaluated independently.



\## Refreshing data

MainActivity reloads pantry data in onResume.

RecipesActivity recalculates suggestions in onResume.

This refreshes the screens when the user returns after changes.



\## Validation

Ingredient names cannot be blank.

Quantities must be finite and greater than zero.

Pieces and slices require whole-number quantities.

Deletion requires confirmation.

