package mx.buap.stu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.theme.AzulOscuroStu
import mx.buap.stu.ui.theme.AzulStu
import mx.buap.stu.ui.theme.NegroEtiqueta
import mx.buap.stu.ui.theme.TemaStu

/**
 * Pantalla 2: Datos del Estudiante (HU-02).
 * Muestra el perfil del estudiante autenticado, su matrícula, correo y saldo de pasajes activos.
 */
@Composable
fun PantallaPerfil(
    usuario: UsuarioSTU,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Pantalla 2: Datos del Estudiante",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AzulOscuroStu,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Alumno:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(usuario.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NegroEtiqueta)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Correo Institucional:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(usuario.correo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NegroEtiqueta)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Matrícula BUAP:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(usuario.matricula, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NegroEtiqueta)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Pasajes STU Disponibles:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("${usuario.pasajesDisponibles} viajes activos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulStu)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroStu)
        ) {
            Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(name = "Pantalla 2 - Perfil Estudiante", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaPerfil() {
    TemaStu {
        PantallaPerfil(
            usuario = UsuarioSTU(
                correo = "Javier.sangines@alumno.buap.mx",
                matricula = "202268046",
                contrasena = "290504",
                nombre = "Javier Sanginés",
                pasajesDisponibles = 10
            ),
            onCerrarSesion = {}
        )
    }
}
