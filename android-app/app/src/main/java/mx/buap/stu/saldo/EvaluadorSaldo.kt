package mx.buap.stu.saldo

import mx.buap.stu.data.UsuarioSTU

/**
 * Estado inmutable resultante de evaluar el saldo de pasajes STU de un estudiante (HU-02).
 */
data class EstadoSaldoStu(
    val pasajesDisponibles: Int,
    val etiquetaSaldo: String,
    val puedeReservarViaje: Boolean,
    val requiereRecarga: Boolean,
    val mensajeEstado: String
)

/**
 * Resultado de intentar consultar rutas y seleccionar una salida según el saldo disponible (HU-02 / HU-06).
 */
data class ResultadoSeleccionSalida(
    val permitido: Boolean,
    val redirigirARecarga: Boolean,
    val mensaje: String
)

/**
 * Lógica de dominio pura para la evaluación del saldo de pasajes STU y control de acceso a salidas (HU-02).
 */
object EvaluadorSaldo {

    const val MENSAJE_SIN_PASAJES_ESTADO =
        "Sin pasajes disponibles. Realiza una recarga para viajar"
    const val MENSAJE_CON_PASAJES_ESTADO =
        "Saldo disponible para consultar rutas y reservar viajes"
    const val MENSAJE_SIN_PASAJES_SALIDA =
        "Necesitas al menos un pasaje disponible para seleccionar una salida"
    const val MENSAJE_SALIDA_PERMITIDA =
        "Puedes consultar rutas y seleccionar una salida disponible"

    /**
     * Evalúa el saldo de pasajes de [usuario], sanitizando valores negativos a 0 sin lanzar excepciones.
     */
    fun evaluar(usuario: UsuarioSTU): EstadoSaldoStu {
        val pasajes = usuario.pasajesDisponibles.coerceAtLeast(0)
        val etiqueta = if (pasajes == 1) "1 viaje disponible" else "$pasajes viajes disponibles"
        val puedeReservar = pasajes >= 1
        val requiereRecarga = pasajes == 0
        val mensajeEstado = if (requiereRecarga) {
            MENSAJE_SIN_PASAJES_ESTADO
        } else {
            MENSAJE_CON_PASAJES_ESTADO
        }

        return EstadoSaldoStu(
            pasajesDisponibles = pasajes,
            etiquetaSaldo = etiqueta,
            puedeReservarViaje = puedeReservar,
            requiereRecarga = requiereRecarga,
            mensajeEstado = mensajeEstado
        )
    }

    /**
     * Determina si el estudiante puede seleccionar una salida o debe ser redirigido a recarga de pasajes.
     * En HU-06 la consulta de rutas y horarios estará disponible aun con saldo 0, validándose este método
     * específicamente al intentar seleccionar una salida.
     */
    fun intentarSeleccionarSalida(usuario: UsuarioSTU): ResultadoSeleccionSalida {
        val estado = evaluar(usuario)
        return if (estado.puedeReservarViaje) {
            ResultadoSeleccionSalida(
                permitido = true,
                redirigirARecarga = false,
                mensaje = MENSAJE_SALIDA_PERMITIDA
            )
        } else {
            ResultadoSeleccionSalida(
                permitido = false,
                redirigirARecarga = true,
                mensaje = MENSAJE_SIN_PASAJES_SALIDA
            )
        }
    }
}
