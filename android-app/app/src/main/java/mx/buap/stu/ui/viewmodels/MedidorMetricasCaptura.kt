package mx.buap.stu.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Encapsula la captura y formateo de métricas no funcionales en vivo (tiempo de respuesta,
 * tiempo de captura y conteo de errores) para los formularios de autenticación STU.
 */
class MedidorMetricasCaptura {
    var tiempoRespuestaMs by mutableStateOf("0.00")
        private set

    var tiempoCapturaSeg by mutableStateOf("0.0")
        private set

    var erroresDetectados by mutableIntStateOf(0)
        private set

    private var inicioCapturaNs: Long? = null

    fun iniciarTemporizador() {
        if (inicioCapturaNs == null) {
            inicioCapturaNs = System.nanoTime()
        }
    }

    inline fun <T> medir(
        crossinline esExitoso: (T) -> Boolean,
        bloque: () -> T
    ): T {
        val t0 = System.nanoTime()
        val resultado = bloque()
        val t1 = System.nanoTime()
        registrarResultado(t0, t1, esExitoso(resultado))
        return resultado
    }

    fun registrarResultado(t0Ns: Long, t1Ns: Long, exitoso: Boolean) {
        val duracionMs = (t1Ns - t0Ns) / 1_000_000.0
        tiempoRespuestaMs = String.format(Locale.US, "%.2f", duracionMs)

        inicioCapturaNs?.let { inicio ->
            val segundos = (t1Ns - inicio) / 1_000_000_000.0
            tiempoCapturaSeg = String.format(Locale.US, "%.1f", segundos)
        }
        inicioCapturaNs = null

        if (!exitoso) {
            erroresDetectados++
        }
    }
}
