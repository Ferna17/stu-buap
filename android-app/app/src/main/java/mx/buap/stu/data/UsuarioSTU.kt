package mx.buap.stu.data

/**
 * Entidad que representa a un estudiante registrado en el padrón del STU BUAP.
 */
data class UsuarioSTU(
    val correo: String,
    val matricula: String,
    val contrasena: String,
    val nombre: String = "Javier Sanginés",
    val pasajesDisponibles: Int = 10,
    val estado: String = "ACTIVO"
)
