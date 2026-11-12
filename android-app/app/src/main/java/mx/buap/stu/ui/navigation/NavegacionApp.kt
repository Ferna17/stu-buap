package mx.buap.stu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.screens.PantallaIngreso
import mx.buap.stu.ui.screens.PantallaPerfil
import mx.buap.stu.ui.viewmodels.IngresoViewModel

object DestinosStu {
    const val INGRESO = "ingreso"
    const val PERFIL = "perfil"
}

/**
 * Grafo central de navegación con Navigation Compose.
 * Controla el flujo entre pantallas desacoplando las transiciones del código visual.
 */
@Composable
fun NavegacionApp(
    repositorio: RepositorioUsuario
) {
    val navController = rememberNavController()
    var usuarioActivo by remember { mutableStateOf<UsuarioSTU?>(null) }

    NavHost(
        navController = navController,
        startDestination = DestinosStu.INGRESO
    ) {
        composable(DestinosStu.INGRESO) {
            val ingresoViewModel: IngresoViewModel = viewModel(
                factory = IngresoViewModel.Factory(repositorio)
            )

            PantallaIngreso(
                viewModel = ingresoViewModel,
                onLoginExitoso = { usuario ->
                    usuarioActivo = usuario
                    navController.navigate(DestinosStu.PERFIL) {
                        popUpTo(DestinosStu.INGRESO) { inclusive = true }
                    }
                }
            )
        }

        composable(DestinosStu.PERFIL) {
            usuarioActivo?.let { usuario ->
                PantallaPerfil(
                    usuario = usuario,
                    onCerrarSesion = {
                        usuarioActivo = null
                        navController.navigate(DestinosStu.INGRESO) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
