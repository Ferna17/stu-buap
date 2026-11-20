package mx.buap.stu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.saldo.EvaluadorSaldo
import mx.buap.stu.ui.theme.*
import mx.buap.stu.ui.viewmodels.InicioViewModel

/**
 * Pantalla 2: Inicio y Datos del Estudiante (P2 Datos — HU-02).
 * Muestra el nombre del estudiante, su saldo de pasajes STU y el menú oficial de 4 accesos.
 */
@Composable
fun PantallaInicioStu(
    viewModel: InicioViewModel,
    onIrARecargar: () -> Unit = {},
    onIrAEstadoPagos: () -> Unit = {},
    onIrARutas: () -> Unit = {},
    onIrABoletos: () -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val usuario = viewModel.usuarioActual ?: return
    val estadoSaldo = viewModel.estadoSaldo ?: EvaluadorSaldo.evaluar(usuario)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoCircularStu()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Inicio STU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuroStu,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Saldo y accesos del estudiante",
                fontSize = 13.sp,
                color = GrisTextoSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta única: Nombre del estudiante + Saldo de pasajes STU (sin correo, matrícula ni estado)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (estadoSaldo.requiereRecarga) BordeErrorSuave else BordeAzulSuave
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (estadoSaldo.requiereRecarga) FondoErrorSuave else FondoAzulSuave
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Estudiante",
                        fontSize = 12.sp,
                        color = GrisTextoSecundario
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = usuario.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegroEtiqueta
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = if (estadoSaldo.requiereRecarga) BordeErrorSuave else BordeAzulSuave
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pasajes STU disponibles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSubtitulo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = estadoSaldo.etiquetaSaldo,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estadoSaldo.puedeReservarViaje) AzulOscuroStu else TextoError
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BannerResultadoStu(
                        esExito = estadoSaldo.puedeReservarViaje,
                        mensaje = estadoSaldo.mensajeEstado
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Menú del estudiante (exactamente 4 accesos + Cerrar sesión)
            Text(
                text = "Menú del estudiante",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrisTextoSubtitulo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Recargar pasajes (siempre habilitado)
            Button(
                onClick = onIrARecargar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
            ) {
                Text(
                    text = "Recargar pasajes",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Estado de mis pagos (siempre habilitado)
            BotonAccesoSecundarioStu(
                texto = "Estado de mis pagos",
                onClick = onIrAEstadoPagos
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Rutas y salidas (HU-02 CA-2 y HU-06 CA-4: estilo activo siempre habilitado; si saldo == 0 abre aviso con recarga directa)
            BotonAccesoSecundarioStu(
                texto = "Rutas y salidas",
                onClick = {
                    val seleccion = viewModel.intentarSeleccionarSalida()
                    if (seleccion?.permitido == true) {
                        onIrARutas()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Mis boletos (siempre habilitado)
            BotonAccesoSecundarioStu(
                texto = "Mis boletos",
                onClick = onIrABoletos
            )

            viewModel.avisoModulo?.let { aviso ->
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FondoAzulSuave)
                        .border(1.dp, BordeAzulSuave, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = aviso,
                        color = AzulOscuroStu,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Cerrar sesión
            Button(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroStu)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (viewModel.mostrarAvisoSinPasajes) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarAvisoSinPasajes() },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Sin pasajes disponibles",
                    color = AzulOscuroStu,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Necesitas al menos 1 pasaje disponible para seleccionar una salida",
                    color = NegroEtiqueta,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cerrarAvisoSinPasajes()
                        onIrARecargar()
                    },
                    modifier = Modifier.testTag("dialogo_btn_recargar"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
                ) {
                    Text(
                        text = "Recargar pasajes",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarAvisoSinPasajes() }) {
                    Text(
                        text = "Cancelar",
                        color = GrisTextoSecundario,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
private fun BotonAccesoSecundarioStu(
    texto: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, AzulStu),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = AzulOscuroStu
        )
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(name = "Inicio STU - Con saldo (10 viajes)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaInicioConSaldo() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = InicioViewModel(repo).apply {
            cargarUsuario(
                UsuarioSTU(
                    correo = "Javier.sangines@alumno.buap.mx",
                    matricula = "202268046",
                    contrasena = "StuBuap2026",
                    nombre = "Javier Sanginés",
                    pasajesDisponibles = 10,
                    estado = "ACTIVO"
                )
            )
        }
        PantallaInicioStu(viewModel = viewModel, onCerrarSesion = {})
    }
}

@Preview(name = "Inicio STU - Saldo mínimo (1 viaje)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaInicioUnViaje() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = InicioViewModel(repo).apply {
            cargarUsuario(
                UsuarioSTU(
                    correo = "Javier.sangines@alumno.buap.mx",
                    matricula = "202268046",
                    contrasena = "StuBuap2026",
                    nombre = "Javier Sanginés",
                    pasajesDisponibles = 1,
                    estado = "ACTIVO"
                )
            )
        }
        PantallaInicioStu(viewModel = viewModel, onCerrarSesion = {})
    }
}

@Preview(name = "Inicio STU - Sin saldo (0 viajes)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaInicioSinSaldo() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        val viewModel = InicioViewModel(repo).apply {
            cargarUsuario(
                UsuarioSTU(
                    correo = "fernando.hernandez@alumno.buap.mx",
                    matricula = "202112345",
                    contrasena = "Fernando2026",
                    nombre = "Fernando Jesús Hernández Gorgua",
                    pasajesDisponibles = 0,
                    estado = "ACTIVO"
                )
            )
        }
        PantallaInicioStu(viewModel = viewModel, onCerrarSesion = {})
    }
}
