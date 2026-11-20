package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow
import mx.buap.stu.data.RepositorioSalidas
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.rutas.ReservaSalida
import mx.buap.stu.rutas.ResultadoReservaSalida
import mx.buap.stu.rutas.RutaStu
import mx.buap.stu.rutas.SalidaAutobus

/**
 * Controlador MVVM reactivo para el flujo desplegable por pasos de rutas, horarios, mapa de 36 asientos y reserva (HU-06).
 */
class RutasViewModel(
    private val repositorioSalidas: RepositorioSalidas
) : ViewModel() {

    val salidasFlow: StateFlow<List<SalidaAutobus>> = repositorioSalidas.salidasFlow
    val rutas: List<RutaStu> = repositorioSalidas.obtenerRutas()

    var rutaSeleccionada by mutableStateOf(
        rutas.firstOrNull()
            ?: RutaStu("CU-CU2", "CU-CU2", "Ciudad Universitaria", "CU2 Ecocampus Valsequillo")
    )
        private set

    var salidasDeRuta by mutableStateOf<List<SalidaAutobus>>(emptyList())
        private set

    var salidaSeleccionada by mutableStateOf<SalidaAutobus?>(null)
        private set

    var selectorRutaDesplegado by mutableStateOf(false)
        private set

    var selectorHorarioDesplegado by mutableStateOf(false)
        private set

    var rutaElegidaPorUsuario by mutableStateOf(false)
        private set

    var horarioElegidoPorUsuario by mutableStateOf(false)
        private set

    val mostrarMapaAsientos: Boolean
        get() = rutaElegidaPorUsuario &&
            horarioElegidoPorUsuario &&
            !selectorRutaDesplegado &&
            !selectorHorarioDesplegado &&
            salidaSeleccionada != null

    var reservaConfirmada by mutableStateOf<ReservaSalida?>(null)
        private set

    var mensajeResultado by mutableStateOf<String?>(null)
        private set

    var esErrorResultado by mutableStateOf(false)
        private set

    var mostrarAccionRecarga by mutableStateOf(false)
        private set

    init {
        sincronizarDesdeFlujo(repositorioSalidas.salidasFlow.value)
    }

    /**
     * Sincroniza las salidas de la ruta activa y actualiza en vivo el horario seleccionado si ya fue elegido.
     */
    fun sincronizarDesdeFlujo(
        todasLasSalidas: List<SalidaAutobus>,
        matriculaActual: String? = null
    ) {
        val salidasFiltradas = todasLasSalidas.filter { it.rutaId == rutaSeleccionada.id }
        salidasDeRuta = salidasFiltradas

        if (!horarioElegidoPorUsuario) {
            salidaSeleccionada = null
            reservaConfirmada = null
            return
        }

        val idSeleccionado = salidaSeleccionada?.id
        val salidaVigente = salidasFiltradas.firstOrNull { it.id == idSeleccionado }
        salidaSeleccionada = salidaVigente
        actualizarReservaDeSalida(salidaVigente, matriculaActual)
    }

    /**
     * Abre o cierra el selector desplegable de rutas; mientras está abierto oculta el mapa de asientos.
     */
    fun alternarSelectorRuta() {
        selectorRutaDesplegado = !selectorRutaDesplegado
        if (selectorRutaDesplegado) {
            selectorHorarioDesplegado = false
        }
    }

    /**
     * Abre o cierra el selector desplegable de horarios de la ruta elegida; mientras está abierto oculta el mapa.
     */
    fun alternarSelectorHorario() {
        if (!rutaElegidaPorUsuario) return
        selectorHorarioDesplegado = !selectorHorarioDesplegado
        if (selectorHorarioDesplegado) {
            selectorRutaDesplegado = false
        }
    }

    /**
     * Selecciona una ruta del catálogo, cierra el desplegable de rutas y abre la elección de horarios sin mostrar aún el mapa.
     */
    fun seleccionarRuta(
        rutaId: String,
        matriculaActual: String? = null
    ) {
        val rutaEncontrada = rutas.firstOrNull { it.id == rutaId } ?: return
        rutaSeleccionada = rutaEncontrada
        rutaElegidaPorUsuario = true
        selectorRutaDesplegado = false
        horarioElegidoPorUsuario = false
        selectorHorarioDesplegado = true
        salidaSeleccionada = null
        limpiarMensajeResultado()
        sincronizarDesdeFlujo(repositorioSalidas.salidasFlow.value, matriculaActual)
    }

    /**
     * Selecciona un horario específico de la ruta activa, pliega la lista de horarios y habilita el mapa de 36 asientos.
     */
    fun seleccionarSalida(
        salidaId: String,
        matriculaActual: String? = null
    ) {
        val salidaEncontrada = salidasDeRuta.firstOrNull { it.id == salidaId } ?: return
        rutaElegidaPorUsuario = true
        horarioElegidoPorUsuario = true
        selectorRutaDesplegado = false
        selectorHorarioDesplegado = false
        salidaSeleccionada = salidaEncontrada
        limpiarMensajeResultado()
        actualizarReservaDeSalida(salidaEncontrada, matriculaActual)
    }

    /**
     * Intenta confirmar la salida seleccionada y bloquear automáticamente el menor asiento disponible.
     */
    fun confirmarSalidaActual(usuario: UsuarioSTU): ResultadoReservaSalida {
        val salidaActual = salidaSeleccionada
            ?: return ResultadoReservaSalida(
                ok = false,
                mensaje = "Selecciona una salida disponible para continuar"
            ).also { resultado ->
                mensajeResultado = resultado.mensaje
                esErrorResultado = true
                mostrarAccionRecarga = false
            }

        val resultado = repositorioSalidas.confirmarReserva(usuario, salidaActual.id)
        mensajeResultado = resultado.mensaje
        esErrorResultado = !resultado.ok
        mostrarAccionRecarga = resultado.redirigirARecarga

        sincronizarDesdeFlujo(repositorioSalidas.salidasFlow.value, usuario.matricula)
        if (resultado.ok && resultado.reserva != null) {
            reservaConfirmada = resultado.reserva
        }
        return resultado
    }

    fun limpiarMensajeResultado() {
        mensajeResultado = null
        esErrorResultado = false
        mostrarAccionRecarga = false
    }

    private fun actualizarReservaDeSalida(
        salida: SalidaAutobus?,
        matriculaActual: String?
    ) {
        val matriculaLimpia = matriculaActual?.trim()?.takeIf { it.isNotEmpty() }
        if (salida == null || matriculaLimpia == null) {
            reservaConfirmada = null
            return
        }
        val asientoExistente = salida.buscarAsientoDeMatricula(matriculaLimpia)
        reservaConfirmada = if (asientoExistente != null) {
            ReservaSalida(
                salidaId = salida.id,
                nombreRuta = salida.nombreRuta,
                horaSalida = salida.horaSalida,
                anden = salida.anden,
                asientoAsignado = asientoExistente,
                matriculaEstudiante = matriculaLimpia
            )
        } else {
            null
        }
    }

    class Factory(
        private val repositorioSalidas: RepositorioSalidas
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RutasViewModel::class.java)) {
                return RutasViewModel(repositorioSalidas) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
