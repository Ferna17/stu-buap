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
 * Suite Automatizada de las 16 Pruebas Funcionales (PR1 a PR16) para Android (JUnit 4).
 * Módulo de Registro de Nuevo Estudiante STU BUAP (HU-01.1 — Técnicas BICEP y CORRECT).
 */
@FixMethodOrder(MethodSorters.JVM)
class ValidadorRegistroTest {

    private val padronExistente = listOf(
        UsuarioSTU(
            correo = "mateo.morales@alumno.buap.mx",
            matricula = "202345678",
            contrasena = "AccesoStu99",
            pasajesDisponibles = 4
        )
    )

    @Test
    fun `PR1 - Registrar exitosamente a un alumno nuevo con dominio @alumno buap mx y saldo inicial 0`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "202456789",
            "ValeriaStu88",
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
            "diego.perez@correo.buap.mx",
            "202154321",
            "Diego2026",
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
            "202456789",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR4 - Rechazar registro si el correo supera 30 caracteres antes del arroba (31 caracteres)`() {
        val correo31 = "valeriasotoparedes1234567890123@alumno.buap.mx"
        val resultado = ValidadorRegistro.validarRegistro(
            correo31,
            "202456789",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR5 - Rechazar registro si la matricula no tiene exactamente 9 digitos (8 digitos)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "20245678",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PR6 - Rechazar registro si la contrasena tiene menos de 8 caracteres (7 caracteres)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "202456789",
            "Vale123",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultado.mensaje)
    }

    @Test
    fun `PR7 - Rechazar registro con dominio externo no permitido (@gmail com)`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "valeria.soto@gmail.com",
            "202456789",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PR8 - Rechazar registro cuando la matricula contiene letras`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "20245678A",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PR9 - Rechazar registro si la contrasena supera 20 caracteres o contiene espacios`() {
        val resultadoExceso = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "202456789",
            "ValeriaStu88ClaveLarg",
            padronExistente
        )
        assertFalse(resultadoExceso.ok)
        assertEquals("password", resultadoExceso.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultadoExceso.mensaje)

        val resultadoEspacios = ValidadorRegistro.validarRegistro(
            "valeria.soto@alumno.buap.mx",
            "202456789",
            "Valeria 2026",
            padronExistente
        )
        assertFalse(resultadoEspacios.ok)
        assertEquals("password", resultadoEspacios.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultadoEspacios.mensaje)
    }

    @Test
    fun `PR10 - Rechazar registro si la matricula ya esta registrada en el padron`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "nuevo.correo@alumno.buap.mx",
            "202345678",
            "ValeriaStu88",
            padronExistente
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
        assertEquals("La matrícula ya cuenta con un registro previo", resultado.mensaje)
    }

    @Test
    fun `PR11 - Rechazar registro si el correo institucional ya esta registrado`() {
        val resultado = ValidadorRegistro.validarRegistro(
            "mateo.morales@alumno.buap.mx",
            "202599999",
            "ValeriaStu88",
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
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202456789",
            contrasena = "ValeriaStu88",
            baseDeDatos = padronExistente,
            nombres = "  Valeria ",
            apellidos = " Soto   Paredes "
        )
        assertTrue(resultado.ok)
        assertEquals("Valeria Soto Paredes", resultado.usuario?.nombre)
        assertEquals(0, resultado.usuario?.pasajesDisponibles)
        assertEquals("ACTIVO", resultado.usuario?.estado)
    }

    @Test
    fun `PR14 - Rechazar registro cuando Nombre(s) esta vacio en formulario completo`() {
        val resultado = ValidadorRegistro.validarRegistro(
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202456789",
            contrasena = "ValeriaStu88",
            baseDeDatos = padronExistente,
            nombres = "",
            apellidos = "Soto Paredes"
        )
        assertFalse(resultado.ok)
        assertEquals("nombres", resultado.campoError)
    }

    @Test
    fun `PR15 - Rechazar registro cuando Apellidos esta vacio en formulario completo`() {
        val resultado = ValidadorRegistro.validarRegistro(
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202456789",
            contrasena = "ValeriaStu88",
            baseDeDatos = padronExistente,
            nombres = "Valeria",
            apellidos = "   "
        )
        assertFalse(resultado.ok)
        assertEquals("apellidos", resultado.campoError)
    }

    @Test
    fun `PR16 - Rechazar registro cuando Nombre(s) o Apellidos contienen numeros o simbolos`() {
        val resultadoNombreInvalido = ValidadorRegistro.validarRegistro(
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202456789",
            contrasena = "ValeriaStu88",
            baseDeDatos = padronExistente,
            nombres = "Valeria123",
            apellidos = "Soto"
        )
        assertFalse(resultadoNombreInvalido.ok)
        assertEquals("nombres", resultadoNombreInvalido.campoError)

        val resultadoApellidoInvalido = ValidadorRegistro.validarRegistro(
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202456789",
            contrasena = "ValeriaStu88",
            baseDeDatos = padronExistente,
            nombres = "Valeria",
            apellidos = "Soto@99"
        )
        assertFalse(resultadoApellidoInvalido.ok)
        assertEquals("apellidos", resultadoApellidoInvalido.campoError)
    }
}
