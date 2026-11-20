package mx.buap.stu.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.buap.stu.rutas.CAPACIDAD_AUTOBUS_STU
import mx.buap.stu.rutas.EstadoSalida
import mx.buap.stu.rutas.GestorSalidas
import mx.buap.stu.rutas.ResultadoReservaSalida
import mx.buap.stu.rutas.RutaStu
import mx.buap.stu.rutas.SalidaAutobus

/**
 * Contrato reactivo de acceso a rutas, salidas y bloqueo automático de asientos en tiempo real (HU-06).
 */
interface RepositorioSalidas {
    val salidasFlow: StateFlow<List<SalidaAutobus>>
    fun obtenerRutas(): List<RutaStu>
    fun obtenerSalidasPorRuta(rutaId: String): List<SalidaAutobus>
    fun buscarSalidaPorId(salidaId: String): SalidaAutobus?
    fun confirmarReserva(usuario: UsuarioSTU, salidaId: String): ResultadoReservaSalida
}

/**
 * Implementación en memoria con `StateFlow` para reflejar bloqueos de asientos al instante entre usuarios.
 */
class RepositorioSalidasMemoria(
    private val rutasIniciales: List<RutaStu> = CATALOGO_RUTAS_OFICIAL,
    salidasIniciales: List<SalidaAutobus> = crearSalidasIniciales()
) : RepositorioSalidas {

    companion object {
        val CATALOGO_RUTAS_OFICIAL = listOf(
            RutaStu("CU-CU2", "CU-CU2", "Ciudad Universitaria", "CU2 Ecocampus Valsequillo"),
            RutaStu("CU2-CU", "CU2-CU", "CU2 Ecocampus Valsequillo", "Ciudad Universitaria"),
            RutaStu("CU-Amalucan", "CU-Amalucan", "Ciudad Universitaria", "Amalucan"),
            RutaStu("CU-CAPU", "CU-CAPU", "Ciudad Universitaria", "Terminal CAPU"),
            RutaStu("CU-Cuautlancingo", "CU-Cuautlancingo", "Ciudad Universitaria", "Cuautlancingo"),
            RutaStu("CU-Maravillas", "CU-Maravillas", "Ciudad Universitaria", "Maravillas"),
            RutaStu("CU-San Ramón", "CU-San Ramón", "Ciudad Universitaria", "San Ramón"),
            RutaStu("CU-Héroes", "CU-Héroes", "Ciudad Universitaria", "Los Héroes"),
            RutaStu("CU-Tlaxcalancingo", "CU-Tlaxcalancingo", "Ciudad Universitaria", "Tlaxcalancingo")
        )

        private fun generarOcupacionInicial(
            cantidad: Int,
            prefijoMatricula: String
        ): Map<Int, String> {
            val limite = cantidad.coerceIn(0, CAPACIDAD_AUTOBUS_STU)
            if (limite == 0) return emptyMap()
            return (1..limite).associateWith { numeroAsiento ->
                "$prefijoMatricula${1000 + numeroAsiento}"
            }
        }

        fun crearSalidasIniciales(): List<SalidaAutobus> = listOf(
            // Ruta CU-CU2
            SalidaAutobus(
                id = "cu-cu2-1430",
                rutaId = "CU-CU2",
                nombreRuta = "CU-CU2",
                horaSalida = "14:30",
                anden = "Andén 3",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(17, "20201")
            ),
            SalidaAutobus(
                id = "cu-cu2-1500",
                rutaId = "CU-CU2",
                nombreRuta = "CU-CU2",
                horaSalida = "15:00",
                anden = "Andén 3",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(35, "20202")
            ),
            SalidaAutobus(
                id = "cu-cu2-1400",
                rutaId = "CU-CU2",
                nombreRuta = "CU-CU2",
                horaSalida = "14:00",
                anden = "Andén 3",
                estadoBase = EstadoSalida.LLENO,
                asientosOcupadosPorMatricula = generarOcupacionInicial(36, "20203")
            ),
            SalidaAutobus(
                id = "cu-cu2-proximo",
                rutaId = "CU-CU2",
                nombreRuta = "CU-CU2",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU2-CU
            SalidaAutobus(
                id = "cu2-cu-1445",
                rutaId = "CU2-CU",
                nombreRuta = "CU2-CU",
                horaSalida = "14:45",
                anden = "Andén 1",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(12, "20204")
            ),
            SalidaAutobus(
                id = "cu2-cu-proximo",
                rutaId = "CU2-CU",
                nombreRuta = "CU2-CU",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-Amalucan
            SalidaAutobus(
                id = "cu-amalucan-1430",
                rutaId = "CU-Amalucan",
                nombreRuta = "CU-Amalucan",
                horaSalida = "14:30",
                anden = "Andén 3",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(17, "20205")
            ),
            SalidaAutobus(
                id = "cu-amalucan-1500",
                rutaId = "CU-Amalucan",
                nombreRuta = "CU-Amalucan",
                horaSalida = "15:00",
                anden = "Andén 3",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(35, "20206")
            ),
            SalidaAutobus(
                id = "cu-amalucan-1400",
                rutaId = "CU-Amalucan",
                nombreRuta = "CU-Amalucan",
                horaSalida = "14:00",
                anden = "Andén 3",
                estadoBase = EstadoSalida.LLENO,
                asientosOcupadosPorMatricula = generarOcupacionInicial(36, "20207")
            ),
            SalidaAutobus(
                id = "cu-amalucan-proximo",
                rutaId = "CU-Amalucan",
                nombreRuta = "CU-Amalucan",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-CAPU
            SalidaAutobus(
                id = "cu-capu-1515",
                rutaId = "CU-CAPU",
                nombreRuta = "CU-CAPU",
                horaSalida = "15:15",
                anden = "Andén 2",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(9, "20208")
            ),
            SalidaAutobus(
                id = "cu-capu-proximo",
                rutaId = "CU-CAPU",
                nombreRuta = "CU-CAPU",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-Cuautlancingo
            SalidaAutobus(
                id = "cu-cuautlancingo-1530",
                rutaId = "CU-Cuautlancingo",
                nombreRuta = "CU-Cuautlancingo",
                horaSalida = "15:30",
                anden = "Andén 4",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(21, "20209")
            ),
            SalidaAutobus(
                id = "cu-cuautlancingo-proximo",
                rutaId = "CU-Cuautlancingo",
                nombreRuta = "CU-Cuautlancingo",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-Maravillas
            SalidaAutobus(
                id = "cu-maravillas-1545",
                rutaId = "CU-Maravillas",
                nombreRuta = "CU-Maravillas",
                horaSalida = "15:45",
                anden = "Andén 5",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(14, "20210")
            ),
            SalidaAutobus(
                id = "cu-maravillas-proximo",
                rutaId = "CU-Maravillas",
                nombreRuta = "CU-Maravillas",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-San Ramón
            SalidaAutobus(
                id = "cu-san-ramon-1600",
                rutaId = "CU-San Ramón",
                nombreRuta = "CU-San Ramón",
                horaSalida = "16:00",
                anden = "Andén 6",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(24, "20211")
            ),
            SalidaAutobus(
                id = "cu-san-ramon-proximo",
                rutaId = "CU-San Ramón",
                nombreRuta = "CU-San Ramón",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-Héroes
            SalidaAutobus(
                id = "cu-heroes-1615",
                rutaId = "CU-Héroes",
                nombreRuta = "CU-Héroes",
                horaSalida = "16:15",
                anden = "Andén 7",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(6, "20212")
            ),
            SalidaAutobus(
                id = "cu-heroes-proximo",
                rutaId = "CU-Héroes",
                nombreRuta = "CU-Héroes",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            ),

            // Ruta CU-Tlaxcalancingo
            SalidaAutobus(
                id = "cu-tlaxcalancingo-1630",
                rutaId = "CU-Tlaxcalancingo",
                nombreRuta = "CU-Tlaxcalancingo",
                horaSalida = "16:30",
                anden = "Andén 8",
                estadoBase = EstadoSalida.DISPONIBLE,
                asientosOcupadosPorMatricula = generarOcupacionInicial(30, "20213")
            ),
            SalidaAutobus(
                id = "cu-tlaxcalancingo-proximo",
                rutaId = "CU-Tlaxcalancingo",
                nombreRuta = "CU-Tlaxcalancingo",
                horaSalida = "Hora por confirmar",
                anden = "Andén por asignar",
                estadoBase = EstadoSalida.PROXIMO
            )
        )
    }

    private val _salidasFlow = MutableStateFlow(salidasIniciales)
    override val salidasFlow: StateFlow<List<SalidaAutobus>> = _salidasFlow.asStateFlow()

    override fun obtenerRutas(): List<RutaStu> = rutasIniciales.toList()

    override fun obtenerSalidasPorRuta(rutaId: String): List<SalidaAutobus> {
        val rutaLimpia = rutaId.trim()
        return _salidasFlow.value.filter { it.rutaId.equals(rutaLimpia, ignoreCase = true) }
    }

    override fun buscarSalidaPorId(salidaId: String): SalidaAutobus? {
        val idLimpio = salidaId.trim()
        return _salidasFlow.value.firstOrNull { it.id == idLimpio }
    }

    @Synchronized
    override fun confirmarReserva(
        usuario: UsuarioSTU,
        salidaId: String
    ): ResultadoReservaSalida {
        val salidaActual = buscarSalidaPorId(salidaId)
            ?: return ResultadoReservaSalida(
                ok = false,
                mensaje = "No se encontró la salida seleccionada"
            )

        val resultado = GestorSalidas.confirmarSalidaYAsignarAsiento(usuario, salidaActual)
        val salidaActualizada = resultado.salidaActualizada
        if (resultado.ok && salidaActualizada != null) {
            _salidasFlow.value = _salidasFlow.value.map { salida ->
                if (salida.id == salidaActualizada.id) salidaActualizada else salida
            }
        }
        return resultado
    }
}
