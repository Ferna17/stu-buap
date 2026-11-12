package mx.buap.stu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.ui.theme.*

/**
 * Logo circular institucional azul "STU" compartido por Ingreso y Registro.
 */
@Composable
fun LogoCircularStu() {
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
}

/**
 * Banner de retroalimentación de validación (éxito o error) compartido por los formularios de autenticación.
 */
@Composable
fun BannerResultadoStu(
    esExito: Boolean,
    mensaje: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (esExito) FondoExito else FondoError)
            .padding(12.dp)
    ) {
        Text(
            text = mensaje,
            color = if (esExito) TextoExito else TextoError,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Panel compacto de métricas no funcionales en vivo compartido entre Login y Registro.
 */
@Composable
fun PanelMetricasStu(
    tiempoRespuestaMs: String,
    tiempoCapturaSeg: String,
    erroresDetectados: Int
) {
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
            text = "• Tiempo de respuesta del validador: $tiempoRespuestaMs ms",
            fontSize = 11.sp,
            color = Color(0xFF475569)
        )
        Text(
            text = "• Tiempo de captura del usuario: $tiempoCapturaSeg s",
            fontSize = 11.sp,
            color = Color(0xFF475569)
        )
        Text(
            text = "• Errores de captura detectados: $erroresDetectados",
            fontSize = 11.sp,
            color = Color(0xFF475569)
        )
    }
}
