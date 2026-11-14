package mx.buap.stu.auth

import mx.buap.stu.data.UsuarioSTU
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Suite Automatizada de las 12 Pruebas Funcionales (PR1 a PR12) para Android (JUnit 4).
 * Módulo de Registro de Nuevo Estudiante STU BUAP (HU-01.1 — Técnicas BICEP y CORRECT, Sprint 2).
 */
@FixMethodOrder(MethodSorters.JVM)
class ValidadorRegistroTest {

    private val padronExistente = listOf(
        UsuarioSTU(
            correo = "Javier.sangines@alumno.buap.mx",
            matricula = "202268046",
            contrasena = "290504",
            pasajesDisponibles = 4
        )
    )

    @Test
    fun `PR1 - Registrar exitosamente a un alumno nuevo con dominio @alumno buap mx y saldo inicial 0`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@alumno.buap.mx",
            "202112345",
            "170301",
            padronExistente
        )
        assertTrue(resultado.ok)
        assertNotNull(resultado.usuario)
        assertEquals(0, resultado.usuario?.pasajesDisponibles)
        assertEquals("ACTIVO", resultado.usuario?.estado)
    }

    @Test
    fun `PR2 - Registrar exitosamente a un alumno nuevo con dominio @correo buap mx`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@correo.buap.mx",
            "202154321",
            "170301",
            padronExistente
        )
        assertTrue(resultado.ok)
        assertNotNull(resultado.usuario)
        assertEquals(0, resultado.usuario?.pasajesDisponibles)
        assertEquals("ACTIVO", resultado.usuario?.estado)
    }

    @Test
    fun `PR3 - Rechazar registro si el correo tiene 0 caracteres antes del arroba`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "@alumno.buap.mx",
            "202112345",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR4 - Rechazar registro si el correo supera 30 caracteres antes del arroba (31 caracteres)`() {
        val correo31 = "fernandohernandez12345678901234@alumno.buap.mx"
        val resultado = ValidadorRegistro.validarRegistro(
            correo31,
            "202112345",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR5 - Rechazar registro si la matricula no tiene exactamente 9 digitos (8 digitos)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@alumno.buap.mx",
            "20211234",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PR6 - Rechazar registro si la contrasena no tiene 6 digitos (5 digitos)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@alumno.buap.mx",
            "202112345",
            "17030",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
    }

    @Test
    fun `PR7 - Rechazar registro con dominio externo no permitido (@gmail com)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@gmail.com",
            "202112345",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR8 - Rechazar registro cuando la matricula contiene letras`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@alumno.buap.mx",
            "20211234A",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PR9 - Rechazar registro cuando la contrasena no es una fecha valida (999999)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "fernando.hernandez@alumno.buap.mx",
            "202112345",
            "999999",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
    }

    @Test
    fun `PR10 - Rechazar registro si la matricula ya esta registrada en el padron`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "nuevo.correo@alumno.buap.mx",
            "202268046",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
        assertEquals("La matrícula ya cuenta con un registro previo", resultado.mensaje)
    }

    @Test
    fun `PR11 - Rechazar registro si el correo institucional ya esta registrado`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "Javier.sangines@alumno.buap.mx",
            "202599999",
            "170301",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
        assertEquals("El correo institucional ya está registrado", resultado.mensaje)
    }

    @Test
    fun `PR12 - Rechazar registro si los tres campos estan vacios`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "",
            "",
            "",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("todos", resultado.campoError)
    }

    @Test
    fun `PR13 - Registrar exitosamente capturando Nombre(s) y Apellidos para reflejarlos en el perfil`() {
        val resultado = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "170301",
            baseDeDatos = padronExistente,
            nombres = "  Fernando Jesús ",
            apellidos = " Hernández   Gorgua "
        )
        assertTrue(resultado.ok)
        assertEquals("Fernando Jesús Hernández Gorgua", resultado.usuario?.nombre)
        assertEquals(0, resultado.usuario?.pasajesDisponibles)
        assertEquals("ACTIVO", resultado.usuario?.estado)
    }

    @Test
    fun `PR14 - Rechazar registro cuando Nombre(s) esta vacio en formulario completo`() {
        val resultado = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "170301",
            baseDeDatos = padronExistente,
            nombres = "",
            apellidos = "Hernández Gorgua"
        )
        assertFalse(resultado.ok)
        assertEquals("nombres", resultado.campoError)
    }

    @Test
    fun `PR15 - Rechazar registro cuando Apellidos esta vacio en formulario completo`() {
        val resultado = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "170301",
            baseDeDatos = padronExistente,
            nombres = "Fernando Jesús",
            apellidos = "   "
        )
        assertFalse(resultado.ok)
        assertEquals("apellidos", resultado.campoError)
    }

    @Test
    fun `PR16 - Rechazar registro cuando Nombre(s) o Apellidos contienen numeros o simbolos`() {
        val resultadoNombreInvalido = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "170301",
            baseDeDatos = padronExistente,
            nombres = "Fernando123",
            apellidos = "Hernández"
        )
        assertFalse(resultadoNombreInvalido.ok)
        assertEquals("nombres", resultadoNombreInvalido.campoError)

        val resultadoApellidoInvalido = ValidadorRegistro.validarRegistro(
            correo = "fernando.hernandez@alumno.buap.mx",
            matricula = "202112345",
            contrasena = "170301",
            baseDeDatos = padronExistente,
            nombres = "Fernando",
            apellidos = "Hernández@99"
        )
        assertFalse(resultadoApellidoInvalido.ok)
        assertEquals("apellidos", resultadoApellidoInvalido.campoError)
    }
}
