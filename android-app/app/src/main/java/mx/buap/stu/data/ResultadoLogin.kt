package mx.buap.stu.data

/**
 * Resultado estructurado de la validación de inicio de sesión (HU-01).
 */
data class ResultadoLogin(
    val ok: Boolean,
    val campoError: String? = null,
    val mensaje: String = "",
    val puedeRegistrarse: Boolean = false,
    val usuario: UsuarioSTU? = null
)
