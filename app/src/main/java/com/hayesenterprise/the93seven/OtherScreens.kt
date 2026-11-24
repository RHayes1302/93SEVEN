@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------ RESET (the 7%)

private val RESET_IDEAS = listOf(
    "Chipotle bowl" to "Rice, beans, protein and fajita veggies. Easy on the extras and it's a great reset.",
    "Sushi" to "Sashimi, nigiri or a couple of rolls. Clean, satisfying and still a reward.",
    "A real burger" to "If you want a burger, eat a burger. Skip the processed chains and go for a local spot, or grill one at home.",
    "Tacos" to "Corn tortillas, grilled meat or fish, salsa and onions. Fresh and full of flavor.",
    "Mediterranean plate" to "Gyro or kabob, hummus, rice and salad.",
    "Pizza night" to "Wood-fired or homemade beats frozen. Pair it with a salad.",
    "Something sweet" to "Dessert is allowed. Pick the one you actually want, and enjoy every bite."
)

@Composable
fun ResetScreen(state: AppState) {
    val cal = Calendar.getInstance()
    val clockOut = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && cal.get(Calendar.HOUR_OF_DAY) >= 12
    val logged = state.resetLoggedThisWeek()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("The 7% — Your Reset", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Card93 {
            SectionLabel("CLOCK OUT")
            Spacer(Modifier.height(4.dp))
            Text("Saturday afternoon, after your first four meals, you clock out. This is your mental reset: satisfy the craving, reward yourself, and don't reverse your achievements.")
            Spacer(Modifier.height(10.dp))
            if (logged) {
                Text("✓ Reset meal logged this week", color = Gold, fontWeight = FontWeight.Bold)
                TextButton(onClick = { state.undoReset() }) { Text("Undo") }
            } else {
                Button(onClick = { state.logReset() }) { Text(if (clockOut) "I clocked out — log my reset" else "Log my reset meal") }
                if (!clockOut) Text("Your reset time opens Saturday at noon.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionLabel("CLEANER-EATING IDEAS")
        RESET_IDEAS.forEach { (title, tip) ->
            Card93 {
                Text(title, fontWeight = FontWeight.Bold)
                Text(tip, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card93 {
            SectionLabel("RESET RULES")
            Spacer(Modifier.height(4.dp))
            Text("• One meal, not a whole day.")
            Text("• Enjoy it and stop when you're satisfied.")
            Text("• Sunday morning you're back on prep.")
        }
        Spacer(Modifier.height(12.dp))
    }
}

// ------------------------------------------------------------------ PROGRESS

@Composable
fun ProgressScreen(state: AppState) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    val entries = state.weights.toList()
    val dateFmt = SimpleDateFormat("MMM d", Locale.US)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Progress", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Card93 {
            SectionLabel("LOG TODAY")
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) weight = v },
                    label = { Text("Weight (lb)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = waist, onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) waist = v },
                    label = { Text("Waist (in)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            val w = weight.toDoubleOrNull()
            Button(
                onClick = { state.logWeight(w!!, waist.toDoubleOrNull()); weight = ""; waist = "" },
                enabled = w != null && w in 50.0..700.0
            ) { Text("Save entry") }
        }

        Card93 {
            SectionLabel("WEIGHT TREND")
            Spacer(Modifier.height(8.dp))
            if (entries.size < 2) {
                Text("Log at least two entries to see your trend.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val lo = entries.minOf { it.weightLb }
                val hi = entries.maxOf { it.weightLb }
                val range = (hi - lo).coerceAtLeast(1.0)
                Canvas(Modifier.fillMaxWidth().height(150.dp).padding(8.dp)) {
                    val path = Path()
                    entries.forEachIndexed { i, e ->
                        val x = size.width * i / (entries.size - 1)
                        val y = size.height - (size.height * ((e.weightLb - lo) / range)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(Gold, radius = 7f, center = Offset(x, y))
                    }
                    drawPath(path, Gold, style = Stroke(width = 5f))
                }
                val change = entries.last().weightLb - entries.first().weightLb
                Text(
                    "${fmt(entries.first().weightLb)} → ${fmt(entries.last().weightLb)} lb  (${if (change > 0) "+" else ""}${fmt(Math.round(change * 10) / 10.0)})",
                    fontWeight = FontWeight.Bold, color = Gold
                )
            }
        }

        SectionLabel("HISTORY")
        if (entries.isEmpty()) Text("No entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        entries.reversed().forEach { e ->
            Card93 {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${fmt(e.weightLb)} lb", fontWeight = FontWeight.Bold)
                        Text(
                            dateFmt.format(Date(e.time)) + (e.waistIn?.let { "  •  waist ${fmt(it)} in" } ?: ""),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { state.deleteWeight(e) }) { Text("Delete", color = Over) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

// ------------------------------------------------------------------ PROFILE

@Composable
fun ProfileScreen(state: AppState, onEdit: () -> Unit, onSignOut: () -> Unit) {
    val p = state.profile ?: return
    val t = Calc.targets(p)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(painterResource(R.drawable.logo_93seven), contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Fit)
        Text(if (p.name.isNotEmpty()) p.name else "Your profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Card93 {
            SectionLabel("YOU")
            Spacer(Modifier.height(4.dp))
            Text("${fmt(p.weightLb)} lb  •  ${heightText(p.heightIn)}  •  ${p.age} yrs  •  ${p.sex.label}")
            Text("${p.activity.label}  •  ${p.deficit.label} deficit (${p.deficit.kcal} cal)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onEdit) { Text("Edit profile") }
        }

        Card93 {
            SectionLabel("ACCOUNT")
            Spacer(Modifier.height(4.dp))
            Text(AuthManager.user?.email ?: "Signed in", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onSignOut) { Text("Sign out") }
        }

        Card93 {
            SectionLabel("HOW YOUR NUMBERS ARE MADE")
            Spacer(Modifier.height(4.dp))
            Text("Resting burn (BMR): ${t.bmr} cal — Mifflin-St Jeor formula")
            Text("Maintenance: ${t.maintenance} cal — BMR × ${p.activity.mult}")
            Text("Deficit: −${t.deficitApplied} cal")
            Text("Daily target: ${t.kcal} cal", fontWeight = FontWeight.Bold, color = Gold)
            Text("${t.protein} g protein • ${t.carbs} g carbs • ${t.fat} g fat (${p.split.p}/${p.split.c}/${p.split.f})")
            Text("1 flex point = ${Calc.POINT_KCAL.toInt()} calories, so ${t.points} points a day.")
            if (t.capped) Text("Capped at the ${t.floor}-calorie safety floor.", color = Over, fontSize = 12.sp)
        }

        Text(
            "93SEVEN gives general nutrition information, not medical advice. Check with your doctor before starting a new eating or exercise plan.",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
    }
}