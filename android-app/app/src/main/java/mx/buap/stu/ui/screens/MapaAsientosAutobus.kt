package mx.buap.stu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.rutas.CAPACIDAD_AUTOBUS_STU
import mx.buap.stu.rutas.EstadoAsiento
import mx.buap.stu.rutas.SalidaAutobus
import mx.buap.stu.ui.theme.*

/**
 * Mapa visual vertical 2+2 de los 36 asientos de pasajeros en tiempo real (9 filas x 4 asientos con pasillo central).
 */
@Composable
fun MapaAsientosAutobus(
    salida: SalidaAutobus,
    matriculaActual: String?,
    modifier: Modifier = Modifier
) {
    val mapaAsientos = salida.obtenerMapaAsientos(matriculaActual)
    val filas = mapaAsientos.chunked(4)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GrisBorde),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mapa de asientos de pasajeros ($CAPACIDAD_AUTOBUS_STU lugares)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AzulOscuroStu
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FondoAzulSuave)
                    .border(1.dp, BordeAzulSuave, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Ocupados: ${salida.totalOcupados} / $CAPACIDAD_AUTOBUS_STU • Disponibles: ${salida.totalDisponibles}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulOscuroStu,
                    modifier = Modifier.testTag("contador_asientos_vivo")
                )
            }

            // Leyenda cromática de estados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElementoLeyendaAsiento(
                    etiqueta = "Disponible",
                    fondo = FondoAsientoDisponible,
                    borde = BordeAsientoDisponible
                )
                ElementoLeyendaAsiento(
                    etiqueta = "Ocupado",
                    fondo = FondoAsientoOcupado,
                    borde = BordeAsientoOcupado
                )
                ElementoLeyendaAsiento(
                    etiqueta = "Tu asiento",
                    fondo = AzulBuapAsiento,
                    borde = BordeAzulBuapAsiento
                )
            }

            HorizontalDivider(color = GrisBorde)

            // Distribución 2+2 exclusiva de los 36 asientos de pasajeros (9 filas x 4 asientos)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FondoTarjetaStu)
                    .border(1.dp, GrisBorde, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filas.forEachIndexed { indiceFila, fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fila.getOrNull(0)?.let { (numero, estado) ->
                            CeldaAsientoStu(
                                numeroAsiento = numero,
                                estado = estado,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        fila.getOrNull(1)?.let { (numero, estado) ->
                            CeldaAsientoStu(
                                numeroAsiento = numero,
                                estado = estado,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Box(
                            modifier = Modifier.width(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "F${indiceFila + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GrisTextoGuia,
                                textAlign = TextAlign.Center
                            )
                        }

                        fila.getOrNull(2)?.let { (numero, estado) ->
                            CeldaAsientoStu(
                                numeroAsiento = numero,
                                estado = estado,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        fila.getOrNull(3)?.let { (numero, estado) ->
                            CeldaAsientoStu(
                                numeroAsiento = numero,
                                estado = estado,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementoLeyendaAsiento(
    etiqueta: String,
    fondo: Color,
    borde: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(fondo)
                .border(1.5.dp, borde, RoundedCornerShape(4.dp))
        )
        Text(
            text = etiqueta,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrisTextoSubtitulo
        )
    }
}

@Composable
private fun CeldaAsientoStu(
    numeroAsiento: Int,
    estado: EstadoAsiento,
    modifier: Modifier = Modifier
) {
    val fondo = when (estado) {
        EstadoAsiento.DISPONIBLE -> FondoAsientoDisponible
        EstadoAsiento.OCUPADO -> FondoAsientoOcupado
        EstadoAsiento.MI_ASIENTO -> AzulBuapAsiento
    }
    val borde = when (estado) {
        EstadoAsiento.DISPONIBLE -> BordeAsientoDisponible
        EstadoAsiento.OCUPADO -> BordeAsientoOcupado
        EstadoAsiento.MI_ASIENTO -> BordeAzulBuapAsiento
    }
    val grosorBorde = when (estado) {
        EstadoAsiento.DISPONIBLE -> 1.dp
        EstadoAsiento.OCUPADO -> 1.5.dp
        EstadoAsiento.MI_ASIENTO -> 2.dp
    }
    val colorTexto = when (estado) {
        EstadoAsiento.DISPONIBLE -> NegroEtiqueta
        EstadoAsiento.OCUPADO -> TextoError
        EstadoAsiento.MI_ASIENTO -> Color.White
    }

    Column(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .border(grosorBorde, borde, RoundedCornerShape(8.dp))
            .testTag("asiento_$numeroAsiento"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = numeroAsiento.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
        if (estado == EstadoAsiento.MI_ASIENTO) {
            Text(
                text = "TÚ",
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 9.sp
            )
        }
    }
}
