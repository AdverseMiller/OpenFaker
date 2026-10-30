package com.android1500.androidfaker.ui.platform.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidfaker.restored.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AboutScreenContent(onBack, uriHandler::openUri)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreenContent(onBack: () -> Unit, openUri: (String) -> Unit) {
    val version = installedAppVersion()
    Scaffold(topBar = {
        TopAppBar(title = { Text("About") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("AboutList"), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(R.drawable.openfaker_icon), "OpenFaker icon", Modifier.size(96.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("OpenFaker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(version, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Your apps. Your device identity.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Per-app control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Choose which apps receive custom device identifiers and build information. Edit individual values, generate new ones, or select a device fingerprint from the catalog.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text("Changes apply when the selected app restarts. Enable OpenFaker and select the app in your Xposed manager to activate its configuration.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item { AboutSection("Project") }
            item { AboutLink("GitHub", "Source code and build instructions", Icons.Outlined.Code) { openUri("https://github.com/AdverseMiller/OpenFaker") } }
            item { AboutLink("Report an issue", "Share a bug or suggest an improvement", Icons.Outlined.BugReport) { openUri("https://github.com/AdverseMiller/OpenFaker/issues") } }
            item { AboutSection("Credits") }
            item { AboutLink("Android Faker", "Original application by Android1500", Icons.Outlined.Person) { openUri("https://github.com/Android1500/AndroidFaker") } }
            item {
                Text("Built with Kotlin, Jetpack Compose and libxposed.", Modifier.padding(24.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AboutSection(title: String) {
    Text(title, Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun AboutLink(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(subtitle) }, leadingContent = { Icon(icon, null) }, trailingContent = { Icon(Icons.Outlined.ChevronRight, null) }, modifier = Modifier.clickable(onClick = onClick))
}
