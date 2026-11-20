package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.saldo.EstadoSaldoStu
import mx.buap.stu.saldo.EvaluadorSaldo
import mx.buap.stu.saldo.ResultadoSeleccionSalida

/**
 * Controlador MVVM para la pantalla principal del estudiante (HU-02: PantallaInicioStu).
 * Mantiene el estado reactivo del estudiante autenticado y sincroniza su saldo desde el repositorio.
 */
class InicioViewModel(
    private val repositorio: RepositorioUsuario
) : ViewModel() {

    var usuarioActual by mutableStateOf<UsuarioSTU?>(null)
        private set

    var estadoSaldo by mutableStateOf<EstadoSaldoStu?>(null)
        private set

    var mostrarAvisoSinPasajes by mutableStateOf(false)
        private set

    var avisoModulo by mutableStateOf<String?>(null)
        private set

    /**
     * Carga al estudiante autenticado y calcula el estado inicial de su saldo.
     */
    fun cargarUsuario(usuario: UsuarioSTU) {
        usuarioActual = usuario
        estadoSaldo = EvaluadorSaldo.evaluar(usuario)
        mostrarAvisoSinPasajes = false
        avisoModulo = null
    }

    /**
     * Consulta y refresca los datos y saldo actualizados del estudiante desde [repositorio].
     */
    fun recargarSaldoDesdeRepositorio(matricula: String): EstadoSaldoStu? {
        val matriculaLimpia = matricula.trim()
        val usuarioEnRepo = usuarioActual
            ?.takeIf { it.matricula.trim() == matriculaLimpia }
            ?.let { repositorio.buscarPorCorreo(it.correo) }
            ?: repositorio.buscarPorMatricula(matriculaLimpia)

        if (usuarioEnRepo != null) {
            usuarioActual = usuarioEnRepo
            estadoSaldo = EvaluadorSaldo.evaluar(usuarioEnRepo)
        } else {
            usuarioActual = null
            estadoSaldo = null
        }
        return estadoSaldo
    }

    /**
     * Evalúa si el estudiante actual puede seleccionar una salida o requiere aviso de recarga.
     */
    fun intentarSeleccionarSalida(): ResultadoSeleccionSalida? {
        val usuario = usuarioActual ?: return null
        val resultado = EvaluadorSaldo.intentarSeleccionarSalida(usuario)
        if (resultado.redirigirARecarga) {
            mostrarAvisoSinPasajes = true
            avisoModulo = null
        } else {
            mostrarAvisoSinPasajes = false
        }
        return resultado
    }

    fun cerrarAvisoSinPasajes() {
        mostrarAvisoSinPasajes = false
    }

    fun mostrarAvisoModulo(mensaje: String) {
        mostrarAvisoSinPasajes = false
        avisoModulo = mensaje
    }

    fun limpiarSesion() {
        usuarioActual = null
        estadoSaldo = null
        mostrarAvisoSinPasajes = false
        avisoModulo = null
    }

    class Factory(private val repositorio: RepositorioUsuario) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InicioViewModel::class.java)) {
                return InicioViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
