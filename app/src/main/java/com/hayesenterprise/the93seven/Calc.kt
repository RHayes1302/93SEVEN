package com.hayesenterprise.the93seven

import kotlin.math.max
import kotlin.math.roundToInt

enum class Sex(val label: String) { MALE("Male"), FEMALE("Female") }

enum class Activity(val label: String, val detail: String, val mult: Double) {
    SEDENTARY("Sedentary", "Desk job, little exercise", 1.2),
    LIGHT("Lightly active", "Exercise 1-3 days a week", 1.375),
    MODERATE("Moderately active", "Exercise 3-5 days a week", 1.55),
    VERY("Very active", "Hard exercise 6-7 days a week", 1.725),
    ATHLETE("Athlete", "Twice-a-day training or a very physical job", 1.9)
}

/** Everyone starts on STANDARD (500 below maintenance). Extreme must be chosen on purpose. */
enum class Deficit(val label: String, val kcal: Int, val detail: String) {
    STANDARD("Standard", 500, "500 calories below maintenance. The 93SEVEN default."),
    AGGRESSIVE("Aggressive", 700, "700 calories below maintenance. Faster, but harder to stick with."),
    EXTREME("Extreme", 1000, "1,000 calories below maintenance. Hardest on energy and recovery.")
}

enum class Split(val label: String, val p: Int, val c: Int, val f: Int) {
    BALANCED("40 / 30 / 30  (protein / carbs / fat)", 40, 30, 30),
    HIGH_CARB("30 / 45 / 25  (more carbs)", 30, 45, 25),
    HIGH_PROTEIN("45 / 30 / 25  (more protein)", 45, 30, 25)
}

data class Profile(
    val name: String,
    val weightLb: Double,
    val heightIn: Double,
    val age: Int,
    val sex: Sex,
    val activity: Activity,
    val deficit: Deficit,
    val split: Split
)

data class Targets(
    val bmr: Int,
    val maintenance: Int,
    val deficitApplied: Int,
    val kcal: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val points: Int,
    val capped: Boolean,
    val floor: Int
)

object Calc {
    /** One flex point = 50 calories. Swapping foods with equal points keeps the day's calories the same. */
    const val POINT_KCAL = 50.0

    fun targets(p: Profile): Targets {
        val kg = p.weightLb * 0.45359237
        val cm = p.heightIn * 2.54
        val base = 10 * kg + 6.25 * cm - 5 * p.age
        val bmr = if (p.sex == Sex.MALE) base + 5 else base - 161      // Mifflin-St Jeor
        val maintenance = bmr * p.activity.mult
        val floor = if (p.sex == Sex.MALE) 1500 else 1200               // safety floor
        val raw = maintenance - p.deficit.kcal
        val kcal = max(raw, floor.toDouble())
        return Targets(
            bmr = bmr.roundToInt(),
            maintenance = maintenance.roundToInt(),
            deficitApplied = (maintenance - kcal).roundToInt(),
            kcal = kcal.roundToInt(),
            protein = (kcal * p.split.p / 100.0 / 4.0).roundToInt(),
            carbs = (kcal * p.split.c / 100.0 / 4.0).roundToInt(),
            fat = (kcal * p.split.f / 100.0 / 9.0).roundToInt(),
            points = (kcal / POINT_KCAL).roundToInt(),
            capped = raw < floor,
            floor = floor
        )
    }
}

fun validateProfile(weight: Double?, heightIn: Double?, age: Int?): String? {
    if (age == null || age < 18) return "93SEVEN is for adults 18 and older."
    if (weight == null || weight < 80 || weight > 600) return "Enter a weight between 80 and 600 lb."
    if (heightIn == null || heightIn < 48 || heightIn > 96) return "Enter a height between 4'0\" and 8'0\"."
    return null
}
