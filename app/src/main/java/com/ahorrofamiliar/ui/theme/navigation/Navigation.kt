package com.ahorro.familiar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahorro.familiar.ui.screens.CrearMetaScreen
import com.ahorro.familiar.ui.screens.DetalleMetaScreen
import com.ahorro.familiar.ui.screens.ListaMetasScreen
import com.ahorro.familiar.ui.screens.RegistrarPagoScreen
import com.ahorro.familiar.ui.viewmodel.MetasViewModel

/**
 * Rutas de navegación — constantes que identifican cada pantalla en el grafo.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - Centralizar las rutas en un object evita errores por strings duplicados o mal escritos.
 * - Las rutas con parámetros usan la sintaxis "{param}" que NavController parsea
 *   automáticamente y convierte al tipo declarado en navArgument.
 * - Las funciones helper (detalleMetaRuta, registrarPagoRuta) construyen la ruta
 *   con el ID concreto; esto evita concatenación de strings dispersa en el código.
 */
object Rutas {
    const val LISTA_METAS   = "lista_metas"
    const val DETALLE_META  = "detalle_meta/{metaId}"
    const val REGISTRAR_PAGO = "registrar_pago/{metaId}"
    const val CREAR_META    = "crear_meta"

    fun detalleMetaRuta(metaId: Int)    = "detalle_meta/$metaId"
    fun registrarPagoRuta(metaId: Int)  = "registrar_pago/$metaId"
}

/**
 * AhorroNavHost — grafo de navegación completo de la aplicación.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - NavHost en Compose reemplaza el FragmentManager de las apps tradicionales.
 *   Gestiona la pila de pantallas (back stack) de forma declarativa.
 * - rememberNavController() crea y recuerda el controlador entre recomposiciones.
 * - El ViewModel se crea UNA SOLA VEZ aquí (viewModel()) y se pasa a todas las
 *   pantallas como parámetro. Esto garantiza que todas comparten el mismo estado
 *   (misma lista de metas, mismos pagos, etc.) sin duplicar datos.
 *   Si cada pantalla creara su propio ViewModel, los datos estarían desincronizados.
 *
 * FLUJO DE NAVEGACIÓN:
 *   ListaMetasScreen
 *     ├── [clic en meta]  → DetalleMetaScreen(metaId)
 *     │                        └── [Registrar Pago] → RegistrarPagoScreen(metaId)
 *     └── [FAB +]         → CrearMetaScreen
 */
@Composable
fun AhorroNavHost() {
    val navController = rememberNavController()

    // ViewModel compartido: todas las pantallas observan el mismo estado.
    // viewModel() lo crea en el primer llamado y retorna la misma instancia después.
    val viewModel: MetasViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA_METAS
    ) {
        // Pantalla 1: Lista de todas las metas de ahorro
        composable(Rutas.LISTA_METAS) {
            ListaMetasScreen(
                viewModel  = viewModel,
                onMetaClick = { metaId -> navController.navigate(Rutas.detalleMetaRuta(metaId)) },
                onCrearMeta = { navController.navigate(Rutas.CREAR_META) }
            )
        }

        // Pantalla 2: Detalle de una meta (miembros, aportes, porcentaje, foto)
        composable(
            route     = Rutas.DETALLE_META,
            arguments = listOf(navArgument("metaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val metaId = backStackEntry.arguments?.getInt("metaId") ?: return@composable
            DetalleMetaScreen(
                viewModel       = viewModel,
                metaId          = metaId,
                onRegistrarPago = { navController.navigate(Rutas.registrarPagoRuta(metaId)) },
                onVolver        = { navController.popBackStack() }
            )
        }

        // Pantalla 3: Formulario para registrar un pago (POST /pagos)
        composable(
            route     = Rutas.REGISTRAR_PAGO,
            arguments = listOf(navArgument("metaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val metaId = backStackEntry.arguments?.getInt("metaId") ?: return@composable
            RegistrarPagoScreen(
                viewModel = viewModel,
                metaId    = metaId,
                onVolver  = { navController.popBackStack() }
            )
        }

        // Pantalla 4: Formulario para crear una nueva meta (POST /metas)
        composable(Rutas.CREAR_META) {
            CrearMetaScreen(
                viewModel = viewModel,
                onVolver  = { navController.popBackStack() }
            )
        }
    }
}