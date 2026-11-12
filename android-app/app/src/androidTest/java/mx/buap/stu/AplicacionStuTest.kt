package mx.buap.stu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

        // Verifica que la interfaz de inicio muestre los elementos clave
        composeTestRule.onNodeWithText("STU", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Correo electrónico", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matrícula", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña", substring = true).assertIsDisplayed()
    }
}
