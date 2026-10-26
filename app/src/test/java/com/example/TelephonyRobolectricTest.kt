package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CallRecordEntity
import com.example.data.model.ProfileEntity
import com.example.data.model.SmsMessageEntity
import com.example.data.repository.CloudPhoneRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TelephonyRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CloudPhoneRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CloudPhoneRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSeedDataInitialization() = runBlocking {
        repository.initializeSeedDataIfEmpty()
        val profiles = repository.allProfiles.first()
        assertEquals(3, profiles.size)

        val activeProfile = repository.activeProfile.first()
        assertNotNull(activeProfile)
        assertEquals("US Alpha (Finance & Retail)", activeProfile?.name)
        assertEquals("+1 (202) 555-0143", activeProfile?.phoneNumber)
        assertEquals("VoIP (Programmable)", activeProfile?.carrierType)
    }

    @Test
    fun testSmsSendAndReceiveAndProfileIsolation() = runBlocking {
        repository.initializeSeedDataIfEmpty()
        val profiles = repository.allProfiles.first()
        val p1 = profiles[0]
        val p2 = profiles[1]

        // Check initial seed messages for Profile 1
        val initialP1Messages = repository.getSmsForProfile(p1.id).first()
        assertTrue("Profile 1 should have initial messages", initialP1Messages.isNotEmpty())

        // Send outbound SMS on Profile 1
        val sendSuccess = repository.sendSms(p1.id, "+1 (800) 555-0199", "Test outbound message from US East")
        assertTrue("Send SMS should succeed", sendSuccess)

        // Verify message appears in Profile 1 thread
        val updatedP1Messages = repository.getSmsForProfile(p1.id).first()
        val sentMsg = updatedP1Messages.last()
        assertEquals("+1 (800) 555-0199", sentMsg.recipient)
        assertEquals("Test outbound message from US East", sentMsg.body)
        assertFalse("Outbound message should not be marked as incoming", sentMsg.isIncoming)
        assertTrue("Message should be marked delivered", sentMsg.isDelivered)

        // Simulate incoming SMS on Profile 1 (verification code)
        repository.receiveSimulatedSms(p1.id, "VerifyService", "Your OTP is 492018")
        val withInboundP1 = repository.getSmsForProfile(p1.id).first()
        val inboundMsg = withInboundP1.last()
        assertEquals("VerifyService", inboundMsg.sender)
        assertEquals("Your OTP is 492018", inboundMsg.body)
        assertTrue("Inbound message should be marked as incoming", inboundMsg.isIncoming)

        // Verify Strict Profile Isolation: Profile 2 must NOT have Profile 1's messages
        val p2Messages = repository.getSmsForProfile(p2.id).first()
        for (msg in p2Messages) {
            assertTrue("Profile 2 must not see Profile 1 messages", msg.profileId == p2.id)
            assertFalse("Profile 2 must not contain Profile 1 message body", msg.body.contains("Test outbound message from US East"))
        }
    }

    @Test
    fun testVoiceCallLoggingAndIsolation() = runBlocking {
        repository.initializeSeedDataIfEmpty()
        val profiles = repository.allProfiles.first()
        val p1 = profiles[0]
        val p2 = profiles[1]

        // Log an outgoing call on Profile 1
        repository.logCall(p1.id, "+1 (202) 555-0199", "OUTGOING", 95)
        val p1Calls = repository.getCallsForProfile(p1.id).first()
        val latestCall = p1Calls.first()
        assertEquals("+1 (202) 555-0199", latestCall.phoneNumber)
        assertEquals("OUTGOING", latestCall.callType)
        assertEquals(95, latestCall.durationSeconds)

        // Verify Profile 2 call log is isolated
        val p2Calls = repository.getCallsForProfile(p2.id).first()
        for (call in p2Calls) {
            assertEquals(p2.id, call.profileId)
            assertFalse(call.phoneNumber == "+1 (202) 555-0199" && call.durationSeconds == 95)
        }
    }
}
