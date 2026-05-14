package com.ahorro.familiar.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Emulador Android → usa 10.0.2.2 para acceder al localhost del PC
    // Celular físico → cambia por la IP local de tu PC (ej: http://192.168.1.100:3000/)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: AhorroApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AhorroApiService::class.java)
    }
}
