package mx.buap.stu.data

/**
 * Contrato de acceso a datos para usuarios del STU.
 * Diseñado desacoplado para permitir migrar a Room/SQLite en futuros Sprints sin alterar la UI.
 */
interface RepositorioUsuario {
    fun obtenerPadron(): List<UsuarioSTU>
    fun registrarUsuario(usuario: UsuarioSTU)
    fun buscarPorMatricula(matricula: String): UsuarioSTU?
    fun buscarPorCorreo(correo: String): UsuarioSTU?
}

/**
 * Implementación en memoria del repositorio de usuarios.
 */
class RepositorioUsuarioMemoria(
    padronInicial: List<UsuarioSTU> = PADRON_PREDETERMINADO
) : RepositorioUsuario {

    companion object {
        val PADRON_PREDETERMINADO = listOf(
            UsuarioSTU("Javier.sangines@alumno.buap.mx", "202268046", "290504"),
            UsuarioSTU("Javier.sangines@correo.buap.mx", "202268046", "290504")
        )
    }

    private val padron = padronInicial.toMutableList()

    override fun obtenerPadron(): List<UsuarioSTU> = padron.toList()

    override fun registrarUsuario(usuario: UsuarioSTU) {
        padron.add(usuario)
    }

    override fun buscarPorMatricula(matricula: String): UsuarioSTU? {
        val matriculaLimpia = matricula.trim()
        return padron.find { it.matricula.trim() == matriculaLimpia }
    }

    override fun buscarPorCorreo(correo: String): UsuarioSTU? {
        val correoLimpio = correo.trim()
        return padron.find { it.correo.trim().equals(correoLimpio, ignoreCase = true) }
    }
}
