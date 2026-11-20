package mx.buap.stu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import mx.buap.stu.data.RepositorioUsuarioMemoria
import mx.buap.stu.ui.navigation.NavegacionApp
import mx.buap.stu.ui.theme.TemaStu
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AplicacionStuTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verificarPantallaIngresoMuestraCamposPrincipales() {
        composeTestRule.setContent {
            TemaStu {
                NavegacionApp(repositorio = RepositorioUsuarioMemoria())
            }
        }

        composeTestRule.onNodeWithText("STU", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo electrónico", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrícula", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("¿No estás registrado? Regístrate")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun verificarNavegacionBidireccionalEntreIngresoYRegistro() {
        composeTestRule.setContent {
            TemaStu {
                NavegacionApp(repositorio = RepositorioUsuarioMemoria())
            }
        }

        composeTestRule.onNodeWithText("¿No estás registrado? Regístrate")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Registro de usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre(s)", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Apellidos", substring = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("¿Ya tienes cuenta? Inicia sesión")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Inicio").assertIsDisplayed()
    }

    @Test
    fun verificarNavegacionYRegistroExitosoConNombreYSaldoCero() {
        composeTestRule.setContent {
            TemaStu {
                NavegacionApp(repositorio = RepositorioUsuarioMemoria())
            }
        }

        composeTestRule.onNodeWithText("¿No estás registrado? Regístrate")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Registro de usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrarse").performScrollTo().performClick()
        composeTestRule.onNodeWithText(
            "Completa tu nombre, apellidos, correo, matrícula y contraseña para registrarte"
        ).performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("Ingresa tu(s) nombre(s)")
            .performScrollTo()
            .performTextInput("Fernando Jesús")
        composeTestRule.onNodeWithText("Ingresa tus apellidos")
            .performScrollTo()
            .performTextInput("Hernández Gorgua")
        composeTestRule.onNodeWithText("nombre@alumno.buap.mx")
            .performScrollTo()
            .performTextInput("fernando.hernandez@alumno.buap.mx")
        composeTestRule.onNodeWithText("Ingresa tu matrícula")
            .performScrollTo()
            .performTextInput("202112345")
        composeTestRule.onNodeWithText("Ingresa tu contraseña (8 a 20 caracteres)")
            .performScrollTo()
            .performTextInput("Fernando2026")
        composeTestRule.onNodeWithText("Registrarse").performScrollTo().performClick()

        composeTestRule.onNodeWithText(
            "¡Registro exitoso! Ya puedes ingresar al STU con tu cuenta."
        ).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Inicio").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Inicio STU").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fernando Jesús Hernández Gorgua").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 viajes disponibles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin pasajes disponibles. Realiza una recarga para viajar")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("fernando.hernandez@alumno.buap.mx").assertDoesNotExist()
        composeTestRule.onNodeWithText("202112345").assertDoesNotExist()
        composeTestRule.onNodeWithText("ACTIVO").assertDoesNotExist()
        composeTestRule.onNodeWithText("Recargar pasajes").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado de mis pagos").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Rutas y salidas").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Mis boletos").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Cerrar sesión").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("Rutas y salidas").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Necesitas al menos 1 pasaje disponible para seleccionar una salida")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("dialogo_btn_recargar").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText(
            "El módulo \"Recargar pasajes\" (SPEI / CoDi) se habilitará en el siguiente sprint (HU-03 / HU-04)."
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun verificarFlujoLoginSinRegistroPrevioRedirigeARegistroPrefill() {
        composeTestRule.setContent {
            TemaStu {
                NavegacionApp(repositorio = RepositorioUsuarioMemoria())
            }
        }

        composeTestRule.onNodeWithText("nombre@ejemplo.com")
            .performTextInput("nuevo.alumno@alumno.buap.mx")
        composeTestRule.onNodeWithText("Ingresa tu matrícula")
            .performTextInput("202512345")
        composeTestRule.onNodeWithText("Ingresa tu contraseña (8 a 20 caracteres)")
            .performTextInput("Alumno2026")
        composeTestRule.onNodeWithText("Inicio").performScrollTo().performClick()

        composeTestRule.onNodeWithText("No estás registrado, debes registrarte", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Registrarse en el padrón STU")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Registro de usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingresa tu(s) nombre(s)")
            .performScrollTo()
            .performTextInput("Ana Sofía")
        composeTestRule.onNodeWithText("Ingresa tus apellidos")
            .performScrollTo()
            .performTextInput("Morales López")
        composeTestRule.onNodeWithText("Registrarse").performScrollTo().performClick()

        composeTestRule.onNodeWithText(
            "¡Registro exitoso! Ya puedes ingresar al STU con tu cuenta."
        ).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Inicio").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Inicio STU").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ana Sofía Morales López").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 viajes disponibles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin pasajes disponibles. Realiza una recarga para viajar")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("nuevo.alumno@alumno.buap.mx").assertDoesNotExist()
        composeTestRule.onNodeWithText("202512345").assertDoesNotExist()
        composeTestRule.onNodeWithText("ACTIVO").assertDoesNotExist()
        composeTestRule.onNodeWithText("Rutas y salidas").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Necesitas al menos 1 pasaje disponible para seleccionar una salida")
            .assertIsDisplayed()
    }
}
