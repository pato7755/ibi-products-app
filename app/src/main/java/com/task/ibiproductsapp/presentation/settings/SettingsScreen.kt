package com.task.ibiproductsapp.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.ibiproductsapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    fun applyLanguage(languageCode: String) {
        viewModel.setLanguage(languageCode)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_sign_out)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLoggedOut)
                    }
                ) { Text(stringResource(R.string.sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Theme section
            SettingsSectionHeader("Appearance")

            SettingsToggleRow(
                icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = stringResource(R.string.dark_mode),
                subtitle = if (state.isDarkMode) {
                    stringResource(R.string.on)
                } else {
                    stringResource(R.string.off)
                },
                checked = state.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode() }
            )

            HorizontalDivider()

            // Language section
            SettingsSectionHeader(stringResource(R.string.language))

            Column(modifier = Modifier.selectableGroup()) {
                listOf("en" to "English", "he" to "Hebrew (עברית)").forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.language == code,
                                onClick = { applyLanguage(code) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.language == code,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            HorizontalDivider()

            // Security section
            SettingsSectionHeader(stringResource(R.string.security))

            if (state.isBiometricAvailable) {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    title = stringResource(R.string.biometric_login),
                    subtitle = stringResource(R.string.use_fingerprint_or_face_to_sign_in),
                    checked = state.isBiometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric() }
                )
            }

            HorizontalDivider()

            // Account section
            SettingsSectionHeader(stringResource(R.string.account))

            ListItem(
                headlineContent = { Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error) },
                leadingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = { showLogoutDialog = true })
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}