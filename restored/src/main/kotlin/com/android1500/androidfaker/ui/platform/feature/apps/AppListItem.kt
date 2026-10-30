package com.android1500.androidfaker.ui.platform.feature.apps

import android.content.pm.PackageInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.androidfaker.restored.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppListItem(
    app: AppItem,
    onClick: () -> Unit,
    onForceStopClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onEnableDataProfile: () -> Unit,
    packageInfo: PackageInfo? = app.packageInfo,
    isAppEnabled: Boolean,
    isProfileEnabled: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick,
        onLongClick = { menuExpanded = true }).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AppIcon(packageInfo, Modifier.size(48.dp))
        Spacer(Modifier.width(16.dp))
        AppContent(app.label, isAppEnabled, isProfileEnabled, Modifier.weight(1f))
        AppListItemMenuList(app.packageName, menuExpanded, isProfileEnabled = isProfileEnabled,
            onForceStopClick = onForceStopClick, onClearDataClick = onClearDataClick,
            onEnableDataProfile = onEnableDataProfile, onUninstallClick = onUninstallClick,
            onDismissRequest = { menuExpanded = false })
    }
}

@Composable
fun AppContent(label: String, isAppEnabled: Boolean, isProfileEnabled: Boolean, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f))
            if (isAppEnabled) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("Enabled", Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                }
            }
            if (isProfileEnabled) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Profile", color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** Local PackageManager loading replaces the original Coil PackageInfo fetcher. */
@Composable
fun AppIcon(info: PackageInfo?, modifier: Modifier = Modifier) {
    val pm = LocalContext.current.packageManager
    val pixels = with(LocalDensity.current) { 48.dp.roundToPx() }
    val bitmap by produceState<ImageBitmap?>(null, info, pm, pixels) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching { info?.applicationInfo?.loadIcon(pm)?.toBitmap(pixels, pixels)?.asImageBitmap() }.getOrNull()
        }
    }
    val loaded = bitmap
    if (loaded == null) Image(painterResource(R.drawable.core_designsystem_ic_android), null, modifier)
    else Image(loaded, null, modifier)
}
