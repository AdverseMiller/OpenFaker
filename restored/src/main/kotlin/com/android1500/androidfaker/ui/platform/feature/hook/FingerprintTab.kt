package com.android1500.androidfaker.ui.platform.feature.hook

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidfaker.ui.platform.component.switch.FakerSwitch

@Composable
internal fun FingerprintTab(state: HookState, onToggle: (Boolean) -> Unit,
    onManufacturerSelect: (String) -> Unit, onModelSelect: (String) -> Unit, onOsSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FingerprintToggle(state.isFingerprintSpoofEnabled, onToggle)
        AnimatedVisibility(state.isFingerprintSpoofEnabled,
            enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CatalogDropdown("Brand", state.selectedManufacture, state.manufacturers,
                        onManufacturerSelect, Modifier.weight(1f))
                    CatalogDropdown("Model", state.selectedModel?.model ?: "", state.models.map { it.model },
                        onModelSelect, Modifier.weight(1f), enabled = state.models.isNotEmpty())
                }
                CatalogDropdown("Android Version", state.selectedOsVersion, state.osVersions, onOsSelect,
                    Modifier.fillMaxWidth(), enabled = state.osVersions.isNotEmpty())
                state.selectedModel?.let { model ->
                    FingerprintSection("Device", Icons.Outlined.PhoneAndroid, initiallyExpanded = true) {
                        DetailRow("Brand", model.brand)
                        DetailRow("Model", model.model)
                        DetailRow("Manufacturer", model.manufacture)
                        DetailRow("Device", model.device)
                        DetailRow("Product", model.product)
                    }
                    FingerprintSection("System", Icons.Outlined.Smartphone) {
                        DetailRow("Android", state.selectedOsVersion.ifEmpty { model.releaseVersion })
                        DetailRow("SDK", model.sdk)
                        DetailRow("Build ID", model.buildId)
                    }
                    FingerprintSection("Hardware", Icons.Outlined.Memory) {
                        DetailRow("Hardware", model.hardware)
                        DetailRow("Bootloader", model.bootloader)
                        DetailRow("Radio", model.radio)
                    }
                    FingerprintSection("Fingerprint", Icons.Outlined.Fingerprint) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                            Text(model.fingerprint, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace, lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FingerprintToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(if (enabled) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainer,
        tween(300), label = "cardBg")
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Row(Modifier.fillMaxWidth().clickable { onToggle(!enabled) }.testTag("FingerprintToggle").padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = if (enabled) colors.primary else colors.outlineVariant) {
                Icon(Icons.Outlined.Fingerprint, null, Modifier.padding(10.dp).size(24.dp),
                    tint = if (enabled) colors.onPrimary else colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Device Fingerprint", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (enabled) "Custom fingerprint active" else "Using real device info",
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            FakerSwitch(enabled, onToggle)
        }
    }
}

@Composable
private fun FingerprintSection(title: String, icon: ImageVector, initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, tween(200), label = "rot")
    Column(Modifier.fillMaxWidth().animateContentSize()) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { expanded = !expanded }
            .testTag("FingerprintSection:$title").padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(22.dp).rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.padding(start = 30.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(0.4f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}
