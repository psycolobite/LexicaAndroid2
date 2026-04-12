package com.example.lexicaandroid2.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexicaTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    useBrandTitle: Boolean = false,
    actionsContent: (@Composable RowScope.() -> Unit)? = null
) {
    val navigationIcon: @Composable () -> Unit = {
        if (canNavigateBack) {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        }
    }

    val actions: @Composable RowScope.() -> Unit = {
        actionsContent?.invoke(this)
        if (onSettingsClick != null) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Réglages"
                )
            }
        }
        if (onProfileClick != null) {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Mon profil"
                )
            }
        }
    }

    if (useBrandTitle) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = modifier.height(if (subtitle.isNullOrBlank()) 40.dp else 56.dp),
            colors = TopAppBarDefaults.topAppBarColors(),
            navigationIcon = navigationIcon,
            actions = actions
        )
    } else {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(title)
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = modifier.height(if (subtitle.isNullOrBlank()) 40.dp else 56.dp),
            colors = TopAppBarDefaults.topAppBarColors(),
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}

