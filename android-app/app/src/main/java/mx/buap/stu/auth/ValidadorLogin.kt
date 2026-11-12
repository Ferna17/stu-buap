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

    val DOMINIOS_BUAP = listOf("alumno.buap.mx", "alm.buap.mx", "correo.buap.mx")

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
        // PF19: Ausencia de datos obligatorios
        if (correo.isEmpty() || matricula.isEmpty() || contrasena.isEmpty()) {
            return ResultadoLogin(
                ok = false,
                campoError = "todos",
                mensaje = "Completa tu correo, matrícula y contraseña para entrar"
            )
        }

        // PF12: Formato de correo (debe incluir @)
        if (!correo.contains("@")) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "Formato inválido de correo electrónico"
            )
        }

        val partes = correo.split("@")
        if (partes.size != 2) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "Formato de correo no permitido"
            )
        }

        val (usuario, dominio) = partes

        // PF3: Límite inválido inferior (0 caracteres antes del @)
        if (usuario.isEmpty()) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "Ingresa un usuario válido antes del @"
            )
        }

        // PF4 (1 caract.), PF5 (30 caract.), PF6 (>30 caract.)
        if (usuario.length > 30) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "El correo excede el máximo de 30 caracteres antes del @"
            )
        }

        // PF1, PF2, PF11: Dominios institucionales BUAP permitidos
        if (!DOMINIOS_BUAP.contains(dominio.lowercase())) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "Usa un dominio institucional BUAP válido"
            )
        }

        // PF13: La matrícula solo admite dígitos numéricos
        if (!matricula.matches(Regex("^\\d+$"))) {
            return ResultadoLogin(
                ok = false,
                campoError = "matricula",
                mensaje = "La matrícula solo admite dígitos numéricos"
            )
        }

        // PF7 (8 dígitos), PF8 (10 dígitos): Exactamente 9 dígitos
        if (matricula.length != 9) {
            return ResultadoLogin(
                ok = false,
                campoError = "matricula",
                mensaje = "La matrícula debe tener exactamente 9 dígitos"
            )
        }

        // PF9 (5 dígitos), PF10 (7 dígitos): Contraseña de 6 dígitos (DDMMAA)
        if (!contrasena.matches(Regex("^\\d{6}$"))) {
            return ResultadoLogin(
                ok = false,
                campoError = "password",
                mensaje = "La contraseña debe contener 6 dígitos (DDMMAA)"
            )
        }

        // PF14: Validación de fecha válida (día 01-31, mes 01-12)
        val dia = contrasena.substring(0, 2).toInt()
        val mes = contrasena.substring(2, 4).toInt()
        if (dia < 1 || dia > 31 || mes < 1 || mes > 12) {
            return ResultadoLogin(
                ok = false,
                campoError = "password",
                mensaje = "La contraseña no corresponde a una fecha válida"
            )
        }

        // Búsqueda combinada en el padrón (prioriza coincidencia exacta matrícula + correo)
        val usuarioEncontrado = baseDeDatos.find {
            it.matricula == matricula && it.correo.equals(correo, ignoreCase = true)
        } ?: baseDeDatos.find {
            it.matricula == matricula || it.correo.equals(correo, ignoreCase = true)
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
        if (!usuarioEncontrado.correo.equals(correo, ignoreCase = true)) {
            return ResultadoLogin(
                ok = false,
                campoError = "email",
                mensaje = "El correo no coincide"
            )
        }

        // PF16: La matrícula no coincide con el correo registrado
        if (usuarioEncontrado.matricula != matricula) {
            return ResultadoLogin(
                ok = false,
                campoError = "matricula",
                mensaje = "La matrícula no coincide"
            )
        }

        // PF15: Contraseña incorrecta
        if (usuarioEncontrado.contrasena != contrasena) {
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
