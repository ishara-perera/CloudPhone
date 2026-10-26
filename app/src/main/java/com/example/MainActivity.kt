package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.BottomNav
import com.example.ui.components.TopHeader
import com.example.ui.screens.AuditsScreen
import com.example.ui.screens.NetworkScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.StreamScreen
import com.example.ui.screens.TelephonyScreen
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val quotaState by viewModel.quotaState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val streamState by viewModel.streamState.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val smsMessages by viewModel.smsMessages.collectAsStateWithLifecycle()
    val callRecords by viewModel.callRecords.collectAsStateWithLifecycle()

    val isFullScreenBrowser = currentTab == NavigationTab.STREAM &&
        streamState.isConnected &&
        streamState.isPowerOn &&
        (streamState.currentApp.contains("Firefox", ignoreCase = true) ||
            streamState.currentApp.contains("Browser", ignoreCase = true) ||
            streamState.currentApp.contains("Tor", ignoreCase = true) ||
            streamState.currentApp.contains("Web", ignoreCase = true) ||
            streamState.currentApp.contains("DuckDuckGo", ignoreCase = true))

    Scaffold(
        bottomBar = {
            if (!isFullScreenBrowser) {
                BottomNav(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        },
        containerColor = DeepNavyBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isFullScreenBrowser) androidx.compose.ui.unit.Dp(0f) else innerPadding.calculateBottomPadding())
                .statusBarsPadding()
                .then(if (isFullScreenBrowser) Modifier.navigationBarsPadding() else Modifier)
                .background(DeepNavyBg)
        ) {
            if (!isFullScreenBrowser) {
                TopHeader(
                    activeProfile = activeProfile,
                    networkState = networkState,
                    streamState = streamState
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when (currentTab) {
                    NavigationTab.PROFILES -> {
                        ProfilesScreen(
                            profiles = allProfiles,
                            activeProfile = activeProfile,
                            onSwitchProfile = { viewModel.switchProfile(it) },
                            onCreateProfile = { name, color, host, port, city, phone ->
                                viewModel.createProfile(name, color, host, port, city, phone)
                            },
                            onDeleteProfile = { viewModel.deleteProfile(it) }
                        )
                    }

                    NavigationTab.STREAM -> {
                        StreamScreen(
                            activeProfile = activeProfile,
                            streamState = streamState,
                            networkState = networkState,
                            onSendTouch = { x, y -> viewModel.sendRemoteTouch(x, y) },
                            onSendKey = { viewModel.sendRemoteKey(it) },
                            onTriggerNav = { viewModel.triggerRemoteNavigation(it) },
                            onLaunchApp = { viewModel.launchAppOnCloud(it) },
                            onInstallApk = { viewModel.installApk(it) },
                            onUninstallApk = { viewModel.uninstallApk(it) },
                            onRestoreInternet = {
                                viewModel.restoreProxyConnection()
                                viewModel.refreshNetworkAudit()
                            }
                        )
                    }

                    NavigationTab.NETWORK -> {
                        NetworkScreen(
                            activeProfile = activeProfile,
                            networkState = networkState,
                            onToggleKillSwitch = { viewModel.toggleKillSwitch(it) },
                            onSimulateProxyDrop = { viewModel.simulateProxyDrop(it) },
                            onRestoreProxy = { viewModel.restoreProxyConnection() },
                            onRefreshAudit = { viewModel.refreshNetworkAudit() }
                        )
                    }

                    NavigationTab.TELEPHONY -> {
                        TelephonyScreen(
                            activeProfile = activeProfile,
                            smsMessages = smsMessages,
                            callRecords = callRecords,
                            callState = callState,
                            onSendSms = { recipient, body -> viewModel.sendSms(recipient, body) },
                            onSimulateIncomingSms = { sender, body -> viewModel.simulateIncomingSms(sender, body) },
                            onStartCall = { viewModel.startCall(it) },
                            onToggleMute = { viewModel.toggleMute() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onEndCall = { viewModel.endCall() }
                        )
                    }

                    NavigationTab.AUDITS -> {
                        AuditsScreen(
                            audits = viewModel.getAcceptanceAudits(),
                            quotaState = quotaState
                        )
                    }
                }
            }
        }
    }
}
