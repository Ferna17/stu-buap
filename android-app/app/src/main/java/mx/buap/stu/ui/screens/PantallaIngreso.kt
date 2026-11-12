package mx.buap.stu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.theme.*
import mx.buap.stu.ui.viewmodels.IngresoViewModel

/**
 * Pantalla 1: Ingreso (Login HU-01).
 * Implementa fielmente la interfaz diseñada en Figma vinculada al ViewModel MVVM.
 */
@Composable
fun PantallaIngreso(
    viewModel: IngresoViewModel,
    onLoginExitoso: (UsuarioSTU) -> Unit
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
        // Logo circular azul "STU" fiel al diseño de Figma
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AzulStu),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "STU",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Campo 1: Correo electrónico
        CampoFormularioStu(
            etiqueta = "Correo electrónico",
            valor = viewModel.correo,
            alCambiarValor = { viewModel.actualizarCorreo(it) },
            textoGuia = "nombre@ejemplo.com",
            esError = resultado?.campoError == "email" || resultado?.campoError == "todos",
            tipoTeclado = KeyboardType.Email,
            alEnfocar = { viewModel.iniciarTemporizador() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Campo 2: Matrícula
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

        // Botón principal "Inicio" tipo píldora (Figma)
        Button(
            onClick = {
                viewModel.ejecutarLogin(onLoginExitoso)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
        ) {
            Text(
                text = "Inicio",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mensaje de retroalimentación por campo y botón de Registro (PF18)
        resultado?.let { respuesta ->
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (respuesta.ok) FondoExito else FondoError)
                    .padding(12.dp)
            ) {
                Text(
                    text = respuesta.mensaje,
                    color = if (respuesta.ok) TextoExito else TextoError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (respuesta.puedeRegistrarse) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.registrarNuevoUsuario(onLoginExitoso)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Registrarse en el padrón STU",
                        color = AzulOscuroStu,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Panel compacto de Métricas No Funcionales en Vivo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, GrisBorde, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Métricas en vivo (Evidencia No Funcional):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Tiempo de respuesta del validador: ${viewModel.tiempoRespuestaMs} ms",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )
            Text(
                text = "• Tiempo de captura del usuario: ${viewModel.tiempoCapturaSeg} s",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )
            Text(
                text = "• Errores de captura detectados: ${viewModel.erroresDetectados}",
                fontSize = 11.sp,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
fun CampoFormularioStu(
    etiqueta: String,
    valor: String,
    alCambiarValor: (String) -> Unit,
    textoGuia: String,
    esError: Boolean,
    tipoTeclado: KeyboardType,
    esContrasena: Boolean = false,
    alEnfocar: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            color = NegroEtiqueta,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiarValor,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) alEnfocar() },
            placeholder = {
                Text(
                    text = textoGuia,
                    color = GrisTextoGuia,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            isError = esError,
            visualTransformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulStu,
                unfocusedBorderColor = GrisBorde,
                errorBorderColor = Color(0xFFDC2626),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Preview(name = "Pantalla 1 - Ingreso STU", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaIngreso() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = IngresoViewModel(repo)
        PantallaIngreso(
            viewModel = viewModel,
            onLoginExitoso = {}
        )
    }
}
