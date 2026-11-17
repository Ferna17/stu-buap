package mx.buap.stu.auth

import mx.buap.stu.data.UsuarioSTU
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * Suite de 21 Pruebas Adversariales (Robustez y Seguridad) para Android (JUnit 4).
 * Homologada con tests/auth/adversarialValidator.test.js de la rama 'tests'.
 */
@FixMethodOrder(MethodSorters.JVM)
class ValidadorAdversarialTest {

    private val padronBase = listOf(
        UsuarioSTU("Javier.sangines@alumno.buap.mx", "202268046", "StuBuap2026", "Javier Sanginés", 5, "ACTIVO"),
        UsuarioSTU("maria.gonzalez@correo.buap.mx", "202112345", "Maria2026!", "María González", 2, "ACTIVO")
    )

    // --- Vector 1: Fronteras de Longitud y Contraseñas con Espacios (8 a 20 caracteres) ---

    @Test
    fun `ADV-PWD-1 - Aceptar longitud minima exacta de 8 caracteres sin espacios en login y registro`() {
        val padronMinimo = listOf(
            UsuarioSTU("minimo@alumno.buap.mx", "202412345", "Stu2026!", pasajesDisponibles = 0)
        )
        val resLogin = ValidadorLogin.validarLogin("minimo@alumno.buap.mx", "202412345", "Stu2026!", padronMinimo)
        assertTrue(resLogin.ok)

        val resReg = ValidadorRegistro.validarRegistro("nuevo.min@alumno.buap.mx", "202499999", "Stu2026!", padronMinimo)
        assertTrue(resReg.ok)
    }

    @Test
    fun `ADV-PWD-2 - Aceptar longitud maxima exacta de 20 caracteres sin espacios en login y registro`() {
        val clave20 = "StuBuap2026Seguro123"
        val padronMax = listOf(
            UsuarioSTU("maximo@alumno.buap.mx", "202412345", clave20, pasajesDisponibles = 0)
        )
        val resLogin = ValidadorLogin.validarLogin("maximo@alumno.buap.mx", "202412345", clave20, padronMax)
        assertTrue(resLogin.ok)

        val resReg = ValidadorRegistro.validarRegistro("nuevo.max@alumno.buap.mx", "202499999", clave20, padronMax)
        assertTrue(resReg.ok)
    }

    @Test
    fun `ADV-PWD-3 - Rechazar 7 caracteres y 21 caracteres en login y registro`() {
        for (clave in listOf("Stu2026", "StuBuap2026Contrasena")) {
            val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", clave, padronBase)
            assertFalse(resLogin.ok)
            assertEquals("password", resLogin.campoError)
            assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resLogin.mensaje)

            val resReg = ValidadorRegistro.validarRegistro("nuevo.alumno@alumno.buap.mx", "202511111", clave, padronBase)
            assertFalse(resReg.ok)
            assertEquals("password", resReg.campoError)
            assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resReg.mensaje)
        }
    }

    @Test
    fun `ADV-PWD-4 - Rechazar espacios iniciales, finales o internos en contrasena en login y registro`() {
        for (claveConEspacios in listOf(" StuBuap2026", "StuBuap2026 ", "Stu Buap 2026")) {
            val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", claveConEspacios, padronBase)
            assertFalse(resLogin.ok)
            assertEquals("password", resLogin.campoError)
            assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resLogin.mensaje)

            val resReg = ValidadorRegistro.validarRegistro("nuevo.alumno@alumno.buap.mx", "202511111", claveConEspacios, padronBase)
            assertFalse(resReg.ok)
            assertEquals("password", resReg.campoError)
            assertEquals(ValidadorCredenciales.MENSAJE_PASSWORD_INVALIDA, resReg.mensaje)
        }
    }

    @Test
    fun `ADV-PWD-5 - Rechazar tabuladores o saltos de linea inyectados en contrasena`() {
        for (claveInsegura in listOf("StuBuap\t2026", "StuBuap\n2026")) {
            val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", claveInsegura, padronBase)
            assertFalse(resLogin.ok)
            assertEquals("password", resLogin.campoError)

            val resReg = ValidadorRegistro.validarRegistro("nuevo.alumno@alumno.buap.mx", "202511111", claveInsegura, padronBase)
            assertFalse(resReg.ok)
            assertEquals("password", resReg.campoError)
        }
    }

    // --- Vector 2: Entradas Malformadas, Desbordamiento (DoS) y Resiliencia de Tipos ---

    @Test
    fun `ADV-TYP-1 - Rechazar valores puramente numericos o booleanos en correo sin excepciones`() {
        val resLogin = ValidadorLogin.validarLogin("12345", "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("true", "202511111", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
    }

    @Test
    fun `ADV-TYP-2 - Rechazar formatos serializados o multiples arrobas en correo`() {
        val resLogin = ValidadorLogin.validarLogin("a@b@alumno.buap.mx", "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("{\"email\":\"a\"}@alumno.buap.mx", "202511111", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
    }

    @Test
    fun `ADV-TYP-3 - Resistir cadenas gigantes (DoS 10000 chars) sin bloqueos ni excepciones`() {
        val emailGigante = "a".repeat(10000) + "@alumno.buap.mx"
        val resEmail = ValidadorLogin.validarLogin(emailGigante, "202268046", "StuBuap2026", padronBase)
        assertFalse(resEmail.ok)
        assertEquals("email", resEmail.campoError)

        val resMat = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "2".repeat(10000), "Fernando2026", padronBase)
        assertFalse(resMat.ok)
        assertEquals("matricula", resMat.campoError)

        val resPass = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202511111", "X".repeat(10000), padronBase)
        assertFalse(resPass.ok)
        assertEquals("password", resPass.campoError)
    }

    @Test
    fun `ADV-TYP-4 - Rechazar campos compuestos unicamente por espacios o tabuladores`() {
        val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", "   \t  ", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("todos", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202511111", "        ", padronBase)
        assertFalse(resReg.ok)
        assertEquals("todos", resReg.campoError)
    }

    @Test
    fun `ADV-TYP-5 - Soportar baseDeDatos vacia sin lanzar excepciones en login y registro`() {
        val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", "StuBuap2026", emptyList())
        assertFalse(resLogin.ok)
        assertTrue(resLogin.puedeRegistrarse)

        val resReg = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202511111", "Fernando2026", emptyList())
        assertTrue(resReg.ok)
    }

    // --- Vector 3: Evasión por Espacios en Blanco (Whitespace Smuggling & Bypass de Duplicados) ---

    @Test
    fun `ADV-WHT-1 - Espacios iniciales o finales NO deben evadir verificacion de duplicidad en registro`() {
        val resReg = ValidadorRegistro.validarRegistro("  Javier.sangines@alumno.buap.mx  ", "202599999", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
        assertEquals("El correo institucional ya está registrado", resReg.mensaje)
    }

    @Test
    fun `ADV-WHT-2 - Matricula con espacios alrededor debe ser sanitizada y no evadir duplicidad en registro`() {
        val resReg = ValidadorRegistro.validarRegistro("alumno.nuevo@alumno.buap.mx", "  202268046  ", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("matricula", resReg.campoError)
        assertEquals("La matrícula ya cuenta con un registro previo", resReg.mensaje)
    }

    @Test
    fun `ADV-WHT-3 - Login con espacios accidentales en correo y matricula debe autenticar con exito`() {
        val res = ValidadorLogin.validarLogin("  Javier.sangines@alumno.buap.mx  ", "  202268046  ", "StuBuap2026", padronBase)
        assertTrue(res.ok)
    }

    @Test
    fun `ADV-WHT-4 - Rechazar espacios internos en el nombre de usuario del correo`() {
        val resLogin = ValidadorLogin.validarLogin("javier sangines@alumno.buap.mx", "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("javier sangines@alumno.buap.mx", "202511111", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
    }

    // --- Vector 4: Inyecciones de Código y Payloads Maliciosos ---

    @Test
    fun `ADV-INJ-1 - Rechazar inyeccion XSS en email y nombre`() {
        val xssEmail = "<script>alert(1)</script>@alumno.buap.mx"
        val resLogin = ValidadorLogin.validarLogin(xssEmail, "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro(
            "nuevo@alumno.buap.mx",
            "202511111",
            "Fernando2026",
            padronBase,
            nombres = "<script>alert(1)</script>",
            apellidos = "Pérez"
        )
        assertFalse(resReg.ok)
        assertEquals("nombres", resReg.campoError)
    }

    @Test
    fun `ADV-INJ-2 - Rechazar inyeccion SQL (' OR 1=1 --) en email`() {
        val sqliEmail = "' OR 1=1 --@alumno.buap.mx"
        val resLogin = ValidadorLogin.validarLogin(sqliEmail, "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro(sqliEmail, "202511111", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
    }

    @Test
    fun `ADV-INJ-3 - Rechazar Path Traversal en email`() {
        val pathEmail = "../../etc/passwd@alumno.buap.mx"
        val resLogin = ValidadorLogin.validarLogin(pathEmail, "202268046", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("email", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro(pathEmail, "202511111", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("email", resReg.campoError)
    }

    @Test
    fun `ADV-INJ-4 - Rechazar inyeccion de Null Byte en email y contrasena`() {
        val resEmail = ValidadorLogin.validarLogin("admin\u0000user@alumno.buap.mx", "202268046", "StuBuap2026", padronBase)
        assertFalse(resEmail.ok)
        assertEquals("email", resEmail.campoError)

        val resPass = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202511111", "StuBuap\u00002026", padronBase)
        assertFalse(resPass.ok)
        assertEquals("password", resPass.campoError)
    }

    // --- Vector 5: Resiliencia ante Registros Corruptos en Padrón ---

    @Test
    fun `ADV-PBN-1 - Soportar registros vacios o malformados en el padron sin lanzar excepciones`() {
        val padronCorrupto = listOf(
            UsuarioSTU("", "", ""),
            UsuarioSTU("Javier.sangines@alumno.buap.mx", "202268046", "StuBuap2026")
        )
        val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046", "StuBuap2026", padronCorrupto)
        assertTrue(resLogin.ok)

        val resReg = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202511111", "Fernando2026", padronCorrupto)
        assertTrue(resReg.ok)
    }

    // --- Vector 6: Matrículas Adversariales (Ceros y Caracteres de Control) ---

    @Test
    fun `ADV-MAT-1 - Rechazar matricula compuesta unicamente por ceros (000000000)`() {
        val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "000000000", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("matricula", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "000000000", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("matricula", resReg.campoError)
    }

    @Test
    fun `ADV-MAT-2 - Rechazar matricula con saltos de linea inyectados`() {
        val resLogin = ValidadorLogin.validarLogin("Javier.sangines@alumno.buap.mx", "202268046\n", "StuBuap2026", padronBase)
        assertFalse(resLogin.ok)
        assertEquals("matricula", resLogin.campoError)

        val resReg = ValidadorRegistro.validarRegistro("nuevo@alumno.buap.mx", "202268046\n", "Fernando2026", padronBase)
        assertFalse(resReg.ok)
        assertEquals("matricula", resReg.campoError)
    }
}
