package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BloodCompatibilityUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Blood Donor", appName)
  }

  @Test
  fun `verify universal donor O negative can donate to all types`() {
    val recipients = BloodCompatibilityUtil.canDonateTo("O-")
    assertTrue(recipients.contains("A+"))
    assertTrue(recipients.contains("B+"))
    assertTrue(recipients.contains("AB+"))
    assertTrue(recipients.contains("O+"))
    assertEquals(8, recipients.size)
  }

  @Test
  fun `verify universal recipient AB positive can receive from all types`() {
    val donors = BloodCompatibilityUtil.canReceiveFrom("AB+")
    assertTrue(donors.contains("O-"))
    assertTrue(donors.contains("A+"))
    assertTrue(donors.contains("B-"))
    assertEquals(8, donors.size)
  }
}
