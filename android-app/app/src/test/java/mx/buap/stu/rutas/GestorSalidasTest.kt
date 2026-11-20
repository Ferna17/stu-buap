package mx.buap.stu.rutas

import mx.buap.stu.data.RepositorioSalidasMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.saldo.EvaluadorSaldo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Suite Automatizada de las 8 Pruebas Unitarias (PRU1 a PRU8) para Android (JUnit 4).
 * Módulo de Consulta de Rutas, Horarios y Mapa de 36 Asientos STU BUAP (HU-06 — Técnicas BICEP y CORRECT).
 */
@FixMethodOrder(MethodSorters.JVM)
class GestorSalidasTest {

    private fun crearUsuarioConSaldo(
        matricula: String = "202345678",
        pasajes: Int = 10
    ) = UsuarioSTU(
        correo = "estudiante.$matricula@alumno.buap.mx",
        matricula = matricula,
        contrasena = "AccesoStu99",
        nombre = "Estudiante STU",
        pasajesDisponibles = pasajes,
        estado = "ACTIVO"
    )

    private fun crearOcupacionParcial(cantidad: Int): Map<Int, String> =
        (1..cantidad).associateWith { numero -> "2019${10000 + numero}" }

    @Test
    fun `PRU1 - Consultar horarios de una ruta existente con estados DISPONIBLE, PROXIMO y LLENO`() {
        val repositorio = RepositorioSalidasMemoria()
        val rutas = repositorio.obtenerRutas()
        assertEquals(9, rutas.size)

        val salidasAmalucan = repositorio.obtenerSalidasPorRuta("CU-Amalucan")
        val salidasCu2 = repositorio.obtenerSalidasPorRuta("CU-CU2")

        assertTrue(salidasAmalucan.any { it.estadoActual == EstadoSalida.DISPONIBLE })
        assertTrue(
            salidasAmalucan.any {
                it.estadoActual == EstadoSalida.PROXIMO && it.horaSalida == "Hora por confirmar"
            }
        )
        assertTrue(salidasAmalucan.any { it.estadoActual == EstadoSalida.LLENO })

        assertTrue(salidasCu2.any { it.estadoActual == EstadoSalida.DISPONIBLE })
        assertTrue(
            salidasCu2.any {
                it.estadoActual == EstadoSalida.PROXIMO && it.horaSalida == "Hora por confirmar"
            }
        )
    }

    @Test
    fun `PRU2 - Reservar con saldo mayor o igual a 1 asignando y bloqueando el menor asiento libre`() {
        val usuario = crearUsuarioConSaldo(matricula = "202345678", pasajes = 10)
        val salida = SalidaAutobus(
            id = "cu-amalucan-1430",
            rutaId = "CU-Amalucan",
            nombreRuta = "CU-Amalucan",
            horaSalida = "14:30",
            anden = "Andén 3",
            estadoBase = EstadoSalida.DISPONIBLE,
            asientosOcupadosPorMatricula = crearOcupacionParcial(17)
        )

        val resultado = GestorSalidas.confirmarSalidaYAsignarAsiento(usuario, salida)

        assertTrue(resultado.ok)
        assertFalse(resultado.redirigirARecarga)
        assertNotNull(resultado.reserva)
        assertEquals(18, resultado.reserva?.asientoAsignado)
        assertEquals("202345678", resultado.reserva?.matriculaEstudiante)

        val salidaActualizada = resultado.salidaActualizada!!
        assertEquals(18, salidaActualizada.totalOcupados)
        assertEquals(18, salidaActualizada.totalDisponibles)
        assertEquals("202345678", salidaActualizada.asientosOcupadosPorMatricula[18])

        val mapaTitular = salidaActualizada.obtenerMapaAsientos("202345678").toMap()
        assertEquals(EstadoAsiento.MI_ASIENTO, mapaTitular[18])
        assertEquals(EstadoAsiento.DISPONIBLE, mapaTitular[19])
    }

    @Test
    fun `PRU3 - Reservar el ultimo asiento disponible 35 de 36 y cambiar estado a LLENO`() {
        val usuario = crearUsuarioConSaldo(matricula = "202345678", pasajes = 5)
        val salida = SalidaAutobus(
            id = "cu-amalucan-1500",
            rutaId = "CU-Amalucan",
            nombreRuta = "CU-Amalucan",
            horaSalida = "15:00",
            anden = "Andén 3",
            estadoBase = EstadoSalida.DISPONIBLE,
            asientosOcupadosPorMatricula = crearOcupacionParcial(35)
        )

        val resultado = GestorSalidas.confirmarSalidaYAsignarAsiento(usuario, salida)

        assertTrue(resultado.ok)
        assertEquals(36, resultado.reserva?.asientoAsignado)

        val salidaActualizada = resultado.salidaActualizada!!
        assertEquals(36, salidaActualizada.totalOcupados)
        assertEquals(0, salidaActualizada.totalDisponibles)
        assertEquals(EstadoSalida.LLENO, salidaActualizada.estadoActual)
    }

    @Test
    fun `PRU4 - Rechazar reserva en autobus lleno 36 de 36 sin alterar mapa ni contadores`() {
        val usuario = crearUsuarioConSaldo(matricula = "202345678", pasajes = 10)
        val salidaLlena = SalidaAutobus(
            id = "cu-amalucan-1400",
            rutaId = "CU-Amalucan",
            nombreRuta = "CU-Amalucan",
            horaSalida = "14:00",
            anden = "Andén 3",
            estadoBase = EstadoSalida.LLENO,
            asientosOcupadosPorMatricula = crearOcupacionParcial(36)
        )

        val resultado = GestorSalidas.confirmarSalidaYAsignarAsiento(usuario, salidaLlena)

        assertFalse(resultado.ok)
        assertFalse(resultado.redirigirARecarga)
        assertNull(resultado.reserva)
        assertNull(resultado.salidaActualizada)
        assertEquals(36, salidaLlena.totalOcupados)
        assertEquals(0, salidaLlena.totalDisponibles)
        assertNull(salidaLlena.buscarAsientoDeMatricula("202345678"))
    }

    @Test
    fun `PRU5 - Rechazar reserva con 0 pasajes y redirigir a recarga sin bloquear asientos`() {
        val usuarioSinSaldo = crearUsuarioConSaldo(matricula = "202500001", pasajes = 0)
        val repositorio = RepositorioSalidasMemoria()

        val resultado = repositorio.confirmarReserva(usuarioSinSaldo, "cu-amalucan-1430")
        val salidaTrasIntento = repositorio.buscarSalidaPorId("cu-amalucan-1430")!!

        assertFalse(resultado.ok)
        assertTrue(resultado.redirigirARecarga)
        assertEquals(EvaluadorSaldo.MENSAJE_SIN_PASAJES_SALIDA, resultado.mensaje)
        assertNull(resultado.reserva)
        assertEquals(17, salidaTrasIntento.totalOcupados)
        assertEquals(19, salidaTrasIntento.totalDisponibles)
        assertNull(salidaTrasIntento.buscarAsientoDeMatricula("202500001"))
    }

    @Test
    fun `PRU6 - Rechazar seleccion de salida PROXIMO con hora por confirmar`() {
        val usuario = crearUsuarioConSaldo(matricula = "202345678", pasajes = 10)
        val salidaProxima = SalidaAutobus(
            id = "cu-amalucan-proximo",
            rutaId = "CU-Amalucan",
            nombreRuta = "CU-Amalucan",
            horaSalida = "Hora por confirmar",
            anden = "Andén por asignar",
            estadoBase = EstadoSalida.PROXIMO
        )

        val resultado = GestorSalidas.confirmarSalidaYAsignarAsiento(usuario, salidaProxima)

        assertFalse(resultado.ok)
        assertFalse(resultado.redirigirARecarga)
        assertTrue(resultado.mensaje.contains("hora por confirmar", ignoreCase = true))
        assertNull(resultado.reserva)
        assertEquals(0, salidaProxima.totalOcupados)
    }

    @Test
    fun `PRU7 - Evitar doble reserva de la misma matricula en la misma salida`() {
        val repositorio = RepositorioSalidasMemoria()
        val usuario = crearUsuarioConSaldo(matricula = "202345678", pasajes = 10)

        val primerResultado = repositorio.confirmarReserva(usuario, "cu-amalucan-1430")
        assertTrue(primerResultado.ok)
        assertEquals(18, primerResultado.reserva?.asientoAsignado)

        val segundoResultado = repositorio.confirmarReserva(usuario, "cu-amalucan-1430")
        val salidaFinal = repositorio.buscarSalidaPorId("cu-amalucan-1430")!!

        assertFalse(segundoResultado.ok)
        assertFalse(segundoResultado.redirigirARecarga)
        assertEquals(GestorSalidas.MENSAJE_ASIENTO_DUPLICADO, segundoResultado.mensaje)
        assertEquals(18, salidaFinal.totalOcupados)
        assertEquals(18, salidaFinal.buscarAsientoDeMatricula("202345678"))
    }

    @Test
    fun `PRU8 - Garantizar consistencia en tiempo real entre dos usuarios en el repositorio`() {
        val repositorio = RepositorioSalidasMemoria()
        val alumnoA = crearUsuarioConSaldo(matricula = "202345678", pasajes = 10)
        val alumnoB = crearUsuarioConSaldo(matricula = "202311111", pasajes = 4)

        val reservaA = repositorio.confirmarReserva(alumnoA, "cu-amalucan-1430")
        assertTrue(reservaA.ok)
        assertEquals(18, reservaA.reserva?.asientoAsignado)

        val salidaVistaPorB = repositorio.salidasFlow.value.first { it.id == "cu-amalucan-1430" }
        val mapaParaB = salidaVistaPorB.obtenerMapaAsientos(alumnoB.matricula).toMap()
        assertEquals(EstadoAsiento.OCUPADO, mapaParaB[18])
        assertEquals(EstadoAsiento.DISPONIBLE, mapaParaB[19])

        val reservaB = repositorio.confirmarReserva(alumnoB, "cu-amalucan-1430")
        val salidaFinal = repositorio.salidasFlow.value.first { it.id == "cu-amalucan-1430" }

        assertTrue(reservaB.ok)
        assertEquals(19, reservaB.reserva?.asientoAsignado)
        assertEquals(19, salidaFinal.totalOcupados)
        assertEquals(17, salidaFinal.totalDisponibles)
        assertEquals("202345678", salidaFinal.asientosOcupadosPorMatricula[18])
        assertEquals("202311111", salidaFinal.asientosOcupadosPorMatricula[19])
    }
}
