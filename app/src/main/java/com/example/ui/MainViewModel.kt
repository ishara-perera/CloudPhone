package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AcceptanceAuditItem
import com.example.data.model.CallRecordEntity
import com.example.data.model.NetworkAuditState
import com.example.data.model.ProfileEntity
import com.example.data.model.QuotaState
import com.example.data.model.SmsMessageEntity
import com.example.data.repository.CloudPhoneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NavigationTab(val title: String) {
    PROFILES("Profiles"),
    STREAM("Cloud Phone"),
    NETWORK("US Network"),
    TELEPHONY("Telephony"),
    AUDITS("Audits & Quota")
}

data class ActiveCallState(
    val isActive: Boolean = false,
    val phoneNumber: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

data class RemoteStreamUiState(
    val isConnected: Boolean = true,
    val isPowerOn: Boolean = true,
    val currentApp: String = "Launcher / Home",
    val sessionSecondsRemaining: Int = 180, // Appetize 3-min timeout benchmark
    val touchFeedbackCoord: Pair<Float, Float>? = null,
    val isInstallSheetOpen: Boolean = false,
    val lastKeySent: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CloudPhoneRepository(database)

    val allProfiles: StateFlow<List<ProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<ProfileEntity?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val networkState: StateFlow<NetworkAuditState> = repository.networkState

    val quotaState: StateFlow<QuotaState> = repository.quotaState

    val smsMessages: StateFlow<List<SmsMessageEntity>> = activeProfile.flatMapLatest { profile ->
        if (profile != null) repository.getSmsForProfile(profile.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callRecords: StateFlow<List<CallRecordEntity>> = activeProfile.flatMapLatest { profile ->
        if (profile != null) repository.getCallsForProfile(profile.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(NavigationTab.STREAM)
    val currentTab = _currentTab.asStateFlow()

    private val _callState = MutableStateFlow(ActiveCallState())
    val callState = _callState.asStateFlow()

    private val _streamState = MutableStateFlow(RemoteStreamUiState())
    val streamState = _streamState.asStateFlow()

    private var callTimerJob: Job? = null
    private var streamTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfEmpty()
        }
        startSessionCountdown()
    }

    private fun startSessionCountdown() {
        streamTimerJob?.cancel()
        streamTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _streamState.update { current ->
                    if (current.isConnected && current.sessionSecondsRemaining > 0) {
                        current.copy(sessionSecondsRemaining = current.sessionSecondsRemaining - 1)
                    } else if (current.sessionSecondsRemaining <= 0) {
                        current.copy(isConnected = false)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.switchProfile(profileId)
            _streamState.update {
                it.copy(
                    sessionSecondsRemaining = 180,
                    isConnected = true,
                    currentApp = "Launcher / Home"
                )
            }
        }
    }

    fun createProfile(name: String, colorHex: String, proxyHost: String, proxyPort: Int, city: String, phone: String) {
        viewModelScope.launch {
            val id = repository.createProfile(name, colorHex, proxyHost, proxyPort, city, phone)
            repository.switchProfile(id)
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun toggleKillSwitch(enabled: Boolean) {
        viewModelScope.launch {
            val p = activeProfile.value
            if (p != null) {
                repository.toggleKillSwitch(enabled, p.id)
            }
        }
    }

    fun simulateProxyDrop(triggerKillSwitch: Boolean) {
        repository.simulateProxyDrop(triggerKillSwitch)
    }

    fun restoreProxyConnection() {
        repository.restoreProxyConnection()
    }

    fun refreshNetworkAudit() {
        viewModelScope.launch {
            repository.refreshEgressAudit()
        }
    }

    fun sendSms(recipient: String, body: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.sendSms(p.id, recipient, body)
        }
    }

    fun simulateIncomingSms(sender: String, body: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.receiveSimulatedSms(p.id, sender, body)
        }
    }

    fun startCall(number: String) {
        if (number.isBlank()) return
        _callState.update {
            ActiveCallState(
                isActive = true,
                phoneNumber = number,
                durationSeconds = 0,
                isMuted = false,
                isSpeakerOn = false
            )
        }
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_callState.value.isActive) {
                delay(1000)
                _callState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        _callState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _callState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun endCall() {
        val duration = _callState.value.durationSeconds
        val number = _callState.value.phoneNumber
        callTimerJob?.cancel()
        _callState.update { ActiveCallState(isActive = false) }

        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.logCall(p.id, number, "OUTGOING", duration)
        }
    }

    fun simulateIncomingCall(number: String) {
        startCall(number)
    }

    fun sendRemoteTouch(x: Float, y: Float) {
        _streamState.update { it.copy(touchFeedbackCoord = Pair(x, y)) }
        viewModelScope.launch {
            delay(400)
            _streamState.update { it.copy(touchFeedbackCoord = null) }
        }
    }

    fun sendRemoteKey(keyLabel: String) {
        _streamState.update { it.copy(lastKeySent = keyLabel) }
        viewModelScope.launch {
            delay(800)
            _streamState.update { it.copy(lastKeySent = null) }
        }
    }

    fun triggerRemoteNavigation(action: String) {
        when (action) {
            "HOME" -> _streamState.update { it.copy(currentApp = "Launcher / Home") }
            "BACK" -> _streamState.update { it.copy(currentApp = "Launcher / Home") }
            "RECENTS" -> _streamState.update { it.copy(currentApp = "App Switcher (3 Tasks)") }
            "POWER" -> _streamState.update { it.copy(isPowerOn = !it.isPowerOn) }
            "RECONNECT" -> _streamState.update { it.copy(isConnected = true, sessionSecondsRemaining = 180) }
        }
    }

    fun launchAppOnCloud(appName: String) {
        _streamState.update { it.copy(currentApp = appName) }
    }

    fun installApk(appName: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.installApkToProfile(p.id, appName)
        }
    }

    fun uninstallApk(appName: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.uninstallApkFromProfile(p.id, appName)
        }
    }

    fun getAcceptanceAudits(): List<AcceptanceAuditItem> {
        return repository.getAcceptanceAudits()
    }
}
