package com.task.ibiproductsapp.presentation.login.addeditproduct


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.ibiproductsapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditProductViewModel = hiltViewModel()
) {
    val state by viewModel.addEditState.collectAsStateWithLifecycle()
    val isEditMode = state.productId != null

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) stringResource(R.string.edit_product) else stringResource(R.string.add_product)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(
                            R.string.back
                        ))
                    }
                },
                actions = {
                    // Reset to API option only in edit mode for locally modified products
                    if (isEditMode && state.isLocallyModified) {
                        IconButton(onClick = viewModel::resetToApi) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_to_api_data))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLocallyModified) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.locally_modified_tap_refresh_to_restore_api_data), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.title.isBlank() && state.errorMessage != null,
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.price,
                    onValueChange = viewModel::onPriceChanged,
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = state.price.toDoubleOrNull() == null && state.price.isNotBlank()
                )
                OutlinedTextField(
                    value = state.stock,
                    onValueChange = viewModel::onStockChanged,
                    label = { Text(stringResource(R.string.stock)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.category,
                onValueChange = viewModel::onCategoryChanged,
                label = { Text(stringResource(R.string.category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.brand,
                onValueChange = viewModel::onBrandChanged,
                label = { Text(stringResource(R.string.brand)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.add_product))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}