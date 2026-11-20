package mx.buap.stu.rutas

/**
 * Capacidad oficial de asientos por autobús del Sistema de Transporte Universitario (STU BUAP).
 */
const val CAPACIDAD_AUTOBUS_STU = 36

/**
 * Estados posibles de una salida de autobús STU.
 */
enum class EstadoSalida {
    DISPONIBLE,
    PROXIMO,
    LLENO
}

/**
 * Estados visuales posibles de cada asiento en el mapa 2+2 de 36 lugares.
 */
enum class EstadoAsiento {
    DISPONIBLE,
    OCUPADO,
    MI_ASIENTO
}

/**
 * Ruta oficial del catálogo STU BUAP.
 */
data class RutaStu(
    val id: String,
    val nombre: String,
    val origen: String,
    val destino: String
)

/**
 * Representación inmutable de una salida programada de autobús STU y la ocupación de sus 36 asientos.
 */
data class SalidaAutobus(
    val id: String,
    val rutaId: String,
    val nombreRuta: String,
    val horaSalida: String,
    val anden: String,
    val estadoBase: EstadoSalida,
    val asientosOcupadosPorMatricula: Map<Int, String> = emptyMap()
) {
    val totalOcupados: Int
        get() = asientosOcupadosPorMatricula.keys
            .count { it in 1..CAPACIDAD_AUTOBUS_STU }
            .coerceIn(0, CAPACIDAD_AUTOBUS_STU)

    val totalDisponibles: Int
        get() = (CAPACIDAD_AUTOBUS_STU - totalOcupados).coerceAtLeast(0)

    val estadoActual: EstadoSalida
        get() = when {
            estadoBase == EstadoSalida.PROXIMO -> EstadoSalida.PROXIMO
            estadoBase == EstadoSalida.LLENO || totalOcupados >= CAPACIDAD_AUTOBUS_STU -> EstadoSalida.LLENO
            else -> EstadoSalida.DISPONIBLE
        }

    /**
     * Genera la lista ordenada del asiento 1 al 36 indicando su estado para la matrícula consultante.
     */
    fun obtenerMapaAsientos(matriculaActual: String?): List<Pair<Int, EstadoAsiento>> {
        val matriculaLimpia = matriculaActual?.trim()?.takeIf { it.isNotEmpty() }
        return (1..CAPACIDAD_AUTOBUS_STU).map { numeroAsiento ->
            val titular = asientosOcupadosPorMatricula[numeroAsiento]?.trim()
            val estado = when {
                titular == null -> EstadoAsiento.DISPONIBLE
                matriculaLimpia != null && titular.equals(matriculaLimpia, ignoreCase = true) ->
                    EstadoAsiento.MI_ASIENTO
                else -> EstadoAsiento.OCUPADO
            }
            numeroAsiento to estado
        }
    }

    /**
     * Busca el número de asiento bloqueado para [matricula] en esta salida, o null si aún no tiene uno.
     */
    fun buscarAsientoDeMatricula(matricula: String): Int? {
        val matriculaLimpia = matricula.trim()
        if (matriculaLimpia.isEmpty()) return null
        return asientosOcupadosPorMatricula.entries
            .firstOrNull { (_, titular) -> titular.trim().equals(matriculaLimpia, ignoreCase = true) }
            ?.key
    }
}

/**
 * Datos de confirmación cuando un estudiante obtiene y bloquea automáticamente un asiento en una salida.
 */
data class ReservaSalida(
    val salidaId: String,
    val nombreRuta: String,
    val horaSalida: String,
    val anden: String,
    val asientoAsignado: Int,
    val matriculaEstudiante: String
)

/**
 * Resultado de intentar confirmar una salida y asignar automáticamente el menor asiento disponible.
 */
data class ResultadoReservaSalida(
    val ok: Boolean,
    val mensaje: String,
    val redirigirARecarga: Boolean = false,
    val reserva: ReservaSalida? = null,
    val salidaActualizada: SalidaAutobus? = null
)
