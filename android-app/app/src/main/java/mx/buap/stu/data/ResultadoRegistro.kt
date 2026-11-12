package mx.buap.stu.data

/**
 * Resultado estructurado de la validación de registro de nuevo estudiante (HU-01.1).
 */
data class ResultadoRegistro(
    val ok: Boolean,
    val campoError: String? = null,
    val mensaje: String = "",
    val usuario: UsuarioSTU? = null
)
