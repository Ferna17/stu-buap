package mx.buap.stu.auth

/**
 * Credenciales institucionales normalizadas tras superar las validaciones de formato.
 */
internal data class CredencialesNormalizadas(
    val correo: String,
    val matricula: String,
    val contrasena: String
)

/**
 * Resultado interno de la validación sintáctica y de formato compartida entre Login y Registro.
 */
internal sealed class ResultadoFormato {
    data class Valido(val credenciales: CredencialesNormalizadas) : ResultadoFormato()
    data class Invalido(val campoError: String, val mensaje: String) : ResultadoFormato()
}

/**
 * Resultado interno de validación para campos de texto alfabético (nombres y apellidos).
 */
internal sealed class ResultadoCampoTexto {
    data class Valido(val textoNormalizado: String) : ResultadoCampoTexto()
    data class Invalido(val campoError: String, val mensaje: String) : ResultadoCampoTexto()
}

/**
 * Reglas comunes de formato y sanitización BICEP/CORRECT para credenciales institucionales STU BUAP.
 */
internal object ValidadorCredenciales {

    val DOMINIOS_BUAP = listOf("alumno.buap.mx", "alm.buap.mx", "correo.buap.mx")
    const val LONGITUD_MIN_PASSWORD = 8
    const val LONGITUD_MAX_PASSWORD = 20
    const val MENSAJE_PASSWORD_INVALIDA =
        "La contraseña debe tener entre 8 y 20 caracteres sin espacios"

    private val REGEX_USUARIO_EMAIL = Regex("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*$")
    private val REGEX_SOLO_DIGITOS = Regex("^\\d+$")
    private val REGEX_SOLO_CEROS = Regex("^0+$")
    private val REGEX_CONTROL = Regex("[\\r\\n\\u0000]")
    private val REGEX_ESPACIOS_MULTIPLES = Regex("\\s+")
    private val REGEX_NOMBRE_PROPIO = Regex("^[\\p{L}]+([ '-][\\p{L}]+)*$")

    fun contieneControl(valor: String): Boolean = REGEX_CONTROL.containsMatchIn(valor)

    /**
     * Valida y normaliza un campo nominal (nombres o apellidos): obligatorio, 2-50 chars,
     * solo letras (incluyendo acentos/ñ), espacios simples, guiones o apóstrofos.
     */
    fun validarCampoNombre(
        valor: String,
        campoError: String,
        mensajeVacio: String,
        mensajeLongitud: String,
        mensajeCaracteres: String
    ): ResultadoCampoTexto {
        if (contieneControl(valor)) {
            return ResultadoCampoTexto.Invalido(
                campoError,
                "Entrada con caracteres de control no permitidos"
            )
        }
        val limpio = valor.trim().replace(REGEX_ESPACIOS_MULTIPLES, " ")
        if (limpio.isEmpty()) {
            return ResultadoCampoTexto.Invalido(campoError, mensajeVacio)
        }
        if (limpio.length !in 2..50) {
            return ResultadoCampoTexto.Invalido(campoError, mensajeLongitud)
        }
        if (!REGEX_NOMBRE_PROPIO.matches(limpio)) {
            return ResultadoCampoTexto.Invalido(campoError, mensajeCaracteres)
        }
        return ResultadoCampoTexto.Valido(limpio)
    }

    /**
     * Valida y normaliza correo BUAP (1-30 chars antes de @), matrícula (9 dígitos) y contraseña (8-20 chars sin espacios).
     */
    fun validarFormato(
        correo: String,
        matricula: String,
        contrasena: String,
        mensajeCamposVacios: String
    ): ResultadoFormato {
        if (REGEX_CONTROL.containsMatchIn(correo) ||
            REGEX_CONTROL.containsMatchIn(matricula) ||
            REGEX_CONTROL.containsMatchIn(contrasena)
        ) {
            val campo = when {
                REGEX_CONTROL.containsMatchIn(matricula) -> "matricula"
                REGEX_CONTROL.containsMatchIn(correo) -> "email"
                else -> "password"
            }
            return ResultadoFormato.Invalido(campo, "Entrada con caracteres de control no permitidos")
        }

        val emailLimpio = correo.trim()
        val matriculaLimpia = matricula.trim()

        if (emailLimpio.isEmpty() || matriculaLimpia.isEmpty() || contrasena.trim().isEmpty()) {
            return ResultadoFormato.Invalido("todos", mensajeCamposVacios)
        }

        if (!emailLimpio.contains("@")) {
            return ResultadoFormato.Invalido("email", "Formato inválido de correo electrónico")
        }

        val partes = emailLimpio.split("@")
        if (partes.size != 2) {
            return ResultadoFormato.Invalido("email", "Formato de correo no permitido")
        }

        val (usuario, dominio) = partes
        if (usuario.isEmpty()) {
            return ResultadoFormato.Invalido("email", "Ingresa un usuario válido antes del @")
        }
        if (usuario.length > 30) {
            return ResultadoFormato.Invalido(
                "email",
                "El correo excede el máximo de 30 caracteres antes del @"
            )
        }
        if (!REGEX_USUARIO_EMAIL.matches(usuario)) {
            return ResultadoFormato.Invalido("email", "El correo contiene caracteres no permitidos")
        }
        if (!DOMINIOS_BUAP.contains(dominio.lowercase())) {
            return ResultadoFormato.Invalido("email", "Usa un dominio institucional BUAP válido")
        }

        if (!REGEX_SOLO_DIGITOS.matches(matriculaLimpia)) {
            return ResultadoFormato.Invalido("matricula", "La matrícula solo admite dígitos numéricos")
        }
        if (matriculaLimpia.length != 9) {
            return ResultadoFormato.Invalido("matricula", "La matrícula debe tener exactamente 9 dígitos")
        }
        if (REGEX_SOLO_CEROS.matches(matriculaLimpia)) {
            return ResultadoFormato.Invalido("matricula", "La matrícula no puede consistir únicamente en ceros")
        }

        if (contrasena.contains(" ") ||
            contrasena.any { it.isWhitespace() } ||
            contrasena.length < LONGITUD_MIN_PASSWORD ||
            contrasena.length > LONGITUD_MAX_PASSWORD
        ) {
            return ResultadoFormato.Invalido("password", MENSAJE_PASSWORD_INVALIDA)
        }

        return ResultadoFormato.Valido(
            CredencialesNormalizadas(
                correo = emailLimpio,
                matricula = matriculaLimpia,
                contrasena = contrasena
            )
        )
    }
}
