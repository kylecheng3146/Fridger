
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fridger.com.io.utils.epochMillisToDateString
import fridger.composeapp.generated.resources.Res
import fridger.composeapp.generated.resources.add_new_item_add
import fridger.composeapp.generated.resources.add_new_item_cancel
import fridger.composeapp.generated.resources.add_new_item_close
import fridger.composeapp.generated.resources.add_new_item_confirm
import fridger.composeapp.generated.resources.add_new_item_expiry_date
import fridger.composeapp.generated.resources.add_new_item_name
import fridger.composeapp.generated.resources.add_new_item_quantity_optional
import fridger.composeapp.generated.resources.add_new_item_suggestion_apple
import fridger.composeapp.generated.resources.add_new_item_suggestion_egg
import fridger.composeapp.generated.resources.add_new_item_suggestion_milk
import fridger.composeapp.generated.resources.add_new_item_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: String, expiryDate: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val suggestions = listOf(stringResource(Res.string.add_new_item_suggestion_milk), stringResource(Res.string.add_new_item_suggestion_egg), stringResource(Res.string.add_new_item_suggestion_apple))
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.add_new_item_close))
                    }
                }

                Text(stringResource(Res.string.add_new_item_title), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.add_new_item_name)) },
                        leadingIcon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        suggestions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    name = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(Res.string.add_new_item_quantity_optional)) },
                    leadingIcon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { },
                        label = { Text(stringResource(Res.string.add_new_item_expiry_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { onConfirm(name, quantity, expiryDate) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.add_new_item_add))
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            expiryDate = epochMillisToDateString(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(Res.string.add_new_item_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.add_new_item_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
