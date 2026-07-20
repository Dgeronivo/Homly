package com.dgero.homly.core.locale

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dgero.homly.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocaleContextTest {

    @Test
    fun withForcedLocale_resolvesUkrainianStrings_regardlessOfDeviceLocale() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val englishDeviceContext = targetContext.withForcedLocale(Locale.ENGLISH)

        val forcedUkrainianContext = englishDeviceContext.withForcedLocale(Locale.forLanguageTag("uk"))

        assertEquals("Вхід", forcedUkrainianContext.getString(R.string.sign_in))
    }
}
