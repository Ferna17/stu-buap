package mx.buap.stu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import mx.buap.stu.data.RepositorioSalidasMemoria
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.ui.navigation.NavegacionApp
import mx.buap.stu.ui.theme.TemaStu

class MainActivity : ComponentActivity() {

    // Repositorios de datos compartidos en el ciclo de vida de la actividad
    // Listos para conectarse con Room en futuros sprints
    private val repositorioUsuario = RepositorioUsuarioMemoria()
    private val repositorioSalidas = RepositorioSalidasMemoria()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaStu {
                NavegacionApp(
                    repositorio = repositorioUsuario,
                    repositorioSalidas = repositorioSalidas
                )
            }
        }
    }
}
