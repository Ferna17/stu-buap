package mx.buap.stu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.data.RepositorioSalidasMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.rutas.CAPACIDAD_AUTOBUS_STU
import mx.buap.stu.rutas.EstadoSalida
import mx.buap.stu.rutas.ReservaSalida
import mx.buap.stu.rutas.RutaStu
import mx.buap.stu.rutas.SalidaAutobus
import mx.buap.stu.saldo.EvaluadorSaldo
import mx.buap.stu.ui.theme.*
import mx.buap.stu.ui.viewmodels.RutasViewModel

/**
 * Pantalla HU-06 con flujo limpio por pasos mediante menús desplegables compactos:
 * 1) Menú desplegable de ruta (Transportes),
 * 2) Menú desplegable de horarios de la ruta elegida,
 * 3) Mapa en vivo 2+2 de los 36 asientos de pasajeros visible únicamente después de elegir ruta y horario.
 */
@Composable
fun PantallaRutasSalidas(
    usuario: UsuarioSTU,
    viewModel: RutasViewModel,
    onVolverInicio: () -> Unit,
    onIrARecargar: () -> Unit
) {
    val todasLasSalidas by viewModel.salidasFlow.collectAsState()
    val estadoSaldo = EvaluadorSaldo.evaluar(usuario)

    LaunchedEffect(
        todasLasSalidas,
        viewModel.rutaSeleccionada.id,
        usuario.matricula
    ) {
        viewModel.sincronizarDesdeFlujo(todasLasSalidas, usuario.matricula)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EncabezadoCompactoRutasStu(onVolverInicio = onVolverInicio)

            // Contenedor principal blanco estilo prototipo con datos y menús desplegables
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GrisBorde),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilaDatoEstudianteStu(etiqueta = "Nombre", valor = usuario.nombre)
                        FilaDatoEstudianteStu(etiqueta = "Matrícula", valor = usuario.matricula)
                        FilaDatoEstudianteStu(
                            etiqueta = "Saldo",
                            valor = estadoSaldo.etiquetaSaldo,
                            colorValor = if (estadoSaldo.puedeReservarViaje) AzulOscuroStu else TextoError
                        )
                    }

                    HorizontalDivider(color = GrisBorde)

                    // PASO 1: Menú desplegable compacto de Rutas (Transportes)
                    Text(
                        text = "1. Transporte (Ruta)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegroEtiqueta
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        SelectorDesplegableRutaStu(
                            rutaSeleccionada = viewModel.rutaSeleccionada,
                            rutaElegidaPorUsuario = viewModel.rutaElegidaPorUsuario,
                            desplegado = viewModel.selectorRutaDesplegado,
                            onAlternar = { viewModel.alternarSelectorRuta() }
                        )

                        if (viewModel.selectorRutaDesplegado) {
                            Spacer(modifier = Modifier.height(4.dp))
                            MenuDesplegableRutasStu(
                                rutas = viewModel.rutas,
                                rutaActivaId = viewModel.rutaSeleccionada.id.takeIf { viewModel.rutaElegidaPorUsuario },
                                onElegirRuta = { rutaId ->
                                    viewModel.seleccionarRuta(rutaId, usuario.matricula)
                                }
                            )
                        }
                    }

                    // PASO 2: Menú desplegable de Horarios (el mapa NO aparece mientras está desplegado)
                    if (viewModel.rutaElegidaPorUsuario && !viewModel.selectorRutaDesplegado) {
                        HorizontalDivider(color = GrisBorde)

                        Text(
                            text = "2. Horario de salida",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegroEtiqueta
                        )

                        val horarioAbierto =
                            !viewModel.horarioElegidoPorUsuario || viewModel.selectorHorarioDesplegado

                        Column(modifier = Modifier.fillMaxWidth()) {
                            SelectorDesplegableHorarioStu(
                                salidaSeleccionada = viewModel.salidaSeleccionada,
                                horarioElegidoPorUsuario = viewModel.horarioElegidoPorUsuario,
                                desplegado = horarioAbierto,
                                onAlternar = { viewModel.alternarSelectorHorario() }
                            )

                            if (horarioAbierto) {
                                Spacer(modifier = Modifier.height(4.dp))
                                MenuDesplegableHorariosStu(
                                    salidas = viewModel.salidasDeRuta,
                                    salidaActivaId = viewModel.salidaSeleccionada?.id.takeIf {
                                        viewModel.horarioElegidoPorUsuario
                                    },
                                    onElegirSalida = { salidaId ->
                                        viewModel.seleccionarSalida(salidaId, usuario.matricula)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // PASO 3: Mapa de los 36 asientos y confirmación (solo después de elegir ruta y horario)
            if (viewModel.mostrarMapaAsientos) {
                viewModel.salidaSeleccionada?.let { salidaActual ->
                    MapaAsientosAutobus(
                        salida = salidaActual,
                        matriculaActual = usuario.matricula
                    )
                }

                viewModel.mensajeResultado?.let { mensaje ->
                    BannerResultadoStu(
                        esExito = !viewModel.esErrorResultado,
                        mensaje = mensaje
                    )
                }

                if (viewModel.mostrarAccionRecarga) {
                    Button(
                        onClick = onIrARecargar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_ir_recargar_rutas"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = TextoError)
                    ) {
                        Text(
                            text = "Ir a recargar pasajes",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                viewModel.reservaConfirmada?.let { reserva ->
                    TarjetaReservaConfirmadaStu(reserva = reserva)
                }

                Button(
                    onClick = { viewModel.confirmarSalidaActual(usuario) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_confirmar_salida"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
                ) {
                    Text(
                        text = "Confirmar salida y asignar asiento",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCompactoRutasStu(
    onVolverInicio: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LogoCircularStu()

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Rutas y salidas STU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuroStu
            )
            Text(
                text = "Transporte Universitario",
                fontSize = 12.sp,
                color = GrisTextoSecundario
            )
        }

        Button(
            onClick = onVolverInicio,
            modifier = Modifier
                .height(40.dp)
                .testTag("btn_volver_inicio"),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulStu)
        ) {
            Text(
                text = "Volver",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FilaDatoEstudianteStu(
    etiqueta: String,
    valor: String,
    colorValor: Color = NegroEtiqueta
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GrisTextoSubtitulo,
            modifier = Modifier.width(86.dp)
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorValor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectorDesplegableRutaStu(
    rutaSeleccionada: RutaStu,
    rutaElegidaPorUsuario: Boolean,
    desplegado: Boolean,
    onAlternar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onAlternar)
            .testTag("selector_ruta"),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (desplegado || rutaElegidaPorUsuario) 1.5.dp else 1.dp,
            color = if (desplegado || rutaElegidaPorUsuario) AzulStu else GrisBorde
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (rutaElegidaPorUsuario) FondoAzulSuave else FondoTarjetaStu
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (rutaElegidaPorUsuario) {
                        rutaSeleccionada.nombre
                    } else {
                        "Selecciona una ruta de transporte"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rutaElegidaPorUsuario) AzulOscuroStu else GrisTextoSubtitulo
                )
                if (rutaElegidaPorUsuario) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${rutaSeleccionada.origen} → ${rutaSeleccionada.destino}",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = GrisTextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (desplegado) "▲" else "▼",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AzulStu
            )
        }
    }
}

@Composable
private fun MenuDesplegableRutasStu(
    rutas: List<RutaStu>,
    rutaActivaId: String?,
    onElegirRuta: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, AzulStu),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            rutas.forEachIndexed { indice, ruta ->
                val seleccionada = ruta.id == rutaActivaId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (seleccionada) FondoAzulSuave else Color.White)
                        .clickable { onElegirRuta(ruta.id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("item_ruta_${ruta.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ruta.nombre,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (seleccionada) AzulOscuroStu else NegroEtiqueta
                        )
                        Text(
                            text = "${ruta.origen} → ${ruta.destino}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = GrisTextoSecundario
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (seleccionada) "✓" else "Elegir ›",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulStu
                    )
                }
                if (indice < rutas.lastIndex) {
                    HorizontalDivider(color = GrisBorde)
                }
            }
        }
    }
}

@Composable
private fun SelectorDesplegableHorarioStu(
    salidaSeleccionada: SalidaAutobus?,
    horarioElegidoPorUsuario: Boolean,
    desplegado: Boolean,
    onAlternar: () -> Unit
) {
    val salidaMostrar = salidaSeleccionada?.takeIf { horarioElegidoPorUsuario }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onAlternar)
            .testTag("selector_horario"),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (desplegado || salidaMostrar != null) 1.5.dp else 1.dp,
            color = if (desplegado || salidaMostrar != null) AzulStu else GrisBorde
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (salidaMostrar != null) FondoAzulSuave else FondoTarjetaStu
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (salidaMostrar != null) {
                val (etiquetaEstado, fondoEstado, textoEstado) =
                    obtenerEstiloEstadoSalida(salidaMostrar.estadoActual)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${salidaMostrar.horaSalida} • ${salidaMostrar.anden}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuroStu
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${salidaMostrar.totalOcupados}/$CAPACIDAD_AUTOBUS_STU ocupados",
                        fontSize = 12.sp,
                        color = GrisTextoSecundario
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(fondoEstado)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = etiquetaEstado,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textoEstado
                        )
                    }
                    Text(
                        text = if (desplegado) "▲" else "▼",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulStu
                    )
                }
            } else {
                Text(
                    text = "Selecciona un horario de salida",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisTextoSubtitulo,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (desplegado) "▲" else "▼",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulStu
                )
            }
        }
    }
}

@Composable
private fun MenuDesplegableHorariosStu(
    salidas: List<SalidaAutobus>,
    salidaActivaId: String?,
    onElegirSalida: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, AzulStu),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            salidas.forEachIndexed { indice, salida ->
                val seleccionada = salida.id == salidaActivaId
                val (etiquetaEstado, fondoEstado, textoEstado) =
                    obtenerEstiloEstadoSalida(salida.estadoActual)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (seleccionada) FondoAzulSuave else Color.White)
                        .clickable { onElegirSalida(salida.id) }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                        .testTag("salida_${salida.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = salida.horaSalida,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (seleccionada) AzulOscuroStu else NegroEtiqueta
                        )
                        Text(
                            text = "${salida.anden} • ${salida.totalOcupados}/$CAPACIDAD_AUTOBUS_STU ocupados",
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(fondoEstado)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = etiquetaEstado,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textoEstado
                        )
                    }
                }
                if (indice < salidas.lastIndex) {
                    HorizontalDivider(color = GrisBorde)
                }
            }
        }
    }
}

private fun obtenerEstiloEstadoSalida(estado: EstadoSalida): Triple<String, Color, Color> =
    when (estado) {
        EstadoSalida.DISPONIBLE -> Triple("DISPONIBLE", FondoExito, TextoExito)
        EstadoSalida.PROXIMO -> Triple("PRÓXIMO", FondoEstadoProximo, TextoEstadoProximo)
        EstadoSalida.LLENO -> Triple("LLENO", FondoError, TextoError)
    }

@Composable
private fun TarjetaReservaConfirmadaStu(reserva: ReservaSalida) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tarjeta_reserva_confirmada"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, BordeAzulBuapAsiento),
        colors = CardDefaults.cardColors(containerColor = AzulBuapAsiento)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tu asiento asignado: Asiento ${reserva.asientoAsignado} de $CAPACIDAD_AUTOBUS_STU",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${reserva.nombreRuta} • ${reserva.horaSalida} (${reserva.anden})",
                fontSize = 13.sp,
                color = Color(0xFFE0F2FE)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Bloqueado para la matrícula ${reserva.matriculaEstudiante}",
                fontSize = 12.sp,
                color = Color(0xFFBAE6FD)
            )
        }
    }
}

@Preview(name = "1. Menú desplegable cerrado (Estado inicial)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPrevia1MenuRutaCerrado() {
    TemaStu {
        val usuario = UsuarioSTU(
            correo = "mateo.morales@alumno.buap.mx",
            matricula = "202345678",
            contrasena = "AccesoStu99",
            nombre = "Mateo Morales Vega",
            pasajesDisponibles = 10
        )
        val viewModel = RutasViewModel(RepositorioSalidasMemoria())
        PantallaRutasSalidas(
            usuario = usuario,
            viewModel = viewModel,
            onVolverInicio = {},
            onIrARecargar = {}
        )
    }
}

@Preview(name = "2. Menú desplegable abierto (9 rutas)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPrevia2MenuRutaDesplegado() {
    TemaStu {
        val usuario = UsuarioSTU(
            correo = "mateo.morales@alumno.buap.mx",
            matricula = "202345678",
            contrasena = "AccesoStu99",
            nombre = "Mateo Morales Vega",
            pasajesDisponibles = 10
        )
        val viewModel = RutasViewModel(RepositorioSalidasMemoria()).apply {
            alternarSelectorRuta()
        }
        PantallaRutasSalidas(
            usuario = usuario,
            viewModel = viewModel,
            onVolverInicio = {},
            onIrARecargar = {}
        )
    }
}

@Preview(name = "3. Menú desplegable de horarios (sin mapa)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPrevia3MenuHorarioDesplegado() {
    TemaStu {
        val usuario = UsuarioSTU(
            correo = "mateo.morales@alumno.buap.mx",
            matricula = "202345678",
            contrasena = "AccesoStu99",
            nombre = "Mateo Morales Vega",
            pasajesDisponibles = 10
        )
        val viewModel = RutasViewModel(RepositorioSalidasMemoria()).apply {
            seleccionarRuta("CU-Amalucan", usuario.matricula)
        }
        PantallaRutasSalidas(
            usuario = usuario,
            viewModel = viewModel,
            onVolverInicio = {},
            onIrARecargar = {}
        )
    }
}

@Preview(name = "4. Mapa de 36 asientos y Asiento 18 asignado", showBackground = true, showSystemUi = true)
@Composable
fun VistaPrevia4MapaAsientosYReserva() {
    TemaStu {
        val usuario = UsuarioSTU(
            correo = "mateo.morales@alumno.buap.mx",
            matricula = "202345678",
            contrasena = "AccesoStu99",
            nombre = "Mateo Morales Vega",
            pasajesDisponibles = 10
        )
        val viewModel = RutasViewModel(RepositorioSalidasMemoria()).apply {
            seleccionarRuta("CU-Amalucan", usuario.matricula)
            seleccionarSalida("cu-amalucan-1430", usuario.matricula)
            confirmarSalidaActual(usuario)
        }
        PantallaRutasSalidas(
            usuario = usuario,
            viewModel = viewModel,
            onVolverInicio = {},
            onIrARecargar = {}
        )
    }
}

@Preview(name = "5. Saldo 0 con botón de recarga", showBackground = true, showSystemUi = true)
@Composable
fun VistaPrevia5SinSaldoRecarga() {
    TemaStu {
        val usuario = UsuarioSTU(
            correo = "valeria.soto@alumno.buap.mx",
            matricula = "202500001",
            contrasena = "ValeriaStu88",
            nombre = "Valeria Soto Paredes",
            pasajesDisponibles = 0
        )
        val viewModel = RutasViewModel(RepositorioSalidasMemoria()).apply {
            seleccionarRuta("CU-Amalucan", usuario.matricula)
            seleccionarSalida("cu-amalucan-1430", usuario.matricula)
            confirmarSalidaActual(usuario)
        }
        PantallaRutasSalidas(
            usuario = usuario,
            viewModel = viewModel,
            onVolverInicio = {},
            onIrARecargar = {}
        )
    }
}
