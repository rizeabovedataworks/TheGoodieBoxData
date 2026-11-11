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

## 🏗️ Architecture & Repositories Structure

```
                     ┌─────────────────────────────────────────┐
                     │          Single Source of Truth         │
                     │  TheGoodieBoxData (GitHub Public Repo)  │
                     │  recipes.json (v29 Live Endpoint)       │
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

### GitHub Repository Architecture
- **Private Source Code Backup Repository (`TheGoodieBoxApp`)**:
  - `https://github.com/rizeabovedataworks/TheGoodieBoxApp`
  - Houses all Android Studio Kotlin source code, UI layouts, assets, and project files.
- **Public Cloud Data Repository (`TheGoodieBoxData`)**:
  - `https://github.com/rizeabovedataworks/TheGoodieBoxData`
  - Houses public `recipes.json` dataset for worldwide OTA cloud sync.
  - Raw OTA Endpoint: `https://raw.githubusercontent.com/rizeabovedataworks/TheGoodieBoxData/main/recipes.json`

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
    userNotes TEXT NOT NULL DEFAULT '',
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
  - Large interactive cards for Creator Letter, Turkey Guide, Tips & Hacks, Conversions, How to Use, and Add Custom Recipe.

### 2. Side Navigation Drawer (`drawer_layout_content.xml`)
- Header banner (`nav_header_onenote.xml`).
- Vertical left-aligned index tabs (`item_drawer_category_tab.xml`, `165dp x 36dp`) styled in **Casual** handwritten font (`casual` BOLD) with Crimson Red star (`★`).
- Secondary tools & legal links (*Sync OTA Recipes*, *About & Legal*).

---

## 🧰 Kitchen Tools & Guides Suite

### 0. 💌 A Note from the Creator (`CreatorLetterActivity`)
- **Greg's Family Memoir**: A moving, personal story card from creator/developer Greg detailing his devotion as caregiver to his 91-year-old mom with dementia, memories of growing up with his parents, aluminum Christmas trees with spinning color wheels, 3x5 recipe cards, uncles, aunts, cousins, G.I. Joe toys, and holiday family feasts.

### 1. 🦃 How to Cook a Turkey Master Guide (`TurkeyGuideActivity`)
- **Safe Thawing & Defrosting**: Refrigerator thawing ratios (24 hrs per 4–5 lbs) and cold-water method.
- **The 24-Hour Dry Brining Secret**: Coating 24 hours ahead with coarse kosher salt and chilling uncovered in the fridge to dry skin and season meat throughout.
- **How to Properly Spatchcock a Turkey**: Step-by-step backbone removal and flattening guide.
- **155°F Pull Temp & Carryover Cooking**: Pull at 155°F; resting 20 mins under foil gently coasts internal temp up to 165°F while locking in moisture.
- **Roasted Carrots & Garlic Gravy Base**: Placed under the turkey in the pan to combine with drippings for rich gravy.

### 1B. 🍖 How to Cook a Holiday Ham Guide (`HamGuideActivity`)
- **Ham Types & Buying Guide**: Bone-In Spiral/City Ham vs. Boneless/Formed Ham vs. Fresh Ham.
- **Low & Slow Reheating Strategy**: Reheating covered with foil at 325°F with 1/2 cup liquid (cider or white wine) to 140°F internal temp.
- **🍷 To Wine or Not to Wine (Spirits & Glazes)**: Bourbon, Dark Rum, Brandy glazes, White Wine pan liquids, red wine reductions, and non-alcoholic fruit juice substitutes.
- **Glaze Caramelization Secret**: Applying glaze in the last 25 minutes uncovered at 375°F/400°F so sugars bubble without burning.
- **Baking Time Charts & Ham Bone Soup**: Time/temp reference tables and saving the ham bone for split pea or potato soup.

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
- Step-by-step user guide covering browsing, search, favorites, adding custom recipes, editing, deleting, heirloom recipe protection, and personal kitchen notes.

### 5. ℹ️ About & Legal (`AboutActivity`)
- RizeAbove DataWorks legal notice, copyright, version information, developer specs, and dynamic dataset version indicator (`App Version 1.0 • Dataset v29`).

---

## 📝 Full Recipe CRUD, Heirloom Protection & Notes Rules

- **Heirloom Recipe Protection**: Original family recipes (`isCustom == false`) are permanently locked and read-only. The 🗑️ Delete and ✏️ Edit buttons are hidden for all original recipes.
- **📝 Personal Kitchen Notes**: Users can tap "✏️ Edit Note" at the bottom of any original or custom recipe card to attach personal notes, family allergy warnings (e.g. *"Leave out cinnamon for Lizzy"*), or cooking tweaks. Personal notes are saved locally (`userNotes` field) and preserved across OTA syncs.
- **Custom Recipe Management**: Recipes created by the user (`isCustom == true`) retain full Edit and Delete capabilities via `AddRecipeActivity`.
- **Keyboard Handling & Padding**: `AddRecipeActivity` layout features `280dp` bottom padding and `android:windowSoftInputMode="adjustResize"` to ensure soft keyboards never obscure inputs while typing.

---

## 🖼️ Asset & Image Resolution Strategy

When rendering dish photos in `RecipeDetailActivity` or `RecipeAdapter`, images are resolved in the following priority order:
1. **User Photo / Custom URI**: `content://` or `file://` local image picker URIs.
2. **Bundled Asset Drawables**: Checked via `resources.getIdentifier(imageUri, "drawable", packageName)` (e.g. `chocolatechipcookie_img`, `meatloaf_img`, `devilsnuts_img`).
3. **Category Fallback Images**: If no photo exists, category fallbacks are dynamically selected:
   - `Cookies` -> `chocolatechipcookie_img`
   - `Candies` -> `candies_fb_img`
   - `Fudge` -> `fudges_fb_img`
   - `Bars` -> `bars_fb_img`
   - `Nuts and Snacks` -> `nutsandsnacks_fb_img`
   - `Bread` -> `breads_fb_img`
   - `Frostings` -> `frostings_fb_img`
   - `Main Dishes` -> `meatloaf_img`
   - Default -> App Splash Icon (`ic_tbg_splash_main`)

---

## 📖 Master Recipe Dataset Registry (`recipes.json` v29)

Contains **31 heirloom recipes** across **10 dynamic categories**:

| ID | Title | Category | Special Highlights |
| :--- | :--- | :--- | :--- |
| `1` | The Classic Crunchy Coated Spiced Nuts | Nuts and Snacks | Market spiced nuts |
| `2` | Spicy Sweet Party Pecans | Nuts and Snacks | Cayenne glaze |
| `3` | Sweet & Savory Rosemary Roasted Almonds | Nuts and Snacks | Fresh rosemary |
| `4` | Mom's Soft & Chewy Molasses Spice Cookies | Cookies | Crinkly ginger cookies |
| `5` | Classic Melt-in-Your-Mouth Butter Shortbread | Cookies | Rich butter shortbread |
| `6` | Cinnamon Sugar Snickerdoodles | Cookies | Cream of tartar tang |
| `7` | Classic Rich Chocolate Fudge | Fudge | Silky chocolate fudge |
| `8` | Mom's Pink Peppermint Bark | Candies | ❤️ Memories attached |
| `9` | Mom's Salted Caramels | Candies | Wax paper wrapped |
| `10` | Classic Chewy Pecan Pralines | Candies | New Orleans pralines |
| `11` | Old-Fashioned Peanut Butter Fudge | Fudge | Creamy peanut butter |
| `12` | Banana Nut Bread | Bread | Ripe banana loaf |
| `13` | Pumpkin Bread | Bread | Autumn pumpkin loaf |
| `14` | Classic Brownies | Bars | Chewy chocolate bars |
| `15` | Lemon Bars | Bars | Tart lemon shortbread |
| `16` | Festive Cranberry Orange Bread | Bread | Fresh orange zest |
| `17` | Soft Sugar Cookies | Cookies | Holiday decorating cookies |
| `18` | Mom's Best Classic Chocolate Chip Cookies | Cookies | ❤️ Memories attached |
| `19` | Vanilla Cupcakes | Bread | Fluffy vanilla cupcakes |
| `20` | Classic Fluffy Vanilla Buttercream | Frostings | Whipped buttercream |
| `21` | Rich Cream Cheese Frosting | Frostings | Tangy cream cheese |
| `22` | Mom's Savory Glazed Meatloaf | Main Dishes | Tangy tomato glaze |
| `23` | Mom's Best Classic Peanut Brittle | Candies | ❤️ Memories attached |
| `24` | Classic Original Chex Mix | Nuts and Snacks | Seasoned butter glaze |
| `25` | Mom's Creamy Baked Macaroni & Cheese | Side Dishes | Ritz cracker crust |
| `26` | Mom's Creamy Homemade Hot Chocolate | Hot Chocolate | Stovetop cocoa |
| `27` | Traditional Herb Butter Roast Turkey | Main Dishes | 155°F pull temp & dry brine |
| `28` | Crispy Spatchcock Roast Turkey | Main Dishes | Butter under skin, olive oil outside, carrots & garlic gravy drippings |
| `29` | Southern Candied Yams | Side Dishes | Brown sugar & maple glaze |
| `30` | Classic Green Bean Casserole | Side Dishes | Creamy mushroom & crunchy onions |
| `31` | Divine Mashed Potatoes | Side Dishes | Yukon Gold, butter & sour cream |
| `32` | Classic Brown Sugar Pineapple Baked Ham | Main Dishes | Clove-pinned pineapple & cherries with brown sugar glaze |
| `33` | Easy Honey Glazed Boneless Ham | Main Dishes | Cinnamon honey mustard glaze for effortless slicing |
| `34` | Mom's Classic Baked Scalloped Corn | Side Dishes | Creamed corn, whole sweet corn & Ritz cracker crust |
| `35` | Greg's Skillet Scalloped Corn | Side Dishes | Fast 15-minute stovetop skillet corn seared in butter |
| `36` | Traditional Pumpkin Pie | Pies | Classic custard pumpkin pie baked in flaky crust |
| `37` | Old-Fashioned Peanut Butter Pie | Pies | No-bake velvety peanut butter pie with hot fudge drizzle |
| `38` | King's Hawaiian Sweet Dinner Rolls | Bread | Cloud-soft sweet Hawaiian rolls with honey butter glaze |
| `39` | Classic Fluffy Dinner Rolls | Bread | Golden feather-light yeast dinner rolls |
| `40` | Mom's Chocolate Wacky Cake | Cakes | Moist Depression-era crazy cake baked with cocoa, oil & vinegar |
| `41` | Greg's Spiced Dry Hot Chocolate Mix | Hot Chocolate | Homemade dry spiced cocoa mix in gift pint Mason jars |

---

## 🛠️ Developer & Publisher Playbook

### Publishing New OTA Recipes & Categories
1. Edit `recipes.json` in `TheGoodieBoxData` repository.
2. Increment `"version"` integer (e.g. `29` -> `30`).
3. Add new recipe object to `"recipes"` list with new `"category"` string.
4. Commit and push changes to `TheGoodieBoxData` `main` branch on GitHub.
5. In the Android app, open side drawer (☰) and tap **`🔄 Sync OTA Recipes`**.

### Standard Build Commands
- **Assemble Debug APK**: `./gradlew :app:assembleDebug`
- **Clean Project**: `./gradlew :app:clean`
- **Run Unit Tests**: `./gradlew :app:testDebugUnitTest`

---

## 🌐 Companion Website Integration
The app syncs with companion web pages in the `rizeabovedataworks.github.io` repository:
- `moms_kitchen.html`
- `goodiebox.html`
- `thegoodiebox.html`

All web pages reflect the studio design system (`companion.html`) and are pushed live to `rizeabovedataworks.github.io`.

---

**© 2026 RizeAbove DataWorks. All rights reserved.**
