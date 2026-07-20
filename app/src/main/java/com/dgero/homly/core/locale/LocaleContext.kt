package com.dgero.homly.core.locale

import android.content.Context
import java.util.Locale

/**
 * Wraps [this] with a configuration pinned to [locale], so resource resolution
 * ignores the device's system locale and always resolves against [locale].
 */
fun Context.withForcedLocale(locale: Locale): Context {
    val configuration = resources.configuration
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}
