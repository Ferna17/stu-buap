package mx.buap.stu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.ui.navigation.NavegacionApp
import mx.buap.stu.ui.theme.TemaStu

class MainActivity : ComponentActivity() {

    // Repositorio de datos compartido en el ciclo de vida de la actividad
    // Listo para conectarse con Room en el Sprint 2
    private val repositorioUsuario = RepositorioUsuarioMemoria()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaStu {
                NavegacionApp(repositorio = repositorioUsuario)
            }
        }
    }
}
