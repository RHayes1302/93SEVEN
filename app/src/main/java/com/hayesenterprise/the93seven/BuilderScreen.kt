@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BuilderScreen(state: AppState) {
    val profile = state.profile ?: return
    val t = Calc.targets(profile)
    val day = totalsOf(state.plan.toList())
    var addSlot by remember { mutableStateOf<Int?>(null) }
    var swapItem by remember { mutableStateOf<PlanItem?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.logo_macro), contentDescription = "Macro", modifier = Modifier.size(84.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Macro Flex'n Plan", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Pick the foods you like. Swap anything with equal points.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card93 {
            SectionLabel("YOUR DAY VS. YOUR TARGETS")
            Spacer(Modifier.height(6.dp))
            MacroBar("Calories", day.kcal, t.kcal, "cal")
            MacroBar("Protein", day.p, t.protein, "g")
            MacroBar("Carbs", day.c, t.carbs, "g")
            MacroBar("Fat", day.f, t.fat, "g")
            MacroBar("Flex points", day.points, t.points, "pts")
        }

        SLOTS.forEachIndexed { slot, title ->
            val items = state.itemsIn(slot)
            val tot = totalsOf(items)
            Card93 {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        if (items.isNotEmpty()) Text(
                            "${tot.kcal.roundToInt()} cal  •  P ${tot.p.roundToInt()}  C ${tot.c.roundToInt()}  F ${tot.f.roundToInt()}  •  ${fmt(Math.round(tot.points * 10) / 10.0)} pts",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = { addSlot = slot }) { Text("+ Add food") }
                }
                items.forEach { item ->
                    val food = Foods.find(item.foodId) ?: return@forEach
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(food.name, fontWeight = FontWeight.Medium)
                    Text(
                        "${servingsText(item.servings)} x ${food.measure}  =  ${(food.grams * item.servings).roundToInt()} g  •  ${(food.kcal * item.servings).roundToInt()} cal  •  ${fmt(Math.round(food.points * item.servings * 10) / 10.0)} pts",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { state.changeServings(item, -0.25) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) { Text("−") }
                        Text(servingsText(item.servings), Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { state.changeServings(item, 0.25) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) { Text("+") }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { swapItem = item }) { Text("Swap") }
                        TextButton(onClick = { state.removeItem(item) }) { Text("Remove", color = Over) }
                    }
                }
                if (items.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Nothing added yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(
            "Weigh on a food scale or use the measuring cups listed. Values are per serving and approximate.",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
    }

    addSlot?.let { slot ->
        AddFoodDialog(onPick = { state.addFood(slot, it) }, onDismiss = { addSlot = null })
    }
    swapItem?.let { item ->
        SwapDialog(item = item, onPick = { state.swap(item, it) }, onDismiss = { swapItem = null })
    }
}

@Composable
private fun AddFoodDialog(onPick: (Food) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<String?>(null) }
    val list = Foods.all.filter { (cat == null || it.cat == cat) && it.name.contains(query, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a food") },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true, label = { Text("Search") }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = cat == null, onClick = { cat = null }, label = { Text("All") })
                    Foods.categories.forEach { c -> FilterChip(selected = cat == c, onClick = { cat = if (cat == c) null else c }, label = { Text(c) }) }
                }
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(list) { food ->
                        Column(Modifier.fillMaxWidth().clickable { onPick(food); onDismiss() }.padding(vertical = 8.dp)) {
                            Text(food.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${food.measure} • ${food.kcal} cal • P ${fmt(food.p)} C ${fmt(food.c)} F ${fmt(food.f)} • ${fmt(Math.round(food.points * 10) / 10.0)} pts",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun SwapDialog(item: PlanItem, onPick: (Food) -> Unit, onDismiss: () -> Unit) {
    val old = Foods.find(item.foodId)
    if (old == null) { onDismiss(); return }
    val matchPoints = old.points * item.servings
    val options = Foods.all.filter { it.cat == old.cat && it.id != old.id }.sortedBy { abs(it.points - old.points) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Flex swap") },
        text = {
            Column {
                Text(
                    "Swap ${old.name} (${fmt(Math.round(matchPoints * 10) / 10.0)} pts) for another ${old.cat.lowercase()} choice. We size the new serving to match your points.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(options) { food ->
                        val servings = (Math.round(matchPoints / food.points.coerceAtLeast(0.1) * 4.0) / 4.0).coerceAtLeast(0.25)
                        Column(Modifier.fillMaxWidth().clickable { onPick(food); onDismiss() }.padding(vertical = 8.dp)) {
                            Text(food.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${servingsText(servings)} x ${food.measure} = ${(food.grams * servings).roundToInt()} g • ${(food.kcal * servings).roundToInt()} cal",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}