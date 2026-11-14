package mx.buap.stu.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.buap.stu.data.RepositorioUsuario
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.data.UsuarioSTU
import mx.buap.stu.ui.screens.PantallaIngreso
import mx.buap.stu.ui.screens.PantallaPerfil
import mx.buap.stu.ui.screens.PantallaRegistro
import mx.buap.stu.ui.theme.TemaStu
import mx.buap.stu.ui.viewmodels.IngresoViewModel
import mx.buap.stu.ui.viewmodels.RegistroViewModel

object DestinosStu {
    const val INGRESO = "ingreso"
    const val REGISTRO = "registro"
    const val PERFIL = "perfil"
}

private data class DatosRegistroIniciales(
    val correo: String = "",
    val matricula: String = "",
    val contrasena: String = ""
)

/**
 * Grafo central de navegación con Navigation Compose.
 * Controla el flujo animado entre Ingreso, Registro y Perfil con persistencia compartida.
 */
@Composable
fun NavegacionApp(
    repositorio: RepositorioUsuario
) {
    val navController = rememberNavController()
    var usuarioActivo by remember { mutableStateOf<UsuarioSTU?>(null) }
    var datosPrecargadosRegistro by remember { mutableStateOf(DatosRegistroIniciales()) }

    // El ViewModel de Ingreso se mantiene a nivel del grafo de navegación
    // para retener el estado al transitar hacia Registro y volver con éxito (Flujo B).
    val ingresoViewModel: IngresoViewModel = viewModel(
        factory = IngresoViewModel.Factory(repositorio)
    )

    NavHost(
        navController = navController,
        startDestination = DestinosStu.INGRESO
    ) {
        composable(
            route = DestinosStu.INGRESO,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            PantallaIngreso(
                viewModel = ingresoViewModel,
                onLoginExitoso = { usuario ->
                    usuarioActivo = usuario
                    navController.navigate(DestinosStu.PERFIL) {
                        popUpTo(DestinosStu.INGRESO) { inclusive = true }
                    }
                },
                onIrARegistro = { precargar ->
                    ingresoViewModel.limpiarErrorPrevio()
                    datosPrecargadosRegistro = if (precargar) {
                        DatosRegistroIniciales(
                            correo = ingresoViewModel.correo,
                            matricula = ingresoViewModel.matricula,
                            contrasena = ingresoViewModel.contrasena
                        )
                    } else {
                        DatosRegistroIniciales()
                    }
                    navController.navigate(DestinosStu.REGISTRO) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = DestinosStu.REGISTRO,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            val registroViewModel: RegistroViewModel = viewModel(
                factory = RegistroViewModel.Factory(repositorio)
            )

            LaunchedEffect(datosPrecargadosRegistro) {
                registroViewModel.precargarDatos(
                    correoInicial = datosPrecargadosRegistro.correo,
                    matriculaInicial = datosPrecargadosRegistro.matricula,
                    contrasenaInicial = datosPrecargadosRegistro.contrasena
                )
            }

            PantallaRegistro(
                viewModel = registroViewModel,
                onRegistroExitoso = { nuevoUsuario ->
                    datosPrecargadosRegistro = DatosRegistroIniciales()
                    // Flujo B: Retornar a Login con credenciales listas y aviso de éxito
                    ingresoViewModel.aplicarRegistroExitoso(nuevoUsuario)
                    navController.popBackStack(DestinosStu.INGRESO, inclusive = false)
                },
                onVolverALogin = {
                    datosPrecargadosRegistro = DatosRegistroIniciales()
                    val regresoALogin = navController.popBackStack(
                        DestinosStu.INGRESO,
                        inclusive = false
                    )
                    if (!regresoALogin) {
                        navController.navigate(DestinosStu.INGRESO) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(
            route = DestinosStu.PERFIL,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
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

@Preview(name = "Flujo Completo Interactivo STU (Login <-> Registro <-> Perfil)", showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaFlujoCompletoAuth() {
    TemaStu {
        val repo = RepositorioUsuarioMemoria()
        NavegacionApp(repositorio = repo)
    }
}
