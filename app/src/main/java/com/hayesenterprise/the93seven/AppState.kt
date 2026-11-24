package com.hayesenterprise.the93seven

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val SLOTS = listOf("Breakfast", "Meal 2", "Meal 3", "Meal 4")

data class PlanItem(val slot: Int, val foodId: String, val servings: Double)
data class WeightEntry(val time: Long, val weightLb: Double, val waistIn: Double?)

fun dateKey(cal: Calendar = Calendar.getInstance()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

/** The Sunday that starts the current clock-in week, as yyyy-MM-dd. */
fun weekStartKey(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -(cal.get(Calendar.DAY_OF_WEEK) - 1))
    return dateKey(cal)
}

/** One saved state per signed-in user, so two accounts on the same phone never see each other's plans. */
class AppState(private val ctx: Context, private val uid: String) {
    private fun prefs() = ctx.getSharedPreferences("the93seven_$uid", Context.MODE_PRIVATE)

    var profile by mutableStateOf<Profile?>(null)
    val plan = mutableStateListOf<PlanItem>()
    val done = mutableStateListOf<String>()          // "yyyy-MM-dd|slot"
    val weights = mutableStateListOf<WeightEntry>()
    val resetDates = mutableStateListOf<String>()    // days a reset meal was logged

    init { load() }

    // ---- profile
    fun saveProfile(p: Profile) { profile = p; save() }

    // ---- plan
    fun itemsIn(slot: Int) = plan.filter { it.slot == slot }

    fun addFood(slot: Int, food: Food) {
        val i = plan.indexOfFirst { it.slot == slot && it.foodId == food.id }
        if (i >= 0) plan[i] = plan[i].copy(servings = plan[i].servings + 1.0)
        else plan.add(PlanItem(slot, food.id, 1.0))
        save()
    }

    fun changeServings(item: PlanItem, delta: Double) {
        val i = plan.indexOf(item)
        if (i < 0) return
        val next = item.servings + delta
        if (next < 0.25) plan.removeAt(i) else plan[i] = item.copy(servings = next)
        save()
    }

    fun removeItem(item: PlanItem) { plan.remove(item); save() }

    /** Flex swap: replace a food with another and size the new serving to match points. */
    fun swap(item: PlanItem, newFood: Food) {
        val old = Foods.find(item.foodId) ?: return
        val i = plan.indexOf(item)
        if (i < 0) return
        val matchPoints = old.points * item.servings
        var servings = Math.round(matchPoints / newFood.points.coerceAtLeast(0.1) * 4.0) / 4.0
        if (servings < 0.25) servings = 0.25
        plan.removeAt(i)
        val existing = plan.indexOfFirst { it.slot == item.slot && it.foodId == newFood.id }
        if (existing >= 0) plan[existing] = plan[existing].copy(servings = plan[existing].servings + servings)
        else plan.add(i.coerceAtMost(plan.size), PlanItem(item.slot, newFood.id, servings))
        save()
    }

    // ---- daily check marks
    fun isDone(date: String, slot: Int) = done.contains("$date|$slot")
    fun toggleDone(date: String, slot: Int) {
        val k = "$date|$slot"
        if (!done.remove(k)) done.add(k)
        save()
    }

    /** 0..100, or null if there is nothing to measure yet. Today never counts against you until a meal is checked off. */
    fun commitmentPercent(): Int? {
        val slotsWithFood = SLOTS.indices.count { itemsIn(it).isNotEmpty() }
        if (slotsWithFood == 0) return null
        val cal = Calendar.getInstance()
        val daysBeforeToday = cal.get(Calendar.DAY_OF_WEEK) - 1
        val start = weekStartKey()
        val today = dateKey()
        val doneThisWeek = done.count { it.substringBefore("|") >= start }
        val doneToday = done.count { it.substringBefore("|") == today }
        val expected = slotsWithFood * daysBeforeToday + doneToday
        if (expected == 0) return null
        return (doneThisWeek * 100 / expected).coerceAtMost(100)
    }

    // ---- reset meal
    fun resetLoggedThisWeek() = resetDates.any { it >= weekStartKey() }
    fun logReset() { if (!resetDates.contains(dateKey())) resetDates.add(dateKey()); save() }
    fun undoReset() { resetDates.removeAll { it >= weekStartKey() }; save() }

    // ---- progress
    fun logWeight(weight: Double, waist: Double?) {
        weights.add(WeightEntry(System.currentTimeMillis(), weight, waist)); save()
    }
    fun deleteWeight(e: WeightEntry) { weights.remove(e); save() }

    // ---- persistence (plain JSON in SharedPreferences: no database setup needed)
    private fun save() {
        val o = JSONObject()
        profile?.let { p ->
            o.put("profile", JSONObject()
                .put("name", p.name).put("weightLb", p.weightLb).put("heightIn", p.heightIn)
                .put("age", p.age).put("sex", p.sex.name).put("activity", p.activity.name)
                .put("deficit", p.deficit.name).put("split", p.split.name))
        }
        o.put("plan", JSONArray().also { a -> plan.forEach {
            a.put(JSONObject().put("slot", it.slot).put("food", it.foodId).put("servings", it.servings)) } })
        o.put("done", JSONArray(done.toList()))
        o.put("resets", JSONArray(resetDates.toList()))
        o.put("weights", JSONArray().also { a -> weights.forEach {
            a.put(JSONObject().put("t", it.time).put("w", it.weightLb).put("waist", it.waistIn ?: -1.0)) } })
        prefs().edit().putString("state", o.toString()).apply()
    }

    private fun load() {
        val raw = prefs().getString("state", null) ?: return
        try {
            val o = JSONObject(raw)
            o.optJSONObject("profile")?.let { p ->
                profile = Profile(
                    p.getString("name"), p.getDouble("weightLb"), p.getDouble("heightIn"), p.getInt("age"),
                    Sex.valueOf(p.getString("sex")), Activity.valueOf(p.getString("activity")),
                    Deficit.valueOf(p.getString("deficit")), Split.valueOf(p.getString("split")))
            }
            o.optJSONArray("plan")?.let { a -> for (i in 0 until a.length()) {
                val x = a.getJSONObject(i)
                plan.add(PlanItem(x.getInt("slot"), x.getString("food"), x.getDouble("servings"))) } }
            o.optJSONArray("done")?.let { a -> for (i in 0 until a.length()) done.add(a.getString(i)) }
            o.optJSONArray("resets")?.let { a -> for (i in 0 until a.length()) resetDates.add(a.getString(i)) }
            o.optJSONArray("weights")?.let { a -> for (i in 0 until a.length()) {
                val x = a.getJSONObject(i)
                val waist = x.getDouble("waist")
                weights.add(WeightEntry(x.getLong("t"), x.getDouble("w"), if (waist < 0) null else waist)) } }
        } catch (e: Exception) {
            // corrupted data: start fresh rather than crash
        }
    }
}