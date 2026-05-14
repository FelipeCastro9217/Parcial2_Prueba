package com.ahorro.familiar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ahorro.familiar.ui.navigation.AhorroNavHost

/**
 * MainActivity — punto de entrada único de la aplicación.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - Extiende ComponentActivity (no AppCompatActivity) porque Jetpack Compose
 *   no requiere el sistema de Fragments ni la capa de compatibilidad de AppCompat.
 * - setContent{} reemplaza setContentView(): la UI se construye como funciones
 *   Kotlin (Composables) en lugar de inflar layouts XML.
 * - La Activity solo invoca AhorroNavHost() y no conoce ninguna pantalla individual.
 *   Esto respeta el principio de responsabilidad única: la Activity gestiona
 *   el ciclo de vida, el grafo de navegación gestiona las pantallas.
 * - El tema Material 3 se aplica directamente en cada pantalla mediante Scaffold
 *   y los colores de MaterialTheme, manteniendo la Activity lo más simple posible.
 *
 * PATRÓN MVVM:
 * - La Activity no contiene lógica de negocio ni llama directamente a la API.
 * - Todo el estado de la UI es manejado por MetasViewModel (StateFlow).
 * - La capa de datos (Retrofit + Repository) es completamente independiente de la UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AhorroNavHost()
        }
    }
}