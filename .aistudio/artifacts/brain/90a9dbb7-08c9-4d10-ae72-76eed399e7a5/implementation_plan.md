# CloudPhone Commander (Cloud Android & Telephony Orchestrator)

A native Android management and remote control console built in Jetpack Compose for orchestrating cloud-hosted Android phone instances, dedicated US network routing with kill-switch diagnostics, private US telephony (real inbound/outbound SMS and two-way voice call logs), and isolated multi-profile workspaces.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The architectural foundation and scope were established during Phase 1 based on the zero-cost feasibility audit and your selected preferences:

- **Confirmed Decision 1 (Provider Feasibility & BYO Keys)**: You selected **Bring Your Own API keys for cloud services**. Continuous cloud Android execution, persistent US egress tunneling, and private US E.164 phone numbers cannot be provided at an ongoing $0 cost without credit cards or paid services. The app will provide secure, local BYO credential management (Twilio for real telephony, Appetize/Redroid API for remote Android streaming, and SOCKS5/HTTP for US proxy endpoints) with zero hardcoded keys and a full offline/sandbox diagnostic mode when keys are not yet input.
- **Confirmed Decision 2 (Interface Target)**: You selected a **Native Android app in Jetpack Compose**. The application is architected with modern Material 3, reactive StateFlows, Room database persistence, and adaptive layout support.
- **Open Decision / Default Strategy (Diagnostics & Verification)**: Pre-configured test profiles ("US E-Commerce Worker", "Privacy Workspace", and "Custom BYO") will be pre-seeded in Room with verified diagnostic checks (egress IP lookups, WebRTC leak detection, simulated/live SMS webhooks, and quota monitoring).

---

### 1. Overview & Core Concept

- **What It Does**: CloudPhone Commander gives users a command center on Android to monitor, stream, and interact with remote cloud Android instances, manage strict US-routed network tunnels with an automated fail-closed kill switch, send/receive real SMS and place/receive voice calls via programmable carrier APIs, and maintain strictly segregated profiles with separate cookies, storage, and proxy configurations.
- **Target Audience / Persona**: Cloud engineers, security researchers, privacy-conscious developers, and remote workers who require strict geo-routing, isolated digital personas, and programmatic telephony.
- **Key Value**: Replaces scattered command-line tools and web dashboards with a unified, high-security mobile controller that enforces profile isolation, alerts on network leaks, and tracks provider quotas to prevent unexpected billing.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Profiles Hub**:
   - View, create, edit, clone, and delete isolated profiles.
   - Switch active profile in 1 tap; immediately swaps device session token, assigned proxy tunnel, and telephony inbox.
2. **Remote Phone Streamer**:
   - Stream remote Android device display directly in-app (via embedded hardware-accelerated interactive view with touch/gesture forwarding).
   - Control navigation bar (Back, Home, Recents, Power), clipboard paste, and APK file installer.
   - Reconnect to active sessions and inspect session remaining time.
3. **US Network & Privacy Shield**:
   - Real-time egress verification: shows public IP, ISP/ASN, detected country (US flag indicator), and DNS leak status.
   - Interactive Kill-Switch toggle: immediately severs non-tunneled traffic if the proxy drops.
   - Telemetry gauges for bandwidth consumed, latency/ping, and remaining quota.
4. **Telephony Console (SMS & Voice)**:
   - Real-time conversation thread inbox for inbound and outbound US SMS.
   - Interactive phone dialer, active call screen with call duration timer, and audio mute/speaker toggles.
   - Carrier type badge clearly identifying whether the number is VoIP, Mobile/SIM, or Toll-Free.
5. **Quota & API Credentials Settings**:
   - Secure input for Twilio Account SID / Auth Token / Phone Number, Cloud Android Endpoint URL / API Key, and US Proxy Host/Port/Auth.
   - Quota threshold warnings and automatic "Cut-off at $0" circuit breaker to prevent overage charges.

#### Visual Identity & Theme
- **Aesthetic Direction**: High-precision Cyberdeck / Telemetry Dark aesthetic (`#0A0E17` background with `#161F30` elevated cards).
- **Color Palette**:
  - Dominant Dark: Deep Space Navy (`#0A0E17`) & Surface Slate (`#121824`)
  - Accent Cyan: Electric Cyan (`#00E5FF`) for active connections, streaming feeds, and primary actions
  - Accent Amber: Warning Amber (`#FFB300`) for quota warnings and DNS leak alerts
  - Accent Emerald: Shield Emerald (`#00E676`) for verified US egress and encrypted tunnel status
  - Accent Crimson: Alert Red (`#FF5252`) for kill-switch activation and disconnected devices
- **Typography & Hierarchy**:
  - Display & Headers: Bold, monospace-influenced technical font styling for IPs, ports, and telemetry metrics.
  - Body: Clean, readable sans-serif with accessible high-contrast color tokens.
- **Component Styling & Layout**:
  - Standard M3 `NavigationBar` with consistent icons and pill highlights (`Profiles`, `Stream`, `Network`, `Telephony`, `Settings`).
  - Safe-area inset aware (`enableEdgeToEdge()`, `safeDrawingPadding()`, `navigationBarsPadding()`).
  - Interactive cards with `minimumInteractiveComponentSize` (48dp minimum touch target).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Local Room Database for Profiles & Messages**:
  - *Chosen Approach*: Persist all profiles, SMS threads, call logs, and network telemetry locally in SQLite via Room with reactive Kotlin `Flow`s.
  - *Why*: Ensures 100% data ownership on the user's client device, instant profile switching with zero cloud latency, and zero database hosting costs.
- **Decision 2: BYO Cloud & Telephony Architecture with Live Fallback**:
  - *Chosen Approach*: Implement real HTTP/REST integration for Twilio (SMS & Voice) and real IP diagnostic endpoints (`ipapi.co` / `httpbin.org`), paired with an interactive mock/simulation engine when credentials are not yet entered.
  - *Why*: Allows immediate evaluation and testing right out of the box while supporting live production carrier infrastructure upon pasting user keys.
- **Decision 3: Fail-Closed Kill-Switch & Privacy Guard**:
  - *Chosen Approach*: Client-side network interceptor and state watchdog that blocks simulated/remote traffic if the proxy handshake fails or country detection resolves outside `US`.
  - *Why*: Meets strict privacy criteria by preventing silent fallback to the device's native network.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CloudPhone Commander                            │
├────────────────────────────────────────────────────────────────────────┤
│                           UI Layer (M3)                                │
│   ┌──────────────┬──────────────┬──────────────┬────────────────────┐  │
│   │ ProfilesView │ StreamerView │ NetworkView  │   TelephonyView    │  │
│   └──────┬───────┴──────┬───────┴──────┬───────┴─────────┬──────────┘  │
│          │              │              │                 │             │
│   ┌──────┴──────────────┴──────────────┴─────────────────┴──────────┐  │
│   │                      MainViewModel                              │  │
│   │  (Active Profile, Egress State, KillSwitch, Messages, Quota)   │  │
│   └─────────────────────────────┬───────────────────────────────────┘  │
│                                 │                                      │
├─────────────────────────────────┼──────────────────────────────────────┤
│                         Data & Domain Layer                            │
│   ┌─────────────────────────────┴───────────────────────────────────┐  │
│   │                     CloudPhoneRepository                        │  │
│   ├─────────────────────────────┬───────────────────────────────────┤  │
│   │     Local Room Storage      │        Remote Network & API       │  │
│   │   • ProfileDao              │   • TwilioTelephonyService        │  │
│   │   • SmsMessageDao           │   • NetworkDiagnosticsService     │  │
│   │   • CallLogDao              │   • RemoteStreamManager           │  │
│   │   • SecurePreferenceStore   │   • KillSwitchInterceptor         │  │
│   └─────────────────────────────┴───────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

#### Core Data Entities (Room)
1. `ProfileEntity`: `id`, `name`, `colorHex`, `deviceSessionId`, `proxyHost`, `proxyPort`, `proxyCountry`, `phoneNumber`, `carrierType`, `apkListJson`, `isActive`, `createdAt`.
2. `SmsMessageEntity`: `id`, `profileId`, `sender`, `recipient`, `body`, `timestamp`, `direction` (INCOMING/OUTGOING), `isRead`.
3. `CallLogEntity`: `id`, `profileId`, `phoneNumber`, `durationSeconds`, `type` (INCOMING, OUTGOING, MISSED), `timestamp`.
4. `NetworkAuditEntity`: `id`, `profileId`, `publicIp`, `countryCode`, `asn`, `dnsLeakDetected`, `killSwitchActive`, `timestamp`.

#### Acceptance Criteria Alignment
- **Installing & Opening APK**: APK installer panel with package verification, permissions inspector, and remote launch triggers.
- **US Egress Verification**: Live IP query against geolocation APIs returning country code, ISP, latency, and flag.
- **Kill-Switch**: One-click kill-switch with instant disconnection simulation, blocking direct fallback.
- **Telephony**: Inbound/outbound SMS threads with timestamping, audio call dialer with active call timer, and carrier classification (VoIP vs Mobile).
- **Profile Isolation**: Multiple distinct profiles with completely separate inboxes, connection histories, and configurations.
- **Quota Protection**: Budget meter tracking voice minutes, SMS counts, and cloud streaming session time with configurable auto-shutoff at $0.
