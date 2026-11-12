package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.buap.stu.auth.ValidadorLogin
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.ResultadoLogin
import mx.buap.stu.data.UsuarioSTU
import java.util.Locale

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

    // Métricas no funcionales en vivo
    var tiempoRespuestaMs by mutableStateOf("0.00")
        private set

    var tiempoCapturaSeg by mutableStateOf("0.0")
        private set

    var erroresDetectados by mutableIntStateOf(0)
        private set

    private var inicioCapturaNs: Long? = null

    fun iniciarTemporizador() {
        if (inicioCapturaNs == null) {
            inicioCapturaNs = System.nanoTime()
        }
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

    fun ejecutarLogin(onLoginExitoso: (UsuarioSTU) -> Unit) {
        val t0 = System.nanoTime()
        val respuesta = ValidadorLogin.validarLogin(
            correo = correo.trim(),
            matricula = matricula.trim(),
            contrasena = contrasena.trim(),
            baseDeDatos = repositorio.obtenerPadron()
        )
        val t1 = System.nanoTime()

        val duracionMs = (t1 - t0) / 1_000_000.0
        tiempoRespuestaMs = String.format(Locale.US, "%.2f", duracionMs)

        inicioCapturaNs?.let { inicio ->
            val segundos = (t1 - inicio) / 1_000_000_000.0
            tiempoCapturaSeg = String.format(Locale.US, "%.1f", segundos)
        }
        inicioCapturaNs = null
        resultado = respuesta

        if (respuesta.ok && respuesta.usuario != null) {
            onLoginExitoso(respuesta.usuario)
        } else {
            erroresDetectados++
        }
    }

    fun registrarNuevoUsuario(onRegistroExitoso: (UsuarioSTU) -> Unit) {
        val nuevo = UsuarioSTU(
            correo = correo.trim(),
            matricula = matricula.trim(),
            contrasena = contrasena.trim(),
            nombre = "Alumno BUAP (${matricula.trim()})"
        )
        repositorio.registrarUsuario(nuevo)
        onRegistroExitoso(nuevo)
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
