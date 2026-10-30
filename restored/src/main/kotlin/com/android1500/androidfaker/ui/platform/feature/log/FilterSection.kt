package com.android1500.androidfaker.ui.platform.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.androidfaker.restored.R

@Composable
fun FilterSection(
    packages: List<String>,
    hookNames: List<String>,
    selectedPackage: String?,
    selectedHookName: String?,
    onPackageSelect: (String?) -> Unit,
    onHookNameSelect: (String?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(
        Modifier.fillMaxWidth().background(colors.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (selectedPackage != null || selectedHookName != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Filtered: ${selectedPackage ?: selectedHookName}", Modifier.weight(1f),
                    color = colors.primary, style = typography.labelMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton(onClick = onClearFilters) { Text("Clear", style = typography.labelMedium) }
            }
        }
        if (packages.isNotEmpty()) {
            Text("By Package", color = colors.onSurfaceVariant, style = typography.labelSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                packages.forEach { name ->
                    LogFilterChip(name.substringAfterLast('.'), name == selectedPackage) {
                        onPackageSelect(if (name == selectedPackage) null else name)
                    }
                }
            }
        }
        if (hookNames.isNotEmpty()) {
            Text("By Hook Type", color = colors.onSurfaceVariant, style = typography.labelSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                hookNames.forEach { name ->
                    LogFilterChip(name, name == selectedHookName) {
                        onHookNameSelect(if (name == selectedHookName) null else name)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
        leadingIcon = if (selected) {
            @Composable { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(14.dp)) }
        } else null,
    )
}
