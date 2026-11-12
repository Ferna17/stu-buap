package mx.buap.stu.auth

import mx.buap.stu.data.ResultadoRegistro
import mx.buap.stu.data.UsuarioSTU

/**
 * Controlador lógico de validación para el Módulo de Registro de Nuevo Estudiante STU BUAP (HU-01.1).
 * Implementa las reglas de negocio evaluadas mediante BICEP y CORRECT (PR1 a PR12):
 * - Correo institucional BUAP: 1 a 30 caracteres antes del @ y dominio autorizado.
 * - Matrícula: exactamente 9 dígitos numéricos.
 * - Contraseña: fecha de nacimiento en formato DDMMAA (6 dígitos numéricos con fecha válida).
 * - Control de no duplicidad de matrícula y correo en el padrón activo.
 * - Alta inicial con saldo de pasajes igual a 0 y estado "ACTIVO".
 */
object ValidadorRegistro {

    val DOMINIOS_BUAP = ValidadorCredenciales.DOMINIOS_BUAP

    fun validarRegistro(
        correo: String,
        matricula: String,
        contrasena: String,
        baseDeDatos: List<UsuarioSTU> = ValidadorLogin.PADRON_DEFECTO
    ): ResultadoRegistro {
        val formato = ValidadorCredenciales.validarFormato(
            correo = correo,
            matricula = matricula,
            contrasena = contrasena,
            mensajeCamposVacios = "Completa tu correo, matrícula y contraseña para registrarte"
        )
        if (formato is ResultadoFormato.Invalido) {
            return ResultadoRegistro(
                ok = false,
                campoError = formato.campoError,
                mensaje = formato.mensaje
            )
        }

        val credenciales = (formato as ResultadoFormato.Valido).credenciales

        // PR10: Rechazar registro si la matrícula ya está registrada en el padrón
        val matriculaExiste = baseDeDatos.any {
            it.matricula.trim() == credenciales.matricula
        }
        if (matriculaExiste) {
            return ResultadoRegistro(
                ok = false,
                campoError = "matricula",
                mensaje = "La matrícula ya cuenta con un registro previo"
            )
        }

        // PR11: Rechazar registro si el correo institucional ya está registrado
        val correoExiste = baseDeDatos.any {
            it.correo.trim().equals(credenciales.correo, ignoreCase = true)
        }
        if (correoExiste) {
            return ResultadoRegistro(
                ok = false,
                campoError = "email",
                mensaje = "El correo institucional ya está registrado"
            )
        }

        // PR1 y PR2: Alta de nuevo estudiante con saldo inicial 0 y estado ACTIVO
        val nuevoUsuario = UsuarioSTU(
            correo = credenciales.correo,
            matricula = credenciales.matricula,
            contrasena = credenciales.contrasena,
            nombre = "Alumno BUAP (${credenciales.matricula})",
            pasajesDisponibles = 0,
            estado = "ACTIVO"
        )

        return ResultadoRegistro(
            ok = true,
            mensaje = "Registro exitoso",
            usuario = nuevoUsuario
        )
    }
}
