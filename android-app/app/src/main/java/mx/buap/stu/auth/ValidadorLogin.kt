package mx.buap.stu.auth

import mx.buap.stu.data.ResultadoLogin
import mx.buap.stu.data.UsuarioSTU

/**
 * Controlador lógico de validación para el Módulo de Inicio de Sesión STU BUAP (HU-01).
 * Implementa las reglas de negocio evaluadas mediante BICEP y CORRECT (PF1 a PF19):
 * - Correo institucional BUAP: 1 a 30 caracteres antes del @ y dominio autorizado.
 * - Matrícula: exactamente 9 dígitos numéricos.
 * - Contraseña: fecha de nacimiento en formato DDMMAA (6 dígitos numéricos con día y mes válidos).
 * - Verificación de coincidencia en el padrón y detección de usuario sin registro previo.
 */
object ValidadorLogin {

    val DOMINIOS_BUAP = ValidadorCredenciales.DOMINIOS_BUAP

    val PADRON_DEFECTO = listOf(
        UsuarioSTU("Javier.sangines@alumno.buap.mx", "202268046", "290504"),
        UsuarioSTU("Javier.sangines@correo.buap.mx", "202268046", "290504")
    )

    fun validarLogin(
        correo: String,
        matricula: String,
        contrasena: String,
        baseDeDatos: List<UsuarioSTU> = PADRON_DEFECTO
    ): ResultadoLogin {
        val formato = ValidadorCredenciales.validarFormato(
            correo = correo,
            matricula = matricula,
            contrasena = contrasena,
            mensajeCamposVacios = "Completa tu correo, matrícula y contraseña para entrar"
        )
        if (formato is ResultadoFormato.Invalido) {
            return ResultadoLogin(
                ok = false,
                campoError = formato.campoError,
                mensaje = formato.mensaje
            )
        }

        val credenciales = (formato as ResultadoFormato.Valido).credenciales

        // Búsqueda combinada en el padrón (prioriza coincidencia exacta matrícula + correo)
        val usuarioEncontrado = baseDeDatos.find {
            it.matricula.trim() == credenciales.matricula &&
                it.correo.trim().equals(credenciales.correo, ignoreCase = true)
        } ?: baseDeDatos.find {
            it.matricula.trim() == credenciales.matricula ||
                it.correo.trim().equals(credenciales.correo, ignoreCase = true)
        }

        // PF18: Usuario sin registro previo
        if (usuarioEncontrado == null) {
            return ResultadoLogin(
                ok = false,
                campoError = "registro",
                mensaje = "Sin registro previo",
                puedeRegistrarse = true
            )
        }

        // PF17: El correo no coincide con la matrícula registrada
        if (!usuarioEncontrado.correo.trim().equals(credenciales.correo, ignoreCase = true)) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "El correo no coincide"
            )
        }

        // PF16: La matrícula no coincide con el correo registrado
        if (usuarioEncontrado.matricula.trim() != credenciales.matricula) {
            return ResultadoLogin(
                ok = false,
                campoError = "matricula",
                mensaje = "La matrícula no coincide"
            )
        }

        // PF15: Contraseña incorrecta
        if (usuarioEncontrado.contrasena.trim() != credenciales.contrasena) {
            return ResultadoLogin(
                ok = false,
                campoError = "password",
                mensaje = "Contraseña incorrecta"
            )
        }

        return ResultadoLogin(
            ok = true,
            mensaje = "¡Acceso concedido! Bienvenido al STU BUAP",
            usuario = usuarioEncontrado
        )
    }
}
