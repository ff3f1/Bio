package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EvolutionStage
import com.example.model.GameBalance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BioSim", appName)
  }

  @Test
  fun `evolution stage progression`() {
    assertEquals(EvolutionStage.SPORE, EvolutionStage.fromLevel(1))
    assertEquals(EvolutionStage.BUD, EvolutionStage.fromLevel(5))
    assertEquals(EvolutionStage.BLOOM, EvolutionStage.fromLevel(10))
    assertEquals(EvolutionStage.APEX, EvolutionStage.fromLevel(20))
  }

  @Test
  fun `game balance required xp`() {
    val xpLvl1 = GameBalance.getRequiredXp(1)
    val xpLvl2 = GameBalance.getRequiredXp(2)
    assertTrue("XP for level 2 must be greater than level 1", xpLvl2 > xpLvl1)
  }
}
