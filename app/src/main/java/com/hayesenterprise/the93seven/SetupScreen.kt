@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val LAST_STEP = 3
private val STEP_TITLES = listOf("About you", "Activity level", "Calorie deficit", "Macro split")

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, decimal: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> if (v.all { it.isDigit() || (decimal && it == '.') }) onChange(v) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
fun SetupScreen(initial: Profile?, onSave: (Profile) -> Unit, onCancel: (() -> Unit)?) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var weight by rememberSaveable { mutableStateOf(initial?.let { fmt(it.weightLb) } ?: "") }
    var feet by rememberSaveable { mutableStateOf(initial?.let { (it.heightIn / 12).toInt().toString() } ?: "") }
    var inches by rememberSaveable { mutableStateOf(initial?.let { fmt(Math.round((it.heightIn % 12) * 10) / 10.0) } ?: "") }
    var age by rememberSaveable { mutableStateOf(initial?.age?.toString() ?: "") }
    var sex by rememberSaveable { mutableStateOf(initial?.sex ?: Sex.MALE) }
    var activity by rememberSaveable { mutableStateOf(initial?.activity ?: Activity.MODERATE) }
    var deficit by rememberSaveable { mutableStateOf(initial?.deficit ?: Deficit.STANDARD) }
    var split by rememberSaveable { mutableStateOf(initial?.split ?: Split.BALANCED) }
    var showExtreme by remember { mutableStateOf(false) }

    val weightD = weight.toDoubleOrNull()
    val heightD = feet.toIntOrNull()?.let { it * 12 + (inches.toDoubleOrNull() ?: 0.0) }
    val ageI = age.toIntOrNull()
    val allEntered = weight.isNotEmpty() && feet.isNotEmpty() && age.isNotEmpty()
    val error = validateProfile(weightD, heightD, ageI)
    val profile = if (error == null) Profile(name.trim(), weightD!!, heightD!!, ageI!!, sex, activity, deficit, split) else null
    val targets = profile?.let { Calc.targets(it) }

    BackHandler(enabled = step > 0) { step -= 1 }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        // ---- header: progress + title
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0..LAST_STEP) {
                Box(
                    Modifier.weight(1f).height(4.dp).background(
                        if (i <= step) Gold else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp)
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("STEP ${step + 1} OF ${LAST_STEP + 1}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text(STEP_TITLES[step], fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // ---- step content
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (step) {
                0 -> {
                    if (initial == null) {
                        Image(
                            painterResource(R.drawable.logo_93seven), contentDescription = "93SEVEN Lifestyle",
                            modifier = Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Fit
                        )
                        Text("Let's build your Macro Flex'n Plan. Tell us a little about you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("First name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    NumField("Weight (lb)", weight, { weight = it }, Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NumField("Height (ft)", feet, { feet = it }, Modifier.weight(1f), decimal = false)
                        NumField("(in)", inches, { inches = it }, Modifier.weight(1f))
                    }
                    NumField("Age", age, { age = it }, Modifier.fillMaxWidth(), decimal = false)
                    SectionLabel("SEX (USED IN THE CALORIE FORMULA)")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Sex.values().forEach { s -> FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s.label) }) }
                    }
                    if (allEntered && error != null) Text(error, color = Over)
                }

                1 -> {
                    Text("How active are you in a typical week?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Activity.values().forEach { a ->
                        ChoiceRow(selected = activity == a, title = a.label, detail = a.detail, onClick = { activity = a })
                    }
                }

                2 -> {
                    Text("Everyone starts at the standard 500-calorie deficit. You can go faster, but it gets harder to stick with.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Deficit.values().forEach { d ->
                        ChoiceRow(
                            selected = deficit == d, title = "${d.label}  •  ${d.kcal} cal", detail = d.detail,
                            onClick = { if (d == Deficit.EXTREME && deficit != Deficit.EXTREME) showExtreme = true else deficit = d }
                        )
                    }
                }

                else -> {
                    Text("How should your calories be split between protein, carbs and fat?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Split.values().forEach { s ->
                        ChoiceRow(selected = split == s, title = s.label, detail = null, onClick = { split = s })
                    }
                    if (targets != null) {
                        Card93 {
                            SectionLabel("YOUR DAILY TARGETS")
                            Spacer(Modifier.height(6.dp))
                            Text("${targets.kcal} calories", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Gold)
                            Text("${targets.protein} g protein  •  ${targets.carbs} g carbs  •  ${targets.fat} g fat")
                            Text("${targets.points} flex points a day", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Text("Maintenance ${targets.maintenance} cal, minus a ${targets.deficitApplied} cal deficit.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (targets.capped) {
                                Spacer(Modifier.height(6.dp))
                                Text("That deficit would drop you below ${targets.floor} calories, so we stopped there. Going lower isn't safe without a doctor's guidance.", fontSize = 12.sp, color = Over)
                            }
                        }
                    }
                    Text(
                        "93SEVEN gives general nutrition information, not medical advice. Check with your doctor before starting a new eating or exercise plan.",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ---- navigation buttons
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) {
                OutlinedButton(onClick = { step -= 1 }, modifier = Modifier.weight(1f).height(52.dp)) { Text("Back") }
            } else if (onCancel != null) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp)) { Text("Cancel") }
            }
            val isLast = step == LAST_STEP
            Button(
                onClick = { if (isLast) profile?.let(onSave) else step += 1 },
                enabled = if (step == 0) error == null else if (isLast) profile != null else true,
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text(if (isLast) (if (initial == null) "CLOCK IN" else "SAVE") else "Next", fontWeight = FontWeight.Bold) }
        }
    }

    if (showExtreme) {
        AlertDialog(
            onDismissRequest = { showExtreme = false },
            title = { Text("Extreme deficit") },
            text = {
                Text("A 1,000-calorie deficit is hard on energy, training and recovery and isn't right for everyone. If you have a medical condition, are pregnant or nursing, or aren't sure, talk with your doctor first. 93SEVEN will never take you below ${if (sex == Sex.MALE) 1500 else 1200} calories.")
            },
            confirmButton = { TextButton(onClick = { deficit = Deficit.EXTREME; showExtreme = false }) { Text("I understand") } },
            dismissButton = { TextButton(onClick = { showExtreme = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ChoiceRow(selected: Boolean, title: String, detail: String?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.padding(start = 6.dp)) {
                Text(title, fontWeight = FontWeight.Medium, color = if (selected) Gold else MaterialTheme.colorScheme.onSurface)
                if (detail != null) Text(detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}