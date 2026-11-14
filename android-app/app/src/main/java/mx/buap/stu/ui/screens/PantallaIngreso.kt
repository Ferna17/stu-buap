package mx.buap.stu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    onLoginExitoso: (UsuarioSTU) -> Unit,
    onIrARegistro: (precargar: Boolean) -> Unit = {}
) {
    val resultado = viewModel.resultado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Logo circular azul "STU" fiel al diseño de Figma
        LogoCircularStu()

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

        Spacer(modifier = Modifier.height(8.dp))

        // Acceso directo a Pantalla de Registro (HU-01.1)
        TextButton(
            onClick = { onIrARegistro(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "¿No estás registrado? Regístrate",
                color = AzulOscuroStu,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mensaje de retroalimentación por campo y botón de Registro (PF18)
        resultado?.let { respuesta ->
            Spacer(modifier = Modifier.height(12.dp))
            val mensajeVisible = if (respuesta.puedeRegistrarse) {
                "${respuesta.mensaje}. No estás registrado, debes registrarte para acceder al STU."
            } else {
                respuesta.mensaje
            }
            BannerResultadoStu(
                esExito = respuesta.ok,
                mensaje = mensajeVisible
            )

            if (respuesta.puedeRegistrarse) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onIrARegistro(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, AzulStu)
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
        PanelMetricasStu(
            tiempoRespuestaMs = viewModel.tiempoRespuestaMs,
            tiempoCapturaSeg = viewModel.tiempoCapturaSeg,
            erroresDetectados = viewModel.erroresDetectados
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
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
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
            keyboardOptions = KeyboardOptions(
                capitalization = capitalizacion,
                keyboardType = tipoTeclado
            ),
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

@Preview(name = "Pantalla 1 - Ingreso STU (Formulario Limpio)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaIngreso() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = IngresoViewModel(repo)
        PantallaIngreso(
            viewModel = viewModel,
            onLoginExitoso = {},
            onIrARegistro = {}
        )
    }
}

@Preview(name = "Pantalla 1 - Ingreso STU (Aviso Sin Registro Previo PF18)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaIngresoSinRegistro() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = IngresoViewModel(repo)
        viewModel.actualizarCorreo("nuevo.alumno@alumno.buap.mx")
        viewModel.actualizarMatricula("202512345")
        viewModel.actualizarContrasena("101006")
        viewModel.ejecutarLogin {} // Genera el estado de Sin registro previo
        PantallaIngreso(
            viewModel = viewModel,
            onLoginExitoso = {},
            onIrARegistro = {}
        )
    }
}

@Preview(name = "Pantalla 1 - Ingreso STU (Retorno con Registro Exitoso Flujo B)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaIngresoRegistroExitoso() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = IngresoViewModel(repo)
        viewModel.aplicarRegistroExitoso(
            UsuarioSTU(
                correo = "nuevo.alumno@alumno.buap.mx",
                matricula = "202512345",
                contrasena = "101006",
                nombre = "Alumno BUAP (202512345)"
            )
        )
        PantallaIngreso(
            viewModel = viewModel,
            onLoginExitoso = {},
            onIrARegistro = {}
        )
    }
}
