package com.ahorrofamiliar

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Prueba instrumentada — se ejecuta sobre un dispositivo o emulador Android real.
 * Verifica que el packageName del contexto coincida con el declarado en el Manifest.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ahorro.familiar", appContext.packageName)
    }
}