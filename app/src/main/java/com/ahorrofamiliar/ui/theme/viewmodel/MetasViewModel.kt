package com.ahorro.familiar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahorro.familiar.data.model.*
import com.ahorro.familiar.data.repository.AhorroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// El ViewModel maneja TODA la lógica.
// Usa StateFlow para que Compose observe los estados reactivamente.
class MetasViewModel : ViewModel() {

    private val repository = AhorroRepository()

    // Lista de metas
    private val _metas = MutableStateFlow<List<Meta>>(emptyList())
    val metas: StateFlow<List<Meta>> = _metas

    // Detalle de una meta
    private val _detalleMeta = MutableStateFlow<Meta?>(null)
    val detalleMeta: StateFlow<Meta?> = _detalleMeta

    // Pagos de una meta
    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos

    // Estado de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Mensajes de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Mensajes de éxito
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // ---- METAS ----

    fun cargarMetas() {
        viewModelScope.launch {
            _cargando.value = true
            repository.getMetas()
                .onSuccess { _metas.value = it }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }

    fun cargarDetalleMeta(id: Int) {
        viewModelScope.launch {
            _cargando.value = true
            repository.getDetalleMeta(id)
                .onSuccess { _detalleMeta.value = it }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }

    fun crearMeta(nombre: String, descripcion: String, valorTotal: Double,
                  foto: String, nombresMiembros: List<String>) {
        if (nombre.isBlank()) { _error.value = "El nombre es obligatorio"; return }
        if (valorTotal <= 0) { _error.value = "El valor debe ser mayor a 0"; return }

        viewModelScope.launch {
            _cargando.value = true
            val request = CrearMetaRequest(
                nombre = nombre,
                descripcion = descripcion,
                valorTotal = valorTotal,
                foto = foto.ifBlank { "https://via.placeholder.com/300x200?text=Meta" },
                miembros = nombresMiembros.filter { it.isNotBlank() }.map { NombreMiembro(it) }
            )
            repository.crearMeta(request)
                .onSuccess { _mensaje.value = "Meta '${it.nombre}' creada"; cargarMetas() }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }

    fun agregarMiembro(metaId: Int, nombre: String) {
        if (nombre.isBlank()) { _error.value = "El nombre es obligatorio"; return }
        viewModelScope.launch {
            repository.agregarMiembro(metaId, nombre)
                .onSuccess { _mensaje.value = "${it.nombre} agregado"; cargarDetalleMeta(metaId) }
                .onFailure { _error.value = it.message }
        }
    }

    // ---- PAGOS ----

    fun cargarPagosDeMeta(metaId: Int) {
        viewModelScope.launch {
            _cargando.value = true
            repository.getPagosDeMeta(metaId)
                .onSuccess { _pagos.value = it }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }

    fun registrarPago(metaId: Int, miembroId: Int, monto: Double, descripcion: String) {
        if (monto <= 0) { _error.value = "El monto debe ser mayor a 0"; return }
        viewModelScope.launch {
            _cargando.value = true
            val request = CrearPagoRequest(metaId, miembroId, monto, descripcion.ifBlank { "Sin descripción" })
            repository.registrarPago(request)
                .onSuccess { _mensaje.value = "Pago de $${monto} registrado"; cargarPagosDeMeta(metaId) }
                .onFailure { _error.value = it.message }
            _cargando.value = false
        }
    }

    // ---- LÓGICA DE NEGOCIO (testeable con prueba unitaria) ----
    fun calcularPorcentaje(totalRecaudado: Double, valorTotal: Double): Int {
        if (valorTotal <= 0) return 0
        return (totalRecaudado / valorTotal * 100).toInt().coerceIn(0, 100)
    }

    fun limpiarMensaje() { _mensaje.value = null }
    fun limpiarError() { _error.value = null }
}
