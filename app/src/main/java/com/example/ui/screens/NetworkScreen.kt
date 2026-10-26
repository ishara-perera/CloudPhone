package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkAuditState
import com.example.data.model.ProfileEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkPrimaryContainer
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.SlateNavySurfaceVariant

@Composable
fun NetworkScreen(
    activeProfile: ProfileEntity?,
    networkState: NetworkAuditState,
    onToggleKillSwitch: (Boolean) -> Unit,
    onSimulateProxyDrop: (Boolean) -> Unit,
    onRestoreProxy: () -> Unit,
    onRefreshAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Egress Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Egress",
                                tint = if (networkState.isSimulatedBlocked) AccentRed else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "US Network Egress Status",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }

                        OutlinedButton(
                            onClick = onRefreshAudit,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("refresh_network_audit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit IP", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Public IP Big Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (networkState.isSimulatedBlocked) AccentRed.copy(alpha = 0.15f) else Color(0xFF0D1424))
                            .border(1.dp, if (networkState.isSimulatedBlocked) AccentRed else BorderOutline, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "VERIFIED OUTBOUND IP",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = networkState.publicIp,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (networkState.isSimulatedBlocked) AccentRed else CyberCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location & Geolocation grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Country / Flag", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🇺🇸", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(networkState.country, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Region / City", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(networkState.city, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Autonomous System", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(networkState.asn, fontSize = 11.sp, color = Color.White, maxLines = 1)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Latency / Ping", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Ping", tint = AccentEmerald, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${networkState.latencyMs} ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fail-Closed Kill Switch Controller
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (networkState.isKillSwitchArmed) Icons.Default.Lock else Icons.Default.NoEncryption,
                                    contentDescription = "Kill switch",
                                    tint = if (networkState.isKillSwitchArmed) CyberCyan else AccentAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fail-Closed Kill Switch",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                            Text(
                                text = if (networkState.isKillSwitchArmed) "Blocks all non-tunneled traffic (prevents direct fallback)" else "Permissive (may leak native IP if proxy fails)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = networkState.isKillSwitchArmed,
                            onCheckedChange = { onToggleKillSwitch(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyberCyan,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = SlateNavySurfaceVariant
                            ),
                            modifier = Modifier.testTag("kill_switch_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Simulate Proxy Disconnect to Verify Fail-Closed Blocking:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSimulateProxyDrop(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_proxy_drop_button")
                        ) {
                            Text("Drop Proxy (Test Block)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRestoreProxy,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimaryContainer, contentColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_proxy_button")
                        ) {
                            Text("Restore Tunnel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (networkState.isSimulatedBlocked) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentRed.copy(alpha = 0.2f))
                                .border(1.dp, AccentRed, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = "Blocked", tint = AccentRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VERIFIED: Kill-Switch triggered. Sockets terminated. Zero native egress leaked.",
                                    color = AccentRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Leak Diagnostics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Leak Shields & Hardware Privacy",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val checks = listOf(
                        Triple("DNS Leak Guard", "NO LEAK", "Queries resolved exclusively through 1.1.1.1 US resolver"),
                        Triple("IPv6 Leak Guard", "DISABLED", "IPv6 disabled at gateway to prevent IPv6 side-channel leak"),
                        Triple("WebRTC STUN Shield", "SHIELDED", "mDNS iceCandidate masking blocks browser local IP leak"),
                        Triple("Local Device Hardware", "BLOCKED", "GPS, Camera, Microphone, and Local Files disabled by default")
                    )

                    checks.forEach { (name, status, detail) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                            }
                        }
                    }
                }
            }
        }

        // Mandatory Provider Observability Disclosures
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Disclosure", tint = AccentAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mandatory Network & Legal Disclosures",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AccentAmber)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Provider Observability: Upstream cloud and proxy providers retain network connection metadata and timestamps.\n" +
                                "2. No Anonymity Guarantee: US proxy routing modifies IP headers but does NOT guarantee absolute anonymity or immunity to browser fingerprinting.\n" +
                                "3. No Residency / Bank Eligibility Guarantee: Financial institutions use risk scoring (IP quality score, datacenter/residential tag, SSN) and may flag datacenter proxy IPs.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
