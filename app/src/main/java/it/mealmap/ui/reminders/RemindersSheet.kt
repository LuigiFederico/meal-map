package it.mealmap.ui.reminders

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.mealmap.R
import it.mealmap.data.ReminderMoment
import it.mealmap.data.ReminderOption
import it.mealmap.data.Reminders
import it.mealmap.ui.components.AppIcons

@get:StringRes
val ReminderMoment.label: Int
    get() = when (this) {
        ReminderMoment.BREAKFAST -> R.string.reminder_breakfast
        ReminderMoment.SNACK -> R.string.reminder_snack
        ReminderMoment.AFTERNOON_SNACK -> R.string.reminder_afternoon_snack
    }

@get:StringRes
private val ReminderMoment.note: Int?
    get() = when (this) {
        ReminderMoment.BREAKFAST -> null
        ReminderMoment.SNACK -> R.string.reminders_snack_note
        ReminderMoment.AFTERNOON_SNACK -> R.string.reminders_afternoon_snack_note
    }

/** Bottom sheet with the fixed breakfast, snack and afternoon snack options. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersSheet(initial: ReminderMoment, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        RemindersContent(initial, onClose = onDismiss)
    }
}

@Composable
fun RemindersContent(initial: ReminderMoment, onClose: () -> Unit, modifier: Modifier = Modifier) {
    var moment by rememberSaveable { mutableStateOf(initial) }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 12.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.reminders_kicker),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(moment.label),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
            }
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.close))
            }
        }
        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ReminderMoment.entries.forEach { entry ->
                    SegmentedButton(
                        selected = entry == moment,
                        onClick = { moment = entry },
                        shape = SegmentedButtonDefaults.itemShape(
                            entry.ordinal,
                            ReminderMoment.entries.size,
                        ),
                        icon = {},
                    ) { Text(stringResource(entry.label)) }
                }
            }
            moment.note?.let { InfoBanner(stringResource(it)) }
            Reminders.getValue(moment).forEach { OptionCard(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    AppIcons.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    stringResource(R.string.reminders_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Icon(
            AppIcons.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun OptionCard(option: ReminderOption) {
    val optionLabel = stringResource(R.string.reminders_option, option.number)
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clearAndSetSemantics { contentDescription = optionLabel },
        ) {
            Text(
                option.number.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            option.items.forEach { item ->
                Row {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    item.quantity?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
