package mx.buap.stu.saldo

import mx.buap.stu.auth.ValidadorRegistro
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.viewmodels.InicioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Suite Automatizada de las 6 Pruebas Unitarias (PS1 a PS6) para Android (JUnit 4).
 * Módulo de Consulta de Saldo y Pantalla Principal STU BUAP (HU-02 — Técnicas BICEP y CORRECT).
 */
@FixMethodOrder(MethodSorters.JVM)
class EvaluadorSaldoTest {

    @Test
    fun `PS1 - Mostrar saldo positivo y habilitar seleccion de salida con 10 pasajes`() {
        val usuario = UsuarioSTU(
            correo = "Javier.sangines@alumno.buap.mx",
            matricula = "202268046",
            contrasena = "StuBuap2026",
            nombre = "Javier Sanginés",
            pasajesDisponibles = 10,
            estado = "ACTIVO"
        )

        val estado = EvaluadorSaldo.evaluar(usuario)
        assertEquals(10, estado.pasajesDisponibles)
        assertEquals("10 viajes disponibles", estado.etiquetaSaldo)
        assertTrue(estado.puedeReservarViaje)
        assertFalse(estado.requiereRecarga)

        val seleccion = EvaluadorSaldo.intentarSeleccionarSalida(usuario)
        assertTrue(seleccion.permitido)
        assertFalse(seleccion.redirigirARecarga)
    }

    @Test
    fun `PS2 - Validar limite minimo valido de 1 pasaje en singular y permitir seleccion de salida`() {
        val usuario = UsuarioSTU(
            correo = "Javier.sangines@alumno.buap.mx",
            matricula = "202268046",
            contrasena = "StuBuap2026",
            pasajesDisponibles = 1
        )

        val estado = EvaluadorSaldo.evaluar(usuario)
        assertEquals(1, estado.pasajesDisponibles)
        assertEquals("1 viaje disponible", estado.etiquetaSaldo)
        assertTrue(estado.puedeReservarViaje)
        assertFalse(estado.requiereRecarga)

        val seleccion = EvaluadorSaldo.intentarSeleccionarSalida(usuario)
        assertTrue(seleccion.permitido)
        assertFalse(seleccion.redirigirARecarga)
    }

    @Test
    fun `PS3 - Bloquear seleccion de salida y redirigir a recarga con 0 pasajes`() {
        val usuario = UsuarioSTU(
            correo = "nuevo.alumno@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "Alumno2026",
            pasajesDisponibles = 0
        )

        val estado = EvaluadorSaldo.evaluar(usuario)
        assertEquals(0, estado.pasajesDisponibles)
        assertEquals("0 viajes disponibles", estado.etiquetaSaldo)
        assertFalse(estado.puedeReservarViaje)
        assertTrue(estado.requiereRecarga)
        assertEquals(
            "Sin pasajes disponibles. Realiza una recarga para viajar",
            estado.mensajeEstado
        )

        val seleccion = EvaluadorSaldo.intentarSeleccionarSalida(usuario)
        assertFalse(seleccion.permitido)
        assertTrue(seleccion.redirigirARecarga)
        assertEquals(
            "Necesitas al menos un pasaje disponible para seleccionar una salida",
            seleccion.mensaje
        )
    }

    @Test
    fun `PS4 - Sanitizar saldo negativo corrupto a 0 y redirigir a recarga al seleccionar salida`() {
        val usuarioCorrupto = UsuarioSTU(
            correo = "anomalo@alumno.buap.mx",
            matricula = "202299999",
            contrasena = "StuBuap2026",
            pasajesDisponibles = -3
        )

        val estado = EvaluadorSaldo.evaluar(usuarioCorrupto)
        assertEquals(0, estado.pasajesDisponibles)
        assertEquals("0 viajes disponibles", estado.etiquetaSaldo)
        assertFalse(estado.puedeReservarViaje)
        assertTrue(estado.requiereRecarga)
        assertEquals(
            "Sin pasajes disponibles. Realiza una recarga para viajar",
            estado.mensajeEstado
        )

        val seleccion = EvaluadorSaldo.intentarSeleccionarSalida(usuarioCorrupto)
        assertFalse(seleccion.permitido)
        assertTrue(seleccion.redirigirARecarga)
        assertEquals(
            "Necesitas al menos un pasaje disponible para seleccionar una salida",
            seleccion.mensaje
        )
    }

    @Test
    fun `PS5 - Integracion entre ValidadorRegistro y EvaluadorSaldo con saldo inicial 0 y redireccion a recarga`() {
        val resultadoRegistro = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "Fernando2026",
            nombres = "Fernando Jesús",
            apellidos = "Hernández Gorgua"
        )
        assertTrue(resultadoRegistro.ok)
        assertNotNull(resultadoRegistro.usuario)

        val usuarioRegistrado = resultadoRegistro.usuario!!
        val estado = EvaluadorSaldo.evaluar(usuarioRegistrado)
        assertEquals(0, estado.pasajesDisponibles)
        assertEquals("0 viajes disponibles", estado.etiquetaSaldo)
        assertFalse(estado.puedeReservarViaje)
        assertTrue(estado.requiereRecarga)

        val seleccion = EvaluadorSaldo.intentarSeleccionarSalida(usuarioRegistrado)
        assertFalse(seleccion.permitido)
        assertTrue(seleccion.redirigirARecarga)
        assertEquals(
            "Necesitas al menos un pasaje disponible para seleccionar una salida",
            seleccion.mensaje
        )
    }

    @Test
    fun `PS6 - Consultar saldo actualizado desde InicioViewModel y repositorio`() {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = InicioViewModel(repo)

        val estadoConSaldo = viewModel.recargarSaldoDesdeRepositorio("202268046")
        assertNotNull(viewModel.usuarioActual)
        assertEquals("202268046", viewModel.usuarioActual?.matricula)
        assertNotNull(estadoConSaldo)
        assertEquals(10, viewModel.estadoSaldo?.pasajesDisponibles)
        assertEquals("10 viajes disponibles", viewModel.estadoSaldo?.etiquetaSaldo)
        assertTrue(viewModel.estadoSaldo?.puedeReservarViaje == true)
        assertFalse(viewModel.estadoSaldo?.requiereRecarga == true)

        val seleccionPermitida = viewModel.intentarSeleccionarSalida()
        assertNotNull(seleccionPermitida)
        assertTrue(seleccionPermitida?.permitido == true)
        assertFalse(seleccionPermitida?.redirigirARecarga == true)
        assertFalse(viewModel.mostrarAvisoSinPasajes)

        repo.registrarUsuario(
            UsuarioSTU(
                correo = "sin.saldo@alumno.buap.mx",
                matricula = "202500001",
                contrasena = "Alumno2026",
                nombre = "Alumno Sin Saldo",
                pasajesDisponibles = 0
            )
        )
        val estadoSinSaldo = viewModel.recargarSaldoDesdeRepositorio("202500001")
        assertNotNull(estadoSinSaldo)
        assertEquals(0, viewModel.estadoSaldo?.pasajesDisponibles)
        assertEquals("0 viajes disponibles", viewModel.estadoSaldo?.etiquetaSaldo)

        val seleccionBloqueada = viewModel.intentarSeleccionarSalida()
        assertNotNull(seleccionBloqueada)
        assertFalse(seleccionBloqueada?.permitido == true)
        assertTrue(seleccionBloqueada?.redirigirARecarga == true)
        assertEquals(
            "Necesitas al menos un pasaje disponible para seleccionar una salida",
            seleccionBloqueada?.mensaje
        )
        assertTrue(viewModel.mostrarAvisoSinPasajes)
    }
}
