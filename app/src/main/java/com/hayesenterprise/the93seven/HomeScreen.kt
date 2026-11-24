@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private data class Phase(val title: String, val body: String)

private val PREP_TASKS = listOf(
    "Plan this week's meals in the Meals tab",
    "Shop for your protein, carb and fat sources",
    "Prep and portion your meals (use the scale or measuring cups)",
    "Pick your workouts for the week"
)

@Composable
fun HomeScreen(state: AppState, goTab: (Int) -> Unit) {
    val profile = state.profile ?: return
    val t = Calc.targets(profile)
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val clockOut = dow == Calendar.SATURDAY && hour >= 12

    val phase = when {
        dow == Calendar.SUNDAY -> Phase("PREP DAY", "Get back on your meals. Prep your food for the week so everything is easy.")
        clockOut -> Phase("CLOCK OUT", "Finish your first four meals, then enjoy your reset meal. You earned it.")
        dow == Calendar.MONDAY -> Phase("CLOCK IN", "Monday morning is the true clock-in. Follow your plan and do something active.")
        else -> Phase("CLOCKED IN", "Stay on your plan, move your body, and take it one meal at a time.")
    }

    val today = dateKey()
    val pct = state.commitmentPercent()
    val prep = remember { mutableStateListOf(false, false, false, false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painterResource(R.drawable.logo_93seven), contentDescription = "93SEVEN Lifestyle",
            modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Fit
        )
        Text(if (profile.name.isNotEmpty()) "Hey ${profile.name}" else "Welcome back", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Card93 {
            SectionLabel(phase.title)
            Spacer(Modifier.height(4.dp))
            Text(phase.body)
            if (clockOut) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = { goTab(2) }) { Text("Pick your reset meal") }
            }
        }

        Card93 {
            SectionLabel("YOUR 93% THIS WEEK")
            Spacer(Modifier.height(6.dp))
            if (pct == null) {
                Text("Build your plan and check off meals to start your week.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("$pct%", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = if (pct >= 93) Gold else MaterialTheme.colorScheme.onSurface)
                LinearProgressIndicator(
                    progress = { pct / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Gold, trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (pct >= 93) "You're at or above the 93. Keep clocking in." else "Goal is 93%. The other 7% is your reset meal.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card93 {
            SectionLabel("TODAY'S TARGETS")
            Spacer(Modifier.height(4.dp))
            Text("${t.kcal} calories", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gold)
            Text("${t.protein} g protein  •  ${t.carbs} g carbs  •  ${t.fat} g fat")
            Text("${t.points} flex points", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card93 {
            SectionLabel("TODAY'S MEALS")
            Spacer(Modifier.height(4.dp))
            val filled = SLOTS.indices.filter { state.itemsIn(it).isNotEmpty() }
            if (filled.isEmpty()) {
                Text("You haven't built your Macro Flex'n Plan yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { goTab(1) }) { Text("Build my plan") }
            } else {
                filled.forEach { slot ->
                    val tot = totalsOf(state.itemsIn(slot))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = state.isDone(today, slot), onCheckedChange = { state.toggleDone(today, slot) })
                        Column(Modifier.weight(1f)) {
                            Text(SLOTS[slot], fontWeight = FontWeight.Medium)
                            Text("${tot.kcal.toInt()} cal  •  ${fmt(Math.round(tot.points * 10) / 10.0)} pts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (dow == Calendar.SUNDAY) {
            Card93 {
                SectionLabel("SUNDAY PREP CHECKLIST")
                PrepList(prep)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PrepList(prep: SnapshotStateList<Boolean>) {
    PREP_TASKS.forEachIndexed { i, task ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = prep[i], onCheckedChange = { prep[i] = it })
            Text(task, fontSize = 14.sp)
        }
    }
}