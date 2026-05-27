package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ClaimViewModel
import com.example.ui.components.SwipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ClaimViewModel) {
    val pendingClaims by viewModel.pendingClaims.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        var urlInput by remember { mutableStateOf(uiState.localLlmUrl) }
        var useLocalInput by remember { mutableStateOf(uiState.useLocalLlm) }

        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("LLM Provider Settings") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = useLocalInput, onCheckedChange = { useLocalInput = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Use Local API (OpenAI Format)")
                    }
                    if (useLocalInput) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Local API Endpoint URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "e.g. http://10.0.2.2:1234/v1/chat/completions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateSettings(useLocalInput, urlInput)
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("ClaimSSOT") },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            Text(
                                text = "${uiState.totalCount - pendingClaims.size} / ${uiState.totalCount}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
                if (uiState.totalCount > 0) {
                    val progress = (uiState.totalCount - pendingClaims.size).toFloat() / uiState.totalCount
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (uiState.totalCount == 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No Claims Datastore", 
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Load the initial CSV export of claims.")
                            Spacer(Modifier.height(32.dp))
                            Button(onClick = { viewModel.loadMockData() }) {
                                Text("Import Database (Mock 11k)")
                            }
                        }
                    } else if (pendingClaims.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("All Claims Processed!", style = MaterialTheme.typography.headlineMedium)
                            Text("The SSOT is up to date.", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        // Stack of cards
                        val displayClaims = pendingClaims.take(2).reversed()
                        
                        Box(modifier = Modifier.fillMaxSize()) {
                            displayClaims.forEachIndexed { index, claim ->
                                key(claim.id) {
                                    if (index == displayClaims.lastIndex) {
                                        LaunchedEffect(claim) {
                                            viewModel.analyzeClaim(claim)
                                        }
                                    }
                                    
                                    SwipeCard(
                                        claim = claim,
                                        onSwipedLeft = {
                                            viewModel.handleSwipe(claim, isApproved = false)
                                        },
                                        onSwipedRight = {
                                            viewModel.handleSwipe(claim, isApproved = true)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bottom control buttons
                if (uiState.totalCount > 0 && pendingClaims.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp, top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(
                            onClick = { viewModel.handleSwipe(pendingClaims.first(), false) },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Filled.Block, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                        }

                        FloatingActionButton(
                            onClick = { viewModel.undoLastSwipe() },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = if (uiState.canUndo) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }

                        FloatingActionButton(
                            onClick = { viewModel.handleSwipe(pendingClaims.first(), true) },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Approve", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                } else if (uiState.totalCount > 0 && uiState.canUndo) {
                    // Show undo button when done
                     Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
                          FloatingActionButton(
                            onClick = { viewModel.undoLastSwipe() },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Filled.Undo, contentDescription = "Undo")
                        }
                     }
                }
            }
        }
    }
}
