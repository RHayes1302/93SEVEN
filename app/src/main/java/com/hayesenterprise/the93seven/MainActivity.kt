@file:OptIn(ExperimentalMaterial3Api::class)

package com.hayesenterprise.the93seven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(applicationContext)
        setContent {
            NinetySevenTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val user = AuthManager.user
                    if (user == null) {
                        LoginScreen()
                    } else {
                        val state = remember(user.id) { AppState(applicationContext, user.id) }
                        Root(state, onSignOut = { AuthManager.signOut() })
                    }
                }
            }
        }
    }
}

private val TABS = listOf("Home", "Meals", "Reset", "Progress", "Profile")

@Composable
fun Root(state: AppState, onSignOut: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by rememberSaveable { mutableStateOf(false) }

    if (state.profile == null) {
        SetupScreen(initial = null, onSave = { state.saveProfile(it) }, onCancel = null)
        return
    }
    if (editing) {
        SetupScreen(initial = state.profile, onSave = { state.saveProfile(it); editing = false }, onCancel = { editing = false })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                TABS.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(tabGlyph(i)) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = Gold,
                            selectedIconColor = Gold,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> HomeScreen(state, goTab = { tab = it })
                1 -> BuilderScreen(state)
                2 -> ResetScreen(state)
                3 -> ProgressScreen(state)
                else -> ProfileScreen(state, onEdit = { editing = true }, onSignOut = onSignOut)
            }
        }
    }
}

private fun tabGlyph(i: Int) = when (i) {
    0 -> "93"
    1 -> "M"
    2 -> "7"
    3 -> "↗"
    else -> "●"
}