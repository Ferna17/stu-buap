package mx.buap.stu.auth

import mx.buap.stu.data.UsuarioSTU
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Suite Automatizada de las 19 Pruebas Funcionales (PF1 a PF19) para Android (JUnit 4).
 * Módulo de Inicio de Sesión STU BUAP (HU-01 — Técnicas BICEP y CORRECT).
 */
@FixMethodOrder(MethodSorters.JVM)
class ValidadorLoginTest {

    private val padronUsuarios = listOf(
        UsuarioSTU("mateo.morales@alumno.buap.mx", "202345678", "AccesoStu99"),
        UsuarioSTU("mateo.morales@correo.buap.mx", "202345678", "AccesoStu99")
    )

    @Test
    fun `PF1 - Validar inicio de sesion con credenciales validas (@alumno buap mx)`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertTrue(resultado.ok)
    }

    @Test
    fun `PF2 - Validar inicio de sesion con credenciales validas (@correo buap mx)`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@correo.buap.mx",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertTrue(resultado.ok)
    }

    @Test
    fun `PF3 - Rechazar si el email tiene 0 caracteres antes del arroba`() {
        val resultado = ValidadorLogin.validarLogin(
            "@alumno.buap.mx",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PF4 - Aceptar longitud minima de 1 caracter antes del arroba`() {
        val padronMinimo = listOf(
            UsuarioSTU("m@alumno.buap.mx", "202345678", "AccesoStu99")
        )
        val resultado = ValidadorLogin.validarLogin(
            "m@alumno.buap.mx",
            "202345678",
            "AccesoStu99",
            padronMinimo
        )
        assertTrue(resultado.ok)
    }

    @Test
    fun `PF5 - Aceptar longitud maxima de 30 caracteres antes del arroba`() {
        val correo30 = "mateomorales123456789012345678@alumno.buap.mx"
        val padronMaximo = listOf(
            UsuarioSTU(correo30, "202345678", "AccesoStu99")
        )
        val resultado = ValidadorLogin.validarLogin(
            correo30,
            "202345678",
            "AccesoStu99",
            padronMaximo
        )
        assertTrue(resultado.ok)
    }

    @Test
    fun `PF6 - Rechazar si el email supera 30 caracteres antes del arroba`() {
        val correo31 = "mateomorales1234567890123456789@alumno.buap.mx"
        val resultado = ValidadorLogin.validarLogin(
            correo31,
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PF7 - Rechazar matricula con 8 digitos`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "20234567",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PF8 - Rechazar matricula con 10 digitos`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "2023456780",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PF9 - Rechazar contrasena con menos de 8 caracteres (7 caracteres)`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202345678",
            "Clave99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultado.mensaje)
    }

    @Test
    fun `PF10 - Rechazar contrasena con mas de 20 caracteres (21 caracteres)`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202345678",
            "AccesoStu99Contrasena",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultado.mensaje)
    }

    @Test
    fun `PF11 - Rechazar dominio externo (@gmail com)`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@gmail.com",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PF12 - Rechazar correo sin arroba`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.moralesalumno.buap.mx",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("email", resultado.campoError)
    }

    @Test
    fun `PF13 - Rechazar matricula con letras`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "20234567A",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("matricula", resultado.campoError)
    }

    @Test
    fun `PF14 - Rechazar contrasena que contenga espacios en blanco`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202345678",
            "Acceso Stu 99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
        assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resultado.mensaje)
    }

    @Test
    fun `PF15 - Rechazar contrasena incorrecta`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202345678",
            "ClaveErronea99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("password", resultado.campoError)
        assertEquals("Contraseña incorrecta", resultado.mensaje)
    }

    @Test
    fun `PF16 - Rechazar matricula incorrecta`() {
        val resultado = ValidadorLogin.validarLogin(
            "mateo.morales@alumno.buap.mx",
            "202399999",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("La matrícula no coincide", resultado.mensaje)
    }

    @Test
    fun `PF17 - Rechazar correo incorrecto`() {
        val resultado = ValidadorLogin.validarLogin(
            "otro.nombre@alumno.buap.mx",
            "202345678",
            "AccesoStu99",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("El correo no coincide", resultado.mensaje)
    }

    @Test
    fun `PF18 - Indicar falta de registro previo si el alumno no existe`() {
        val resultado = ValidadorLogin.validarLogin(
            "nuevo.alumno@alumno.buap.mx",
            "202512345",
            "Alumno2026",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("Sin registro previo", resultado.mensaje)
        assertTrue(resultado.puedeRegistrarse)
    }

    @Test
    fun `PF19 - Rechazar si los tres campos estan vacios`() {
        val resultado = ValidadorLogin.validarLogin(
            "",
            "",
            "",
            padronUsuarios
        )
        assertFalse(resultado.ok)
        assertEquals("todos", resultado.campoError)
    }
}
