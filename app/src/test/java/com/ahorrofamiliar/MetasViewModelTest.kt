package com.ahorro.familiar

import com.ahorro.familiar.ui.viewmodel.MetasViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias del ViewModel — validan la lógica de negocio sin red ni emulador.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - Estas pruebas se ubican en src/test/ (no en src/androidTest/) porque NO requieren
 *   el framework de Android: se ejecutan directamente en la JVM del PC, lo que las
 *   hace mucho más rápidas y confiables que las pruebas instrumentadas.
 *
 * - Se prueba calcularPorcentaje() porque es lógica de negocio pura: calcula
 *   el progreso de la meta de ahorro a partir de los montos recaudados.
 *   Si esta función falla, la barra de progreso y los porcentajes en pantalla
 *   mostrarán valores incorrectos al usuario.
 *
 * - El ViewModel puede instanciarse directamente en pruebas unitarias porque
 *   no referencia Context ni Activity (principio de diseño limpio de MVVM).
 *
 * CASOS DE PRUEBA:
 * Se cubren los casos límite más importantes de la función calcularPorcentaje:
 * 1. Caso normal (resultado esperado: 25%)
 * 2. Sin aportes (resultado esperado: 0%)
 * 3. Meta alcanzada exactamente (resultado esperado: 100%)
 * 4. Sobre-aportado (no debe superar 100%)
 * 5. División por cero si valorTotal = 0 (no debe crashear)
 * 6. Caso intermedio 80%
 */
class MetasViewModelTest {

    private lateinit var viewModel: MetasViewModel

    @Before
    fun setUp() {
        viewModel = MetasViewModel()
    }

    @Test
    fun `calcularPorcentaje con valores normales retorna 25`() {
        val resultado = viewModel.calcularPorcentaje(500000.0, 2000000.0)
        assertEquals(25, resultado)
    }

    @Test
    fun `calcularPorcentaje sin recaudar nada retorna 0`() {
        val resultado = viewModel.calcularPorcentaje(0.0, 2000000.0)
        assertEquals(0, resultado)
    }

    @Test
    fun `calcularPorcentaje cuando se alcanzo la meta retorna 100`() {
        val resultado = viewModel.calcularPorcentaje(2000000.0, 2000000.0)
        assertEquals(100, resultado)
    }

    @Test
    fun `calcularPorcentaje no supera 100 aunque se pague de mas`() {
        val resultado = viewModel.calcularPorcentaje(3000000.0, 2000000.0)
        assertEquals(100, resultado)
    }

    @Test
    fun `calcularPorcentaje con valorTotal cero retorna 0 sin dividir por cero`() {
        val resultado = viewModel.calcularPorcentaje(100.0, 0.0)
        assertEquals(0, resultado)
    }

    @Test
    fun `calcularPorcentaje con meta al 80 por ciento`() {
        val resultado = viewModel.calcularPorcentaje(800000.0, 1000000.0)
        assertEquals(80, resultado)
    }
}