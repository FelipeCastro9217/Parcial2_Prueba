package com.ahorro.familiar.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient — cliente HTTP singleton para consumo de la API REST.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - Se implementa como object (Singleton de Kotlin) para garantizar que solo existe
 *   una instancia de Retrofit en toda la aplicación. Crear múltiples instancias
 *   desperdiciaría recursos (conexiones HTTP, parsers JSON, etc.).
 *
 * - BASE_URL apunta a 10.0.2.2 porque el emulador de Android trata esa IP como
 *   el localhost de la máquina host. Usar "localhost" o "127.0.0.1" dentro del
 *   emulador apuntaría al propio emulador, no al PC donde corre el backend.
 *   → Para dispositivo físico: cambiar por la IP local del PC (ej: 192.168.1.X).
 *
 * - Se usa GsonConverterFactory para deserializar automáticamente las respuestas
 *   JSON del backend en data classes de Kotlin (Meta, Pago, Miembro).
 *   Gson mapea los campos JSON por nombre al constructor de cada data class.
 *
 * - El patrón "by lazy" asegura que la instancia de Retrofit se crea solo cuando
 *   se usa por primera vez (inicialización diferida), optimizando el arranque de la app.
 *
 * - Esta clase es parte de la capa de datos (data/network). Ni el ViewModel
 *   ni las pantallas la conocen directamente; solo AhorroRepository la usa.
 */
object RetrofitClient {

    // 10.0.2.2 = localhost del PC host visto desde el emulador Android
    // Para celular físico → cambiar por la IP local del PC (ej: http://192.168.1.100:3000/)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: AhorroApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON → data classes Kotlin
            .build()
            .create(AhorroApiService::class.java)
    }
}