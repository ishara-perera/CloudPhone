package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfileEntity
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkPrimaryContainer
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.SlateNavySurfaceVariant

@Composable
fun ProfilesScreen(
    profiles: List<ProfileEntity>,
    activeProfile: ProfileEntity?,
    onSwitchProfile: (Long) -> Unit,
    onCreateProfile: (String, String, String, Int, String, String) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProxyHost by remember { mutableStateOf("us-west.vpn-proxy.org") }
    var newProxyPort by remember { mutableStateOf("1080") }
    var newCity by remember { mutableStateOf("Los Angeles, CA") }
    var newPhoneNumber by remember { mutableStateOf("+1 (213) 555-0199") }
    var expandedProfileId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card with Creation Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = "Profiles", tint = CyberCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Isolated Profiles Hub",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                        Text(
                            text = "${profiles.size} Isolated Profiles configured | AOSP Multi-User",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("create_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Free-plan Limits & Isolation Disclosure
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131A2B))
                    .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Notice", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real Isolation Architecture: Unlike cosmetic dashboard tabs, each profile assigns a distinct remote Android container namespace (/data/user/{id}), independent browser cookie jars, separate local storage, dedicated US proxy socket routes, and isolated SMS/call history in Room DB.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                    )
                }
            }
        }

        // Profile Cards List
        items(profiles) { profile ->
            val isActive = profile.id == activeProfile?.id
            val profileColor = try {
                Color(android.graphics.Color.parseColor(profile.colorHex))
            } catch (e: Exception) {
                CyberCyan
            }
            val isExpanded = expandedProfileId == profile.id

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) Color(0xFF141F33) else SlateNavySurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) CyberCyan else BorderOutline
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(profileColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AccentEmerald.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentEmerald
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Device ID: ${profile.deviceSessionId} • ${profile.proxyCity}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isActive) {
                                Button(
                                    onClick = { onSwitchProfile(profile.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimaryContainer, contentColor = CyberCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("activate_profile_${profile.id}")
                                ) {
                                    Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (profiles.size > 1) {
                                IconButton(onClick = { onDeleteProfile(profile) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key telemetry preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = CyberCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = profile.phoneNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Proxy", tint = AccentEmerald, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.proxyHost}:${profile.proxyPort}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Expander for detailed isolation parameters
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedProfileId = if (isExpanded) null else profile.id }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Hide Isolation Details" else "View Isolation Details",
                            fontSize = 10.sp,
                            color = CyberCyan
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0C121F))
                                .padding(10.dp)
                        ) {
                            Text("• Storage Namespace: /data/user/${profile.id}/", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                            Text("• Dedicated Cookies: profile_${profile.id}_cookie_store", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                            Text("• US Proxy Egress: ${profile.proxyType}://${profile.proxyHost}:${profile.proxyPort}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                            Text("• Assigned US Line: ${profile.phoneNumber} (${profile.carrierName})", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                            Text("• Installed APKs: ${profile.installedAppsCsv}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Create Profile Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Create Isolated Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name (e.g. US Work)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProxyHost,
                        onValueChange = { newProxyHost = it },
                        label = { Text("US Proxy Host") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProxyPort,
                            onValueChange = { newProxyPort = it },
                            label = { Text("Port") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderOutline
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = { newCity = it },
                            label = { Text("US City") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderOutline
                            ),
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                    OutlinedTextField(
                        value = newPhoneNumber,
                        onValueChange = { newPhoneNumber = it },
                        label = { Text("Assigned US Phone Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            val colors = listOf("#00E5FF", "#B388FF", "#00E676", "#FFB300", "#FF4081")
                            val randomColor = colors[profiles.size % colors.size]
                            val port = newProxyPort.toIntOrNull() ?: 1080
                            onCreateProfile(newProfileName, randomColor, newProxyHost, port, newCity, newPhoneNumber)
                            newProfileName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("Provision Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = SlateNavySurface
        )
    }
}
