package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val deviceSessionId: String,
    val proxyHost: String,
    val proxyPort: Int,
    val proxyCountry: String,
    val proxyCity: String,
    val proxyType: String = "SOCKS5",
    val phoneNumber: String,
    val carrierName: String,
    val carrierType: String = "VoIP (Programmable)",
    val isKillSwitchActive: Boolean = true,
    val isProxyConnected: Boolean = true,
    val installedAppsCsv: String = "Firefox Focus,Signal Private,F-Droid Store",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val sender: String,
    val recipient: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true,
    val isDelivered: Boolean = true
)

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val phoneNumber: String,
    val callType: String, // "INCOMING", "OUTGOING", "MISSED"
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val carrierInfo: String = "Twilio US Voice Line"
)

data class NetworkAuditState(
    val publicIp: String = "198.51.100.42",
    val country: String = "United States",
    val countryCode: String = "US",
    val city: String = "Ashburn, Virginia",
    val asn: String = "AS14061 DigitalOcean LLC",
    val isKillSwitchArmed: Boolean = true,
    val isTunnelActive: Boolean = true,
    val isSimulatedBlocked: Boolean = false,
    val hasDnsLeak: Boolean = false,
    val hasWebRtcLeak: Boolean = false,
    val latencyMs: Int = 38,
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

data class QuotaState(
    val voiceMinutesUsed: Int = 18,
    val voiceMinutesLimit: Int = 50,
    val smsCountUsed: Int = 34,
    val smsCountLimit: Int = 100,
    val streamMinutesUsed: Int = 12,
    val streamMinutesLimit: Int = 30,
    val budgetCapUsd: Double = 0.00,
    val circuitBreakerTripped: Boolean = false
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val version: String,
    val sizeMb: Double,
    val permissions: List<String>,
    val isRunning: Boolean = false
)

data class AcceptanceAuditItem(
    val id: String,
    val title: String,
    val category: String,
    val status: String, // "Verified", "Requires setup", "Unsupported", "Unverified"
    val description: String,
    val technicalProof: String
)
