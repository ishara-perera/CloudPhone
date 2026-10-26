package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CallRecordEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.SmsMessageEntity
import com.example.ui.ActiveCallState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkPrimaryContainer
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.SlateNavySurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelephonyScreen(
    activeProfile: ProfileEntity?,
    smsMessages: List<SmsMessageEntity>,
    callRecords: List<CallRecordEntity>,
    callState: ActiveCallState,
    onSendSms: (String, String) -> Unit,
    onSimulateIncomingSms: (String, String) -> Unit,
    onStartCall: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var smsRecipientInput by remember { mutableStateOf("+1 (202) 555-0199") }
    var smsBodyInput by remember { mutableStateOf("") }
    var dialerNumber by remember { mutableStateOf("") }
    var showInboundSmsDialog by remember { mutableStateOf(false) }
    var simulatedSender by remember { mutableStateOf("+1 (800) 555-0133") }
    var simulatedMessageBody by remember { mutableStateOf("Your login verification code is 849201. Valid for 10 minutes.") }

    val assignedNumber = activeProfile?.phoneNumber ?: "+1 (202) 555-0143"
    val carrierType = activeProfile?.carrierType ?: "VoIP (Programmable)"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Phone Number & Carrier Type Card
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
                            Text(
                                text = "ASSIGNED US TELEPHONE LINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = assignedNumber,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentAmber.copy(alpha = 0.2f))
                                .border(1.dp, AccentAmber, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SimCard, contentDescription = "Carrier", tint = AccentAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = carrierType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Carrier Routing: ${activeProfile?.carrierName ?: "Twilio Programmable Voice & SMS"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Verification code acceptance disclosure
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161F30))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Notice", tint = AccentAmber, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VoIP Disclosure: This number is classified as VoIP (E.164). Major US financial institutions, WhatsApp, and Google may reject VoIP numbers for SMS OTP verification codes. Physical SIM numbers require monthly recurring carrier contracts and cannot be provided at $0.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                            )
                        }
                    }
                }
            }
        }

        // Telephony Tabs (SMS vs Calls)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateNavySurface,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberCyan
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Sms, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS Inbox (${smsMessages.size})", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("telephony_sms_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Calls", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calls & Dialer (${callRecords.size})", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("telephony_calls_tab")
                )
            }
        }

        if (selectedTab == 0) {
            // SMS Tab Content
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Message Thread (Isolated to ${activeProfile?.name ?: "Profile"})",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    OutlinedButton(
                        onClick = { showInboundSmsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("simulate_inbound_sms_button")
                    ) {
                        Text("+ Inbound SMS Test", fontSize = 11.sp)
                    }
                }
            }

            // Message Bubbles List
            if (smsMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateNavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No SMS messages for this profile yet.\nSend a message below or trigger an inbound SMS test.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(smsMessages) { msg ->
                    val isIncoming = msg.isIncoming
                    val timeFormatted = remember(msg.timestamp) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End
                    ) {
                        Text(
                            text = if (isIncoming) "From: ${msg.sender}" else "To: ${msg.recipient}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isIncoming) 2.dp else 12.dp,
                                        bottomEnd = if (isIncoming) 12.dp else 2.dp
                                    )
                                )
                                .background(if (isIncoming) SlateNavySurface else DarkPrimaryContainer)
                                .border(1.dp, if (isIncoming) BorderOutline else CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = msg.body,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = timeFormatted,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!isIncoming) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (msg.isDelivered) "✓ Delivered" else "Sending...",
                                            fontSize = 9.sp,
                                            color = CyberCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Compose & Send SMS Bar
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Send Outbound SMS via US Line",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = smsRecipientInput,
                            onValueChange = { smsRecipientInput = it },
                            label = { Text("Recipient Phone Number") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderOutline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = smsBodyInput,
                                onValueChange = { smsBodyInput = it },
                                placeholder = { Text("Type message body...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = BorderOutline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sms_input_field")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (smsBodyInput.isNotBlank()) {
                                        onSendSms(smsRecipientInput, smsBodyInput)
                                        smsBodyInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("send_sms_button")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Voice Calls & Dialer Tab
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interactive US Phone Dialer",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Number Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D1424))
                                .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (dialerNumber.isBlank()) "Enter phone number" else dialerNumber,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dialerNumber.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3x4 Dialpad Grid
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("*", "0", "#")
                        )

                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { digit ->
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SlateNavySurfaceVariant)
                                            .border(1.dp, BorderOutline, CircleShape)
                                            .clickable { dialerNumber += digit },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = digit,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Call Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (dialerNumber.isNotEmpty()) dialerNumber = dialerNumber.dropLast(1) }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    if (dialerNumber.isNotBlank()) {
                                        onStartCall(dialerNumber)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald, contentColor = Color.Black),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("start_call_button")
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", modifier = Modifier.size(24.dp))
                            }

                            TextButton(onClick = { dialerNumber = "" }) {
                                Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Call History List
            item {
                Text(
                    text = "Recent Call History (${callRecords.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            if (callRecords.isEmpty()) {
                item {
                    Text(
                        text = "No recent call activity for this profile.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            } else {
                items(callRecords) { call ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateNavySurface)
                            .border(1.dp, BorderOutline, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (call.callType) {
                                "INCOMING" -> Icons.Default.CallReceived
                                "OUTGOING" -> Icons.Default.CallMade
                                else -> Icons.Default.CallMissed
                            }
                            val iconColor = when (call.callType) {
                                "INCOMING" -> AccentEmerald
                                "OUTGOING" -> CyberCyan
                                else -> AccentRed
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = call.callType, tint = iconColor, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = call.phoneNumber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${call.callType} • ${call.durationSeconds}s duration • ${call.carrierInfo}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onStartCall(call.phoneNumber) }) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Callback", tint = CyberCyan)
                        }
                    }
                }
            }
        }
    }

    // Active Two-Way Call Sheet / Modal
    if (callState.isActive) {
        AlertDialog(
            onDismissRequest = { /* Force explicit hang up */ },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Active call", tint = AccentEmerald, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CALL IN PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentEmerald, letterSpacing = 1.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = callState.phoneNumber,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val minutes = callState.durationSeconds / 60
                    val seconds = callState.durationSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Two-Way Audio via Twilio WebRTC Bridge",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (callState.isMuted) AccentRed.copy(alpha = 0.2f) else SlateNavySurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (callState.isMuted) AccentRed else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (callState.isMuted) "Muted" else "Mute", fontSize = 10.sp, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onToggleSpeaker,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (callState.isSpeakerOn) CyberCyan.copy(alpha = 0.2f) else SlateNavySurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                    contentDescription = "Speaker",
                                    tint = if (callState.isSpeakerOn) CyberCyan else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (callState.isSpeakerOn) "Speaker ON" else "Speaker", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("end_call_button")
                ) {
                    Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End Call", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SlateNavySurface
        )
    }

    // Inbound SMS Test Dialog
    if (showInboundSmsDialog) {
        AlertDialog(
            onDismissRequest = { showInboundSmsDialog = false },
            title = {
                Text(
                    text = "Verify Real Inbound SMS Delivery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyberCyan)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Simulates carrier webhook delivering an incoming SMS to this profile's private number:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = simulatedSender,
                        onValueChange = { simulatedSender = it },
                        label = { Text("Sender Phone / Service") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = simulatedMessageBody,
                        onValueChange = { simulatedMessageBody = it },
                        label = { Text("Message Body") },
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
                        onSimulateIncomingSms(simulatedSender, simulatedMessageBody)
                        showInboundSmsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("Deliver to Phone", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInboundSmsDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = SlateNavySurface
        )
    }
}
