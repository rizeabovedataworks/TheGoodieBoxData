# 📜 The Goodie Box — Master Application Bible & Specification
**RizeAbove DataWorks | Official Project Specification, Architecture & Developer Playbook**

---

## 📌 Executive Overview
**The Goodie Box** is a digital family heirloom recipe chest for Android. It transforms treasured family recipes into authentic handwritten 3x5 index cards with color-coded category tabs, family kitchen memories, tools & guides, full CRUD recipe management, dynamic OTA (Over-The-Air) cloud syncing, and companion web pages.

- **Application ID**: `com.rizeabovedataworks.thegoodiebox`
- **Publisher**: RizeAbove DataWorks
- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 15 (API Level 35) / Android 16 (API Level 37)
- **Primary Build Tool**: Gradle Kotlin DSL (`./gradlew :app:assembleDebug`)

---

## 🎨 Design System & Visual Aesthetics

### Color Palette & Theme Tokens
| Token Name | Hex Code | Purpose / Usage |
| :--- | :--- | :--- |
| `home_bg` | `#FFF5EA` | Primary warm cream background |
| `gingerbread_brown` | `#A35C2A` | Accent brown headers & buttons |
| `title_brown` | `#4A2E1B` | Primary title typography |
| `desc_brown` | `#7A6252` | Subtitle & body typography |
| `link_brown` | `#8C5A32` | Action links & view triggers |
| `candy_cane_red` | `#C23B22` | Crimson red star (`★`) & primary save buttons |
| `cocoa_shadow` | `#3E2723` | Dark cocoa outline stars & input labels |
| `muted_teal` | `#2A8C82` | Muted vintage accent for new categories |
| `frosting_cream` | `#FFF9F3` | Memories card & guide card background |

### Watermarks & Asset Motifs
- **`home_sparkles_bg.xml`**: Solid warm cream overlaid with `sparkles_sprinkles_bg.xml` watermark.
- **`mrs_santa_bg.xml`**: Holiday kitchen watermark featuring rolling pin, whisk, heart apron, and gingerbread stars.
- **`indexcard_bg.png`**: Authentic 3x5 landscape index card texture with red top line and blue ruled lines (5:3 aspect ratio).
- **`index_tab_image.png`**: Aged Manila index card divider tab background (`110dp x 34dp` home chips & `165dp x 36dp` side drawer tabs).
- **`recipe_box_rail_bg.xml`**: Vintage brass and wood rail bar anchoring category tabs.

---

## 🏗️ Architecture & Technical Stack

```
                     ┌─────────────────────────────────────────┐
                     │          Single Source of Truth         │
                     │    recipes.json (GitHub OTA Endpoint)   │
                     └────────────────────┬────────────────────┘
                                          │
                                     OTA Sync Flow
                                          │
                                          ▼
 ┌───────────────────┐        ┌──────────────────────┐        ┌───────────────────┐
 │   MainActivity    │ ◄────► │    MainViewModel     │ ◄────► │ RecipeRepository  │
 │ (ViewPager2 UI)   │        │ (StateFlow / Coroutines)      │   (Seeding & OTA) │
 └─────────┬─────────┘        └──────────────────────┘        └─────────┬─────────┘
           │                                                            │
    ┌──────┴──────────────┐                                             ▼
    ▼                     ▼                                   ┌───────────────────┐
Page 1: RecipeBox   Page 2: ToolsHub                          │      Room DB      │
Fragment            Fragment                                  │   (AppDatabase)   │
                                                              └───────────────────┘
```

### Architecture Highlights
- **Pattern**: MVVM (Model-View-ViewModel) with Android Jetpack Architecture Components.
- **Concurrency**: Kotlin Coroutines & `StateFlow` reactive streams.
- **Database**: Room Persistence Library Version `2` with `fallbackToDestructiveMigration(true)` in `AppDatabase.kt`.
- **Single Source of Truth (SSOT)**: `recipes.json` hosted at `https://raw.githubusercontent.com/rizeabovedataworks/TheGoodieBox/main/recipes.json`.
- **Dynamic Category Discovery**: `SELECT DISTINCT category FROM recipes` via `RecipeDao` and `RecipeRepository`. When a new category is published to `recipes.json`, the app dynamically generates Home Page chips, Side Drawer tabs, and Add Recipe dropdown items automatically.

---

## 🗄️ Database Schema & Room Entities

### 1. `RecipeEntity` (`recipes` Table)
```sql
CREATE TABLE recipes (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    prepTime TEXT NOT NULL DEFAULT '15 mins',
    cookTime TEXT NOT NULL DEFAULT '30 mins',
    ingredientsJson TEXT NOT NULL,    -- Stored as JSON string list
    instructionsJson TEXT NOT NULL,   -- Stored as JSON string list
    handlingInstructions TEXT NOT NULL DEFAULT '',
    modifications TEXT NOT NULL DEFAULT '',
    imageUri TEXT NOT NULL DEFAULT '',
    memories TEXT NOT NULL DEFAULT '',
    isFavorite INTEGER NOT NULL DEFAULT 0,
    isCustom INTEGER NOT NULL DEFAULT 0
);
```

### 2. `MetadataEntity` (`app_metadata` Table)
```sql
CREATE TABLE app_metadata (
    id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
    datasetVersion INTEGER NOT NULL DEFAULT 1
);
```

---

## 📱 User Interface & Navigation Structure

### 1. 2-Screen Horizontal Swipe Experience (`ViewPager2` + `TabLayoutMediator`)
- **Top Selector Bar (`tabLayoutPages`)**:
  - **`📜 Recipe Box`** (Page 1)
  - **`💡 Tools & Guides`** (Page 2)
- **Page 1 (`MainRecipeBoxFragment`)**:
  - Search input card (`etSearchRecipes`).
  - Horizontal dynamic category chips (`categoryChipContainer`).
  - Vintage brass & wood rail bar (`recipe_box_rail_bg.xml`).
  - RecyclerView displaying 3x5 index cards (`item_recipe.xml`).
- **Page 2 (`ToolsHubFragment`)**:
  - Dedicated Kitchen Tools & Guides Hub.
  - Large interactive cards for Turkey Guide, Tips & Hacks, Conversions, How to Use, and Add Custom Recipe.

### 2. Side Navigation Drawer (`drawer_layout_content.xml`)
- Header banner (`nav_header_onenote.xml`).
- Vertical left-aligned index tabs (`item_drawer_category_tab.xml`, `165dp x 36dp`) styled in **Casual** handwritten font (`casual` BOLD) with Crimson Red star (`★`).
- Secondary tools & legal links (*Sync OTA Recipes*, *About & Legal*).

---

## 🧰 Kitchen Tools & Guides Suite

### 0. 💌 A Note from the Creator (`CreatorLetterActivity`)
- **Greg's Family Letter**: A moving, personal memoir card from creator/developer Greg detailing his devotion as caregiver to his 91-year-old mom with dementia, memories of growing up with his parents, aluminum Christmas trees with spinning color wheels, 3x5 recipe cards, uncles, aunts, cousins, G.I. Joe toys, and holiday family feasts.

### 1. 🦃 How to Cook a Turkey Master Guide (`TurkeyGuideActivity`)
- **Safe Thawing & Defrosting**: Refrigerator thawing ratios (24 hrs per 4–5 lbs) and cold-water method. Warnings against counter thawing.
- **The 24-Hour Dry Brining Secret**: Coating 24 hours ahead with coarse kosher salt and chilling uncovered in the fridge to dry skin and season meat throughout.
- **How to Properly Spatchcock a Turkey**: Step-by-step backbone removal and flattening guide.
- **155°F Pull Temp & Carryover Cooking**: Pull at 155°F; resting 20 mins under foil gently coasts internal temp up to 165°F while locking in moisture.
- **Roasted Carrots & Garlic Gravy Base**: Placed under the turkey in the pan to combine with drippings for rich gravy.

### 2. 💡 Mom's Kitchen Tips & Hacks (`TipsActivity`)
- **Flax & Chia Seed Egg Substitute**: Mix 1 tbsp ground flaxseed/chia with 3 tbsp water (sit 5 mins) to replace 1 egg in baking.
- **Room Temperature Rule**: Eggs & butter at room temp for smooth batters.
- **Buttermilk Substitute**: 1 tbsp lemon juice/white vinegar per 1 cup whole milk.
- **Dough Chilling & Flour Sifting**: Preventing spreading and dense bakes.
- **Soft Cookie Bread Trick**: Storing soft cookies with a slice of white bread.

### 3. 📏 Measurement Conversions Calculator (`ConversionsActivity`)
- Live unit conversion calculator (Weight, Liquid Volume, Temperature).
- **🥛 Quick View Volume Conversions Cheat Sheet**: Gallons, Quarts, Pints, Cups, Tablespoons, Teaspoons, Fl Oz.
- **⚖️ Quick View Weight & Butter Conversions Cheat Sheet**: Pounds, Ounces, Grams, Kilograms, Butter Sticks.
- **🌡️ Quick Oven Temp Reference Table**: 300°F (Slow) to 425°F (High-Heat Roast).

### 4. 📖 How to Use This App (`HowToUseActivity`)
- Step-by-step user guide covering browsing, search, favorites, adding custom recipes, editing, and deleting.

### 5. ℹ️ About & Legal (`AboutActivity`)
- RizeAbove DataWorks legal notice, copyright, version information, and developer specs.

---

## 📝 Full Recipe CRUD & Safety Rules

- **Heirloom Recipe Protection**: Original family recipes (`isCustom == false`) are permanently locked and read-only. The 🗑️ Delete and ✏️ Edit buttons are hidden for all original recipes.
- **📝 Personal Kitchen Notes**: Users can tap "✏️ Edit Note" at the bottom of any original or custom recipe card to attach personal notes, family allergy warnings (e.g. *"Leave out cinnamon for Lizzy"*), or cooking tweaks. Personal notes are saved locally (`userNotes` field) and preserved across OTA syncs.
- **Custom Recipe Management**: Recipes created by the user (`isCustom == true`) retain full Edit and Delete capabilities.
- **Safe Storage Rule**: Generic default storage text was removed. Storage notes and modifications are optional and automatically hidden when blank, ensuring meat or dairy recipes never show inappropriate room-temperature storage hints.

---

## 🖼️ Asset & Image Resolution Strategy

When rendering dish photos in `RecipeDetailActivity` or `RecipeAdapter`, images are resolved in the following priority order:
1. **User Photo / Custom URI**: `content://` or `file://` local image picker URIs.
2. **Bundled Asset Drawables**: Checked via `resources.getIdentifier(imageUri, "drawable", packageName)` (e.g. `chocolatechipcookie_img`, `meatloaf_img`, `devilsnuts_img`).
3. **Category Fallback Images**: If no photo exists, category fallbacks are dynamically selected:
   - `Cookies` -> `chocolatechipcookie_img`
   - `Nuts and Snacks` -> `nutsandsnacks_fb_img`
   - `Bread` -> `breads_fb_img`
   - `Main Dishes` -> `meatloaf_img`
   - Default -> App Splash Icon (`ic_tbg_splash_main`)

---

## 📡 OTA (Over-The-Air) Sync Protocol

```
1. App Triggers Sync -> HTTP GET Request to GitHub raw recipes.json
2. Parse JSON -> Read remote "version" integer
3. Compare -> If remoteVersion > localVersion:
   - Read JSON "recipes" array
   - For each recipe: Check if ID exists in Room DB
   - Preserve existing local `isFavorite` state
   - Insert / Update recipe in Room DB
   - Save new remote version to `app_metadata` table
4. Notify User -> "Successfully updated recipes to version X!"
```

---

## 📖 Master Recipe Dataset Registry (`recipes.json` v26)

Contains **28 heirloom recipes** across **10 dynamic categories**:

| ID | Title | Category | Special Highlights |
| :--- | :--- | :--- | :--- |
| `1` | The Classic Crunchy Coated Spiced Nuts | Nuts and Snacks | Market spiced nuts |
| `2` | Spicy Sweet Party Pecans | Nuts and Snacks | Cayenne glaze |
| `3` | Sweet & Savory Rosemary Roasted Almonds | Nuts and Snacks | Fresh rosemary |
| `4` | Mom's Soft & Chewy Molasses Spice Cookies | Cookies | Crinkly ginger cookies |
| `5` | Classic Melt-in-Your-Mouth Butter Shortbread | Cookies | Rich butter shortbread |
| `6` | Cinnamon Sugar Snickerdoodles | Cookies | Cream of tartar tang |
| `7` | Classic Rich Chocolate Fudge | Fudge | Silky chocolate fudge |
| `8` | Creamy Peanut Butter Fudge | Fudge | Smooth peanut butter |
| `9` | Old-Fashioned Maple Walnut Fudge | Fudge | Real maple syrup |
| `10` | Classic Chewy Pecan Pralines | Candies | New Orleans pralines |
| `11` | Old-Fashioned Peppermint Bark | Candies | Layered dark/white chocolate |
| `12` | Easy Salted Caramel Turtles | Candies | Pecans & caramel |
| `13` | Mom's Chewy Chocolate Chip Pecan Bars | Bars | Brown sugar Blondie base |
| `14` | Classic Layered Seven-Layer Bars | Bars | Coconut & chocolate layers |
| `15` | Decadent Fudge Brownies | Bars | Fudgy crinkle top |
| `16` | Festive Cranberry Orange Quick Bread | Bread | Fresh orange zest |
| `17` | Cinnamon Swirl Pumpkin Bread | Bread | Warm autumn spice |
| `18` | Mom's Best Classic Chocolate Chip Cookies | Cookies | ❤️ Memories attached |
| `19` | Old-Fashioned Gingerbread Cake | Bread | Warm molasses cake |
| `20` | Classic Fluffy Vanilla Buttercream | Frostings | Whipped buttercream |
| `21` | Rich Cream Cheese Frosting | Frostings | Tangy cream cheese |
| `22` | Mom's Savory Glazed Meatloaf | Main Dishes | Tangy tomato glaze |
| `23` | Mom's Best Classic Peanut Brittle | Candies | ❤️ Memories attached |
| `24` | Classic Original Chex Mix | Nuts and Snacks | Seasoned butter glaze |
| `25` | Mom's Creamy Baked Macaroni & Cheese | Side Dishes | Ritz cracker crust |
| `26` | Mom's Creamy Homemade Hot Chocolate | Hot Chocolate | Stovetop cocoa |
| `27` | Traditional Herb Butter Roast Turkey | Main Dishes | 155°F pull temp & dry brine |
| `28` | Crispy Spatchcock Roast Turkey | Main Dishes | Butter under skin, olive oil outside, carrots & garlic gravy drippings |

---

## 🛠️ Developer & Publisher Playbook

### Publishing New OTA Recipes & Categories
1. Edit `app/src/main/assets/recipes.json` or remote `recipes.json` on GitHub.
2. Increment `"version"` integer (e.g. `26` -> `27`).
3. Add new recipe object to `"recipes"` list with new `"category"` string.
4. Commit and push changes to GitHub `main` branch.
5. In the Android app, open side drawer (☰) and tap **`🔄 Sync OTA Recipes`**.

### Standard Build Commands
- **Assemble Debug APK**: `./gradlew :app:assembleDebug`
- **Clean Project**: `./gradlew :app:clean`
- **Run Unit Tests**: `./gradlew :app:testDebugUnitTest`

---

## 🌐 Companion Website Integration
The app syncs with companion web pages in the `RizeAbove Dataworks Website/` repository directory:
- `moms_kitchen.html`
- `goodiebox.html`
- `thegoodiebox.html`

All web pages reflect the studio design system (`companion.html`) and are pushed live to `rizeabovedataworks.github.io`.

---

**© 2026 RizeAbove DataWorks. All rights reserved.**
