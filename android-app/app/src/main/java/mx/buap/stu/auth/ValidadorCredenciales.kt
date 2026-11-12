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
 * Reglas comunes de formato y sanitización BICEP/CORRECT para credenciales institucionales STU BUAP.
 */
internal object ValidadorCredenciales {

    val DOMINIOS_BUAP = listOf("alumno.buap.mx", "alm.buap.mx", "correo.buap.mx")

    private val REGEX_USUARIO_EMAIL = Regex("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*$")
    private val REGEX_SOLO_DIGITOS = Regex("^\\d+$")
    private val REGEX_SOLO_CEROS = Regex("^0+$")
    private val REGEX_SEIS_DIGITOS = Regex("^\\d{6}$")
    private val REGEX_CONTROL = Regex("[\\r\\n\\u0000]")

    /**
     * Valida estrictamente fechas en formato DDMMAA considerando días por mes y años bisiestos.
     */
    fun esFechaNacimientoValida(ddmmaa: String): Boolean {
        if (!REGEX_SEIS_DIGITOS.matches(ddmmaa)) {
            return false
        }
        val dia = ddmmaa.substring(0, 2).toInt()
        val mes = ddmmaa.substring(2, 4).toInt()
        val anio = ddmmaa.substring(4, 6).toInt()

        if (mes < 1 || mes > 12 || dia < 1) {
            return false
        }

        val diasPorMes = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val esBisiesto = anio % 4 == 0
        val maxDias = if (mes == 2 && esBisiesto) 29 else diasPorMes[mes]

        return dia <= maxDias
    }

    /**
     * Valida y normaliza correo BUAP (1-30 chars antes de @), matrícula (9 dígitos) y contraseña (DDMMAA).
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
        val passwordLimpia = contrasena.trim()

        if (emailLimpio.isEmpty() || matriculaLimpia.isEmpty() || passwordLimpia.isEmpty()) {
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

        if (!REGEX_SEIS_DIGITOS.matches(passwordLimpia)) {
            return ResultadoFormato.Invalido("password", "La contraseña debe contener 6 dígitos (DDMMAA)")
        }
        if (!esFechaNacimientoValida(passwordLimpia)) {
            return ResultadoFormato.Invalido("password", "La contraseña no corresponde a una fecha válida")
        }

        return ResultadoFormato.Valido(
            CredencialesNormalizadas(
                correo = emailLimpio,
                matricula = matriculaLimpia,
                contrasena = passwordLimpia
            )
        )
    }
}
