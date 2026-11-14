package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.buap.stu.auth.ValidadorLogin
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.ResultadoLogin
import mx.buap.stu.data.UsuarioSTU

/**
 * ViewModel para la pantalla de Ingreso (Login HU-01).
 * Implementa el patrón MVVM separando el estado y la lógica de validación de la interfaz gráfica Compose.
 */
class IngresoViewModel(
    private val repositorio: RepositorioUsuario
) : ViewModel() {

    var correo by mutableStateOf("")
        private set

    var matricula by mutableStateOf("")
        private set

    var contrasena by mutableStateOf("")
        private set

    var resultado by mutableStateOf<ResultadoLogin?>(null)
        private set

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

    /**
     * Limpia mensajes o errores residuales de intentos previos de inicio de sesión.
     */
    fun limpiarErrorPrevio() {
        if (resultado?.ok == false) {
            resultado = null
        }
    }

    /**
     * Precarga los datos del nuevo estudiante registrado y activa el aviso de éxito (Flujo B).
     */
    fun aplicarRegistroExitoso(usuario: UsuarioSTU) {
        correo = usuario.correo
        matricula = usuario.matricula
        contrasena = usuario.contrasena
        resultado = ResultadoLogin(
            ok = true,
            mensaje = "¡Registro exitoso! Ya puedes ingresar al STU con tu cuenta.",
            usuario = usuario
        )
    }

    fun ejecutarLogin(onLoginExitoso: (UsuarioSTU) -> Unit) {
        val respuesta = metricas.medir(esExitoso = { it.ok && it.usuario != null }) {
            ValidadorLogin.validarLogin(
                correo = correo.trim(),
                matricula = matricula.trim(),
                contrasena = contrasena.trim(),
                baseDeDatos = repositorio.obtenerPadron()
            )
        }
        resultado = respuesta

        if (respuesta.ok && respuesta.usuario != null) {
            onLoginExitoso(respuesta.usuario)
        }
    }

    class Factory(private val repositorio: RepositorioUsuario) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IngresoViewModel::class.java)) {
                return IngresoViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
