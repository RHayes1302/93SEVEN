package com.hayesenterprise.the93seven

/** One "serving" = the household measure listed. Values are per serving (approximate, USDA-style). */
data class Food(
    val name: String,
    val cat: String,
    val measure: String,
    val grams: Int,
    val kcal: Int,
    val p: Double,
    val c: Double,
    val f: Double
) {
    val id: String get() = name
    val points: Double get() = kcal / Calc.POINT_KCAL
}

object Foods {
    val categories = listOf("Protein", "Carbs", "Fruit", "Veggies", "Fats", "Dairy")

    val all: List<Food> = listOf(
        // Protein
        Food("Chicken breast, cooked", "Protein", "4 oz", 113, 187, 35.0, 0.0, 4.0),
        Food("Lean ground turkey, cooked", "Protein", "4 oz", 113, 190, 25.0, 0.0, 10.0),
        Food("Lean ground beef 90%, cooked", "Protein", "4 oz", 113, 230, 26.0, 0.0, 13.0),
        Food("Salmon, cooked", "Protein", "4 oz", 113, 233, 25.0, 0.0, 14.0),
        Food("Tuna, canned in water", "Protein", "1 can drained", 113, 120, 26.0, 0.0, 1.0),
        Food("Shrimp, cooked", "Protein", "4 oz", 113, 112, 24.0, 0.0, 1.5),
        Food("Whole egg", "Protein", "1 large", 50, 72, 6.0, 0.4, 5.0),
        Food("Egg whites", "Protein", "1/2 cup", 123, 63, 13.0, 1.0, 0.0),
        Food("Tofu, firm", "Protein", "1/2 cup", 126, 94, 10.0, 2.0, 5.0),
        Food("Tempeh", "Protein", "3 oz", 85, 160, 15.0, 8.0, 9.0),
        Food("Whey protein powder", "Protein", "1 scoop", 30, 120, 24.0, 3.0, 1.5),
        Food("Black beans, cooked", "Protein", "1/2 cup", 86, 114, 8.0, 20.0, 0.5),
        Food("Lentils, cooked", "Protein", "1/2 cup", 99, 115, 9.0, 20.0, 0.4),
        Food("Chickpeas, cooked", "Protein", "1/2 cup", 82, 135, 7.0, 22.0, 2.0),
        // Carbs
        Food("Oats, dry", "Carbs", "1/2 cup", 40, 150, 5.0, 27.0, 3.0),
        Food("White rice, cooked", "Carbs", "3/4 cup", 117, 150, 3.0, 33.0, 0.3),
        Food("Brown rice, cooked", "Carbs", "1 cup", 195, 216, 5.0, 45.0, 1.8),
        Food("Quinoa, cooked", "Carbs", "1 cup", 185, 222, 8.0, 39.0, 4.0),
        Food("Sweet potato", "Carbs", "1 medium", 130, 112, 2.0, 26.0, 0.0),
        Food("Potato", "Carbs", "1 medium", 173, 161, 4.0, 37.0, 0.0),
        Food("Pasta, cooked", "Carbs", "1 cup", 140, 220, 8.0, 43.0, 1.0),
        Food("Whole wheat bread", "Carbs", "1 slice", 28, 80, 4.0, 14.0, 1.0),
        Food("Whole wheat tortilla", "Carbs", "1 medium", 45, 130, 4.0, 22.0, 3.0),
        Food("Rice cakes", "Carbs", "2 cakes", 18, 70, 1.5, 15.0, 0.5),
        Food("Honey", "Carbs", "1 tbsp", 21, 64, 0.0, 17.0, 0.0),
        // Fruit
        Food("Banana", "Fruit", "1 medium", 118, 105, 1.0, 27.0, 0.4),
        Food("Apple", "Fruit", "1 medium", 182, 95, 0.5, 25.0, 0.3),
        Food("Blueberries", "Fruit", "1 cup", 148, 84, 1.0, 21.0, 0.5),
        Food("Strawberries", "Fruit", "1 cup", 152, 49, 1.0, 12.0, 0.5),
        Food("Orange", "Fruit", "1 medium", 131, 62, 1.0, 15.0, 0.2),
        // Veggies
        Food("Broccoli", "Veggies", "1 cup", 91, 31, 2.5, 6.0, 0.3),
        Food("Spinach, raw", "Veggies", "2 cups", 60, 14, 2.0, 2.0, 0.2),
        Food("Mixed salad greens", "Veggies", "2 cups", 85, 15, 1.0, 3.0, 0.0),
        Food("Green beans", "Veggies", "1 cup", 125, 31, 2.0, 7.0, 0.0),
        Food("Asparagus", "Veggies", "1 cup", 134, 27, 3.0, 5.0, 0.0),
        Food("Bell pepper", "Veggies", "1 medium", 119, 31, 1.0, 7.0, 0.0),
        Food("Zucchini", "Veggies", "1 cup", 124, 21, 1.5, 4.0, 0.4),
        Food("Cauliflower rice", "Veggies", "1 cup", 107, 27, 2.0, 5.0, 0.3),
        Food("Carrots", "Veggies", "1 cup", 128, 52, 1.0, 12.0, 0.3),
        // Fats
        Food("Olive oil", "Fats", "1 tbsp", 14, 119, 0.0, 0.0, 13.5),
        Food("Avocado", "Fats", "1/2 medium", 100, 160, 2.0, 9.0, 15.0),
        Food("Almonds", "Fats", "1 oz", 28, 164, 6.0, 6.0, 14.0),
        Food("Walnuts", "Fats", "1 oz", 28, 185, 4.0, 4.0, 18.0),
        Food("Peanut butter", "Fats", "2 tbsp", 32, 190, 8.0, 7.0, 16.0),
        Food("Chia seeds", "Fats", "1 tbsp", 12, 58, 2.0, 5.0, 4.0),
        Food("Butter", "Fats", "1 tbsp", 14, 102, 0.0, 0.0, 11.5),
        Food("Hummus", "Fats", "2 tbsp", 30, 70, 2.0, 4.0, 5.0),
        Food("Dark chocolate", "Fats", "1 oz", 28, 170, 2.0, 13.0, 12.0),
        // Dairy
        Food("Greek yogurt, nonfat", "Dairy", "3/4 cup", 170, 100, 17.0, 6.0, 0.0),
        Food("Cottage cheese, low-fat", "Dairy", "1/2 cup", 113, 90, 12.0, 5.0, 2.5),
        Food("Cheddar cheese", "Dairy", "1 oz", 28, 113, 7.0, 0.4, 9.0),
        Food("Feta cheese", "Dairy", "1 oz", 28, 75, 4.0, 1.0, 6.0),
        Food("Skim milk", "Dairy", "1 cup", 245, 83, 8.0, 12.0, 0.2),
        Food("Almond milk, unsweetened", "Dairy", "1 cup", 240, 30, 1.0, 1.0, 2.5)
    )

    private val byId: Map<String, Food> = all.associateBy { it.id }
    fun find(id: String): Food? = byId[id]
}
