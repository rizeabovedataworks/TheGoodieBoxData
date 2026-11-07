# Database-First Rebuild for The Goodie Box

Rebuild "The Goodie Box" application using a **Database-First (Data-Centric)** architecture. The local Room database acts as the Single Source of Truth (SSOT), exposing reactive `Flow` streams. Any change in storage (initial JSON seed, user favoriting, custom recipe creation, or OTA GitHub updates) instantly propagates to the UI via reactive data observation.

## Proposed Changes

### Data & Storage Layer (Single Source of Truth)

#### [MODIFY] [RecipeEntity.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/RecipeEntity.kt)
- Define clean Room `@Entity(tableName = "recipes")` with robust fields (`id`, `title`, `description`, `category`, `prepTime`, `cookTime`, `ingredientsJson`, `instructionsJson`, `handlingInstructions`, `modifications`, `imageUri`, `isFavorite`, `isCustom`).
- Provide clean `toRecipe()` and `fromRecipe()` converter functions.

#### [MODIFY] [RecipeDao.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/RecipeDao.kt)
- Expose reactive `Flow<List<RecipeEntity>>` queries:
  - `observeAllRecipes(): Flow<List<RecipeEntity>>`
  - `observeFavorites(): Flow<List<RecipeEntity>>`
  - `observeByCategory(category: String): Flow<List<RecipeEntity>>`
- Provide `suspend` methods for inserting, updating favorite status, and managing dataset versions.

#### [MODIFY] [RecipeRepository.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/RecipeRepository.kt)
- Implement `RecipeRepository` with `Flow<List<Recipe>>` exposures.
- Automatically check DB population on launch and seed from `app/src/main/assets/recipes.json` if empty.
- Implement `toggleFavorite(recipeId, isFav)` updating Room directly.
- Implement `addCustomRecipe(recipe)` writing new recipes into Room.
- Implement `syncOta()` fetching remote JSON version from GitHub and merging new recipes.

---

### UI & Presentation Layer

#### [NEW] [MainViewModel.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/MainViewModel.kt)
- ViewModel that connects `RecipeRepository` flows to the UI.
- Exposes `selectedCategory` and `recipesState: StateFlow<List<Recipe>>`.
- Automatically emits updated recipe lists whenever database records change.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/MainActivity.kt)
- Wire `MainActivity` to `MainViewModel`.
- Observe `recipesState` using lifecycle-aware coroutines to update `RecipeAdapter` automatically.
- Connect OneNote-style side category notebook tabs and navigation drawer filters.

#### [MODIFY] [AddRecipeActivity.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/AddRecipeActivity.kt)
- Collect title, category, ingredients, instructions, and photo URI.
- Save recipe to `RecipeRepository`, letting Room automatically trigger a refresh on the main screen.

#### [MODIFY] [RecipeDetailActivity.kt](file:///C:/Users/John/AndroidStudioProjects/TheGoodieBox/app/src/main/java/com/rizeabovedataworks/thegoodiebox/RecipeDetailActivity.kt)
- Display structured recipe details (prep time, cook time, bulleted ingredients, numbered steps, handling instructions, and modifications).

---

## Verification Plan

### Automated Tests
- `./gradlew assembleDebug`: Verifies Room annotation processing, `AppDatabase_Impl` generation, and Kotlin compilation.
- `./gradlew testDebugUnitTest`: Verifies unit tests pass.

### Manual Verification
1. **First Launch Seeding**: Launch app -> Verify database populates from `recipes.json`.
2. **Category Filtering**: Click OneNote side notebook tabs ("Favorites", "Cookies", "Fudge", etc.) -> Verify displayed list updates.
3. **Reactive Favorite Toggle**: Tap star on any recipe -> Verify Room DB updates and item persists in "Favorites" tab.
4. **Custom Recipe Insertion**: Tap "Add New Recipe", fill form, and save -> Verify new custom recipe appears in the database and list.
