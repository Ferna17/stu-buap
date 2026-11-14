package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.buap.stu.auth.ValidadorRegistro
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.ResultadoRegistro
import mx.buap.stu.data.UsuarioSTU

/**
 * ViewModel para la pantalla de Registro de Nuevo Estudiante (HU-01.1).
 * Mantiene el mismo patrón MVVM y medición de métricas en vivo que IngresoViewModel.
 */
class RegistroViewModel(
    private val repositorio: RepositorioUsuario
) : ViewModel() {

    var nombres by mutableStateOf("")
        private set

    var apellidos by mutableStateOf("")
        private set

    var correo by mutableStateOf("")
        private set

    var matricula by mutableStateOf("")
        private set

    var contrasena by mutableStateOf("")
        private set

    var resultado by mutableStateOf<ResultadoRegistro?>(null)
        private set

    private var precargaAplicada = false
    private val metricas = MedidorMetricasCaptura()

    val tiempoRespuestaMs: String
        get() = metricas.tiempoRespuestaMs

    val tiempoCapturaSeg: String
        get() = metricas.tiempoCapturaSeg

    val erroresDetectados: Int
        get() = metricas.erroresDetectados

    fun iniciarTemporizador() {
        metricas.iniciarTemporizador()
    }

    fun precargarDatos(correoInicial: String, matriculaInicial: String, contrasenaInicial: String) {
        if (precargaAplicada) return
        if (correoInicial.isNotBlank() || matriculaInicial.isNotBlank() || contrasenaInicial.isNotBlank()) {
            correo = correoInicial
            matricula = matriculaInicial
            contrasena = contrasenaInicial
        }
        precargaAplicada = true
    }

    fun actualizarNombres(nuevo: String) {
        iniciarTemporizador()
        nombres = nuevo
    }

    fun actualizarApellidos(nuevo: String) {
        iniciarTemporizador()
        apellidos = nuevo
    }

    fun actualizarCorreo(nuevo: String) {
        iniciarTemporizador()
        correo = nuevo
    }

    fun actualizarMatricula(nueva: String) {
        iniciarTemporizador()
        matricula = nueva
    }

    fun actualizarContrasena(nueva: String) {
        iniciarTemporizador()
        contrasena = nueva
    }

    fun ejecutarRegistro(onRegistroExitoso: (UsuarioSTU) -> Unit) {
        val respuesta = metricas.medir(esExitoso = { it.ok && it.usuario != null }) {
            ValidadorRegistro.validarRegistro(
                correo = correo,
                matricula = matricula,
                contrasena = contrasena,
                baseDeDatos = repositorio.obtenerPadron(),
                nombres = nombres,
                apellidos = apellidos
            )
        }
        resultado = respuesta

        if (respuesta.ok && respuesta.usuario != null) {
            repositorio.registrarUsuario(respuesta.usuario)
            onRegistroExitoso(respuesta.usuario)
        }
    }

    class Factory(private val repositorio: RepositorioUsuario) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
                return RegistroViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
