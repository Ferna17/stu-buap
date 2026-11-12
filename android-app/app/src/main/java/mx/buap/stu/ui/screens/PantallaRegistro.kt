package mx.buap.stu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.theme.AzulOscuroStu
import mx.buap.stu.ui.theme.AzulStu
import mx.buap.stu.ui.theme.TemaStu
import mx.buap.stu.ui.viewmodels.RegistroViewModel

/**
 * Pantalla 1.1: Registro de Nuevo Usuario (HU-01.1).
 * Mantiene exactamente la identidad visual, tipografía, espaciado y métricas en vivo de PantallaIngreso.
 */
@Composable
fun PantallaRegistro(
    viewModel: RegistroViewModel,
    onRegistroExitoso: (UsuarioSTU) -> Unit,
    onVolverALogin: () -> Unit = {}
) {
    val resultado = viewModel.resultado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LogoCircularStu()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Registro de usuario",
            color = AzulOscuroStu,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo 1: Correo institucional BUAP
        CampoFormularioStu(
            etiqueta = "Correo electrónico",
            valor = viewModel.correo,
            alCambiarValor = { viewModel.actualizarCorreo(it) },
            textoGuia = "nombre@alumno.buap.mx",
            esError = resultado?.campoError == "email" || resultado?.campoError == "todos",
            tipoTeclado = KeyboardType.Email,
            alEnfocar = { viewModel.iniciarTemporizador() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Campo 2: Matrícula (9 dígitos)
        CampoFormularioStu(
            etiqueta = "Matrícula",
            valor = viewModel.matricula,
            alCambiarValor = { viewModel.actualizarMatricula(it) },
            textoGuia = "Ingresa tu matrícula",
            esError = resultado?.campoError == "matricula" || resultado?.campoError == "todos",
            tipoTeclado = KeyboardType.Number,
            alEnfocar = { viewModel.iniciarTemporizador() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Campo 3: Contraseña (DDMMAA)
        CampoFormularioStu(
            etiqueta = "Contraseña",
            valor = viewModel.contrasena,
            alCambiarValor = { viewModel.actualizarContrasena(it) },
            textoGuia = "Ingresa Fecha de Nacimiento a 6 dígitos",
            esError = resultado?.campoError == "password" || resultado?.campoError == "todos",
            tipoTeclado = KeyboardType.NumberPassword,
            esContrasena = true,
            alEnfocar = { viewModel.iniciarTemporizador() }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Botón principal "Registrarse" tipo píldora idéntico al de Login
        Button(
            onClick = {
                viewModel.ejecutarRegistro(onRegistroExitoso)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
        ) {
            Text(
                text = "Registrarse",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onVolverALogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                color = AzulOscuroStu,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mensaje de retroalimentación por campo (PR1 a PR12)
        resultado?.let { respuesta ->
            Spacer(modifier = Modifier.height(12.dp))
            BannerResultadoStu(
                esExito = respuesta.ok,
                mensaje = respuesta.mensaje
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Panel compacto de Métricas No Funcionales en Vivo
        PanelMetricasStu(
            tiempoRespuestaMs = viewModel.tiempoRespuestaMs,
            tiempoCapturaSeg = viewModel.tiempoCapturaSeg,
            erroresDetectados = viewModel.erroresDetectados
        )
    }
}

@Preview(name = "Pantalla 1.1 - Registro STU", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaRegistro() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = RegistroViewModel(repo)
        PantallaRegistro(
            viewModel = viewModel,
            onRegistroExitoso = {}
        )
    }
}
