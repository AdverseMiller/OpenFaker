package com.androidfaker.ui.platform.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.androidfaker.restored.R

/** Reconstructed from the retained Compose metadata and dialog lambdas. */
@Composable
fun FakerBasicDialog(
    title: String?,
    message: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.semantics {
            testTagsAsResourceId = true
            testTag = "AlertPopup"
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest, modifier = Modifier.testTag("AcceptAlertButton")) {
                Text(stringResource(R.string.okay))
            }
        },
        title = title?.let { value ->
            @Composable { Text(value, Modifier.testTag("AlertTitleText")) }
        },
        text = { Text(message, Modifier.testTag("AlertContentText")) },
    )
}
