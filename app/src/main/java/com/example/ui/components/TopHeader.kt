package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.RemoteStreamUiState
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SlateNavySurface

@Composable
fun TopHeader(
    activeProfile: ProfileEntity?,
    networkState: NetworkAuditState,
    streamState: RemoteStreamUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SlateNavySurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderOutline, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // App branding and active profile pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Logo",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CLOUDPHONE COMMANDER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = CyberCyan
                            )
                        )
                        Text(
                            text = "US Cloud Phone & Telephony Orchestrator",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Active profile tag
                if (activeProfile != null) {
                    val profileColor = try {
                        Color(android.graphics.Color.parseColor(activeProfile.colorHex))
                    } catch (e: Exception) {
                        CyberCyan
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(profileColor.copy(alpha = 0.15f))
                            .border(1.dp, profileColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("active_profile_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(profileColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeProfile.name.take(16),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = profileColor
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // US Egress status
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (networkState.isSimulatedBlocked) AccentRed.copy(alpha = 0.15f) else AccentEmerald.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "US Egress",
                            tint = if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (networkState.isSimulatedBlocked) "SEVERED" else "US EGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald
                            )
                        )
                    }
                }

                // Kill switch status
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (networkState.isKillSwitchArmed) CyberCyan.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (networkState.isKillSwitchArmed) CyberCyan else Color.Gray,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Kill Switch",
                            tint = if (networkState.isKillSwitchArmed) CyberCyan else Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (networkState.isKillSwitchArmed) "KILL-SWITCH ARMED" else "PERMISSIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (networkState.isKillSwitchArmed) CyberCyan else Color.Gray
                            )
                        )
                    }
                }

                // Session remaining timer
                val minutes = streamState.sessionSecondsRemaining / 60
                val seconds = streamState.sessionSecondsRemaining % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateNavySurface)
                        .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Session Timer",
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeString,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CyberCyan
                            )
                        )
                    }
                }
            }
        }
    }
}
