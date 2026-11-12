package mx.buap.stu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AzulStu = Color(0xFF3B82F6)
val AzulOscuroStu = Color(0xFF1E3A8A)
val GrisBorde = Color(0xFFE2E8F0)
val GrisTextoGuia = Color(0xFF94A3B8)
val NegroEtiqueta = Color(0xFF1E293B)
val FondoError = Color(0xFFFEE2E2)
val TextoError = Color(0xFF991B1B)
val FondoExito = Color(0xFFDCFCE7)
val TextoExito = Color(0xFF14532D)

@Composable
fun TemaStu(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}
