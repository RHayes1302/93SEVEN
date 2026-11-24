@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun Card93(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
fun MacroBar(label: String, current: Double, target: Int, unit: String) {
    val frac = if (target > 0) (current / target).toFloat() else 0f
    val over = frac > 1.05f
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp)
            Text(
                "${current.roundToInt()} / $target $unit", fontSize = 13.sp,
                color = if (over) Over else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { frac.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (over) Over else Gold,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

data class Totals(val kcal: Double, val p: Double, val c: Double, val f: Double, val points: Double)

fun totalsOf(items: List<PlanItem>): Totals {
    var k = 0.0; var p = 0.0; var c = 0.0; var f = 0.0; var pts = 0.0
    for (it in items) {
        val food = Foods.find(it.foodId) ?: continue
        k += food.kcal * it.servings; p += food.p * it.servings
        c += food.c * it.servings; f += food.f * it.servings; pts += food.points * it.servings
    }
    return Totals(k, p, c, f, pts)
}

fun fmt(d: Double): String =
    if (d % 1.0 == 0.0) d.toInt().toString() else String.format(Locale.US, "%.1f", d)

fun servingsText(s: Double): String = when (s) {
    0.25 -> "1/4"; 0.5 -> "1/2"; 0.75 -> "3/4"; else -> fmt(s)
}

fun heightText(totalIn: Double): String {
    val ft = (totalIn / 12).toInt()
    val inch = totalIn - ft * 12
    return "$ft' ${fmt(Math.round(inch * 10) / 10.0)}\""
}
