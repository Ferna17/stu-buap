package mx.buap.stu.rutas

import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.saldo.EvaluadorSaldo

/**
 * Lógica de dominio pura para confirmar salidas STU y asignar automáticamente el menor asiento libre (HU-06).
 * En esta historia se valida saldo >= 1 y se bloquea el asiento por matrícula sin descontar aún pasajes (HU-07).
 */
object GestorSalidas {

    const val MENSAJE_SALIDA_PROXIMA =
        "Este autobús es próximo y aún tiene hora por confirmar"
    const val MENSAJE_ASIENTO_DUPLICADO =
        "Ya tienes un asiento asignado en esta salida"
    const val MENSAJE_AUTOBUS_LLENO =
        "El autobús se encuentra lleno (36 de 36 asientos ocupados)"
    const val MENSAJE_MATRICULA_INVALIDA =
        "No se pudo identificar la matrícula del estudiante para asignar un asiento"

    /**
     * Valida saldo, disponibilidad, unicidad por matrícula y cupo de 36 asientos; si todo es válido,
     * asigna y bloquea automáticamente el menor número de asiento disponible en `1..36`.
     */
    fun confirmarSalidaYAsignarAsiento(
        usuario: UsuarioSTU,
        salida: SalidaAutobus
    ): ResultadoReservaSalida {
        val validacionSaldo = EvaluadorSaldo.intentarSeleccionarSalida(usuario)
        if (!validacionSaldo.permitido) {
            return ResultadoReservaSalida(
                ok = false,
                mensaje = validacionSaldo.mensaje,
                redirigirARecarga = true
            )
        }

        val matriculaLimpia = usuario.matricula.trim()
        if (matriculaLimpia.isEmpty()) {
            return ResultadoReservaSalida(
                ok = false,
                mensaje = MENSAJE_MATRICULA_INVALIDA
            )
        }

        if (salida.estadoActual == EstadoSalida.PROXIMO) {
            return ResultadoReservaSalida(
                ok = false,
                mensaje = MENSAJE_SALIDA_PROXIMA
            )
        }

        if (salida.buscarAsientoDeMatricula(matriculaLimpia) != null) {
            return ResultadoReservaSalida(
                ok = false,
                mensaje = MENSAJE_ASIENTO_DUPLICADO
            )
        }

        if (salida.estadoActual == EstadoSalida.LLENO) {
            return ResultadoReservaSalida(
                ok = false,
                mensaje = MENSAJE_AUTOBUS_LLENO
            )
        }

        val asientoLibre = (1..CAPACIDAD_AUTOBUS_STU)
            .firstOrNull { it !in salida.asientosOcupadosPorMatricula.keys }
            ?: return ResultadoReservaSalida(
                ok = false,
                mensaje = MENSAJE_AUTOBUS_LLENO
            )

        val mapaActualizado = salida.asientosOcupadosPorMatricula
            .toMutableMap()
            .apply { put(asientoLibre, matriculaLimpia) }
            .toMap()

        val salidaActualizada = salida.copy(
            asientosOcupadosPorMatricula = mapaActualizado
        )

        val reserva = ReservaSalida(
            salidaId = salida.id,
            nombreRuta = salida.nombreRuta,
            horaSalida = salida.horaSalida,
            anden = salida.anden,
            asientoAsignado = asientoLibre,
            matriculaEstudiante = matriculaLimpia
        )

        return ResultadoReservaSalida(
            ok = true,
            mensaje = "Salida confirmada. Tu asiento asignado es el $asientoLibre de $CAPACIDAD_AUTOBUS_STU.",
            redirigirARecarga = false,
            reserva = reserva,
            salidaActualizada = salidaActualizada
        )
    }
}
