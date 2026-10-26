package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.NetworkAuditState
import com.example.data.model.ProfileEntity
import com.example.ui.RemoteStreamUiState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkPrimaryContainer
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.SlateNavySurfaceVariant

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StreamScreen(
    activeProfile: ProfileEntity?,
    streamState: RemoteStreamUiState,
    networkState: NetworkAuditState,
    onSendTouch: (Float, Float) -> Unit,
    onSendKey: (String) -> Unit,
    onTriggerNav: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onInstallApk: (String) -> Unit,
    onUninstallApk: (String) -> Unit,
    onRestoreInternet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var keyboardInputText by remember { mutableStateOf("") }
    var showInstallDialog by remember { mutableStateOf(false) }
    var customApkName by remember { mutableStateOf("") }

    // Live Web Browser state
    var browserUrl by remember { mutableStateOf("https://ipinfo.io") }
    var urlBarInput by remember { mutableStateOf("https://ipinfo.io") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isWebLoading by remember { mutableStateOf(false) }

    val installedAppsList = remember(activeProfile?.installedAppsCsv) {
        activeProfile?.installedAppsCsv?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
            ?: listOf("Firefox Focus", "Signal Private", "Web Browser")
    }

    val isBrowserActive = streamState.isConnected &&
        streamState.isPowerOn &&
        (streamState.currentApp.contains("Firefox", ignoreCase = true) ||
            streamState.currentApp.contains("Browser", ignoreCase = true) ||
            streamState.currentApp.contains("Tor", ignoreCase = true) ||
            streamState.currentApp.contains("Web", ignoreCase = true) ||
            streamState.currentApp.contains("DuckDuckGo", ignoreCase = true))

    fun navigateToInput(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return
        val target = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${Uri.encode(trimmed)}"
        }
        urlBarInput = target
        browserUrl = target
        webViewInstance?.loadUrl(target)
    }

    // FULL-SCREEN PHONE BROWSER MODE
    if (isBrowserActive) {
        BackHandler {
            if (webViewInstance?.canGoBack() == true) {
                webViewInstance?.goBack()
            } else {
                onTriggerNav("HOME")
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DeepNavyBg)
                .testTag("fullscreen_browser_container")
        ) {
            // URL Bar pinned right at the very top for full-screen browsing
            Surface(
                color = SlateNavySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    webViewInstance?.goBack()
                                } else {
                                    onTriggerNav("HOME")
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("browser_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        OutlinedTextField(
                            value = urlBarInput,
                            onValueChange = { urlBarInput = it },
                            placeholder = { Text("Search or enter URL...", fontSize = 12.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "HTTPS",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = { navigateToInput(urlBarInput) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderOutline,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(horizontal = 2.dp)
                                .testTag("browser_url_input")
                        )

                        IconButton(
                            onClick = { navigateToInput(urlBarInput) },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("browser_go_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("browser_reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onTriggerNav("HOME") },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("browser_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Browser",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Ultra-slim Quick Bookmarks Bar
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateNavySurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bookmarks = listOf(
                            "IP Check" to "https://ipinfo.io",
                            "Google" to "https://www.google.com",
                            "DuckDuckGo" to "https://duckduckgo.com",
                            "Cloudflare" to "https://one.one.one.one",
                            "Wikipedia" to "https://en.wikipedia.org"
                        )
                        items(bookmarks) { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF162137))
                                    .border(1.dp, BorderOutline, RoundedCornerShape(12.dp))
                                    .clickable {
                                        urlBarInput = url
                                        browserUrl = url
                                        webViewInstance?.loadUrl(url)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CyberCyan)
                            }
                        }
                    }

                    if (isWebLoading) {
                        LinearProgressIndicator(
                            color = CyberCyan,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        )
                    }
                }
            }

            // Full-Screen WebView Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                if (networkState.isSimulatedBlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DeepNavyBg)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Blocked",
                                tint = AccentRed,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "INTERNET BLOCKED BY KILL-SWITCH",
                                color = AccentRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "US proxy connection is dropped. All non-tunneled traffic has been severed to prevent direct IP leaks.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = onRestoreInternet,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentEmerald,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Restore US Proxy & Reconnect", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                CookieManager.getInstance().setAcceptCookie(true)

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isWebLoading = true
                                        if (url != null) urlBarInput = url
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isWebLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        isWebLoading = false
                                    }
                                }
                                loadUrl(browserUrl)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            if (webView.url != browserUrl && !networkState.isSimulatedBlocked) {
                                webView.loadUrl(browserUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        return
    }

    // STANDARD CLOUD PHONE LAUNCHER & DEVICE CONTROLLER VIEW
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Internet Connection Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (networkState.isSimulatedBlocked) AccentRed.copy(alpha = 0.2f) else SlateNavySurface
                ),
                border = BorderStroke(
                    1.dp,
                    if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (networkState.isSimulatedBlocked) "INTERNET SEVERED (KILL-SWITCH BLOCKED)" else "INTERNET ONLINE: US TUNNEL VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (networkState.isSimulatedBlocked) AccentRed else AccentEmerald
                                )
                            )
                            Text(
                                text = if (networkState.isSimulatedBlocked) "Proxy disconnected. Tap 'Reconnect' to restore." else "Egress: ${networkState.publicIp} (${networkState.city})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (networkState.isSimulatedBlocked) {
                        Button(
                            onClick = onRestoreInternet,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("restore_internet_banner_btn")
                        ) {
                            Text("Reconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onLaunchApp("Web Browser") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_fullscreen_browser_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Full Screen Browser", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browser", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Remote Cloud Phone Chassis Display
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF0F1522))
                        .border(3.dp, BorderOutline, RoundedCornerShape(32.dp))
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF060911))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    onSendTouch(offset.x, offset.y)
                                }
                            }
                            .testTag("cloud_phone_touch_surface")
                    ) {
                        if (!streamState.isPowerOn) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "DEVICE POWERED OFF\nTap Power button below to turn on",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (!streamState.isConnected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Timeout",
                                        tint = AccentAmber,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "3-Minute Session Timeout Reached",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AccentAmber
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Appetize.io free sessions disconnect after 3 minutes. Tap below to reconnect with fresh quota.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { onTriggerNav("RECONNECT") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberCyan,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reconnect Cloud Stream", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Active Remote Android Display
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Remote Android Status Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A))
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "10:24 AM",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (networkState.isSimulatedBlocked) "NO SIGNAL" else "5G US",
                                            color = if (networkState.isSimulatedBlocked) AccentRed else CyberCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = "WiFi",
                                            tint = if (networkState.isSimulatedBlocked) AccentRed else Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("94%", color = Color.White, fontSize = 10.sp)
                                    }
                                }

                                if (streamState.currentApp.contains("Launcher", ignoreCase = true)) {
                                    // Launcher Home Screen
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Fullscreen Browser Launch Search Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(22.dp))
                                                .background(SlateNavySurfaceVariant)
                                                .border(1.dp, CyberCyan, RoundedCornerShape(22.dp))
                                                .clickable { onLaunchApp("Web Browser") }
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                                .testTag("launcher_browser_search_bar")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint = CyberCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Touch to open Full-Screen Browser...",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Fullscreen,
                                                    contentDescription = "Full Screen",
                                                    tint = CyberCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        // App Grid inside Cloud Phone
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            installedAppsList.take(4).forEach { appName ->
                                                val isBrowserApp = appName.contains("Browser", ignoreCase = true) ||
                                                    appName.contains("Firefox", ignoreCase = true) ||
                                                    appName.contains("Tor", ignoreCase = true)
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .clickable { onLaunchApp(appName) }
                                                        .padding(6.dp)
                                                        .testTag("cloud_app_icon_${appName.lowercase().replace(" ", "_")}")
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(52.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(if (isBrowserApp) DarkPrimaryContainer else SlateNavySurfaceVariant)
                                                            .border(1.dp, CyberCyan, RoundedCornerShape(14.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isBrowserApp) Icons.Default.Language else Icons.Default.Smartphone,
                                                            contentDescription = appName,
                                                            tint = CyberCyan,
                                                            modifier = Modifier.size(26.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = appName.take(12),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isBrowserApp) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }

                                        // Device Egress Card inside Cloud Phone
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SlateNavySurface)
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = "Egress",
                                                    tint = AccentEmerald,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Network: ${networkState.publicIp}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "City: ${networkState.city} • Latency: ${networkState.latencyMs}ms",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Non-browser App Window inside Cloud Phone (e.g. Signal Private, Chase Mobile)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF131B2E))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = streamState.currentApp.uppercase(),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = CyberCyan,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Package: com.app.${streamState.currentApp.lowercase().replace(" ", "")}",
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Running in isolated user namespace (${activeProfile?.name}). Storage, cookies, and network sockets segregated.",
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (streamState.lastKeySent != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(CyberCyan.copy(alpha = 0.2f))
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Remote Keystroke: \"${streamState.lastKeySent}\"",
                                                    color = CyberCyan,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onLaunchApp("Web Browser") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CyberCyan,
                                                    contentColor = Color.Black
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Open Full-Screen Browser", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = { onTriggerNav("HOME") },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Exit to Launcher", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                if (streamState.touchFeedbackCoord != null) {
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = (streamState.touchFeedbackCoord.first / 4).dp,
                                                y = (streamState.touchFeedbackCoord.second / 4).dp
                                            )
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, CyberCyan, CircleShape)
                                            .background(CyberCyan.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware Navigation Controls
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                    border = BorderStroke(1.dp, BorderOutline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onTriggerNav("BACK") },
                            modifier = Modifier.testTag("nav_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onTriggerNav("HOME") },
                            modifier = Modifier.testTag("nav_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Home",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onLaunchApp("Web Browser") },
                            modifier = Modifier.testTag("nav_browser_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Full-Screen Browser",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { onTriggerNav("POWER") },
                            modifier = Modifier.testTag("nav_power_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power",
                                tint = if (streamState.isPowerOn) AccentEmerald else AccentRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Remote Keyboard Input Sender
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Remote Keyboard Input",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keyboardInputText,
                            onValueChange = { keyboardInputText = it },
                            placeholder = { Text("Send text to cloud device...", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderOutline,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("remote_keyboard_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (keyboardInputText.isNotBlank()) {
                                    onSendKey(keyboardInputText)
                                    keyboardInputText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("send_remote_key_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Send",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Installed APKs Manager & Installer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavySurface),
                border = BorderStroke(1.dp, BorderOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Installed Android APKs",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            )
                            Text(
                                text = "Isolated to ${activeProfile?.name ?: "Active Profile"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = { showInstallDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkPrimaryContainer,
                                contentColor = CyberCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("install_new_apk_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Install APK",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    installedAppsList.forEach { appName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateNavySurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Smartphone,
                                        contentDescription = appName,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Permissions: Internet, Egress Proxy",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Row {
                                OutlinedButton(
                                    onClick = { onLaunchApp(appName) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Launch",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Open", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onUninstallApk(appName) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Uninstall",
                                        tint = AccentRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = {
                Text(
                    text = "Install APK to Remote Phone",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select from pre-vetted privacy packages or enter custom package name:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    val presets = listOf("Tor Browser", "Firefox Focus", "DuckDuckGo", "Aurora Store", "Proton Mail")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SlateNavySurfaceVariant)
                                    .border(1.dp, BorderOutline, RoundedCornerShape(6.dp))
                                    .clickable { customApkName = preset }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(preset, fontSize = 10.sp, color = CyberCyan)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customApkName,
                        onValueChange = { customApkName = it },
                        label = { Text("App or APK Package Name") },
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
                        if (customApkName.isNotBlank()) {
                            onInstallApk(customApkName)
                            customApkName = ""
                            showInstallDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Install on Device", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = SlateNavySurface
        )
    }
}
