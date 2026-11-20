const MENSAJE_SIN_PASAJES_ESTADO = 'Sin pasajes disponibles. Realiza una recarga para viajar';
const MENSAJE_CON_PASAJES_ESTADO = 'Saldo disponible para consultar rutas y reservar viajes';
const MENSAJE_SIN_PASAJES_SALIDA = 'Necesitas al menos un pasaje disponible para seleccionar una salida';
const MENSAJE_SALIDA_PERMITIDA = 'Puedes consultar rutas y seleccionar una salida disponible';

function obtenerPasajesSanitizados(usuario) {
  const valorBruto = usuario && (usuario.pasajesDisponibles ?? usuario.saldoPasajes ?? 0);
  const numero = Number.isFinite(Number(valorBruto)) ? Math.trunc(Number(valorBruto)) : 0;
  return Math.max(0, numero);
}

/**
 * Evalúa el saldo de pasajes de un estudiante STU BUAP (HU-02).
 */
function evaluarSaldo(usuario) {
  const pasajesDisponibles = obtenerPasajesSanitizados(usuario);
  const etiquetaSaldo = pasajesDisponibles === 1
    ? '1 viaje disponible'
    : `${pasajesDisponibles} viajes disponibles`;
  const puedeReservarViaje = pasajesDisponibles >= 1;
  const requiereRecarga = pasajesDisponibles === 0;
  const mensajeEstado = requiereRecarga
    ? MENSAJE_SIN_PASAJES_ESTADO
    : MENSAJE_CON_PASAJES_ESTADO;

  return {
    pasajesDisponibles,
    etiquetaSaldo,
    puedeReservarViaje,
    requiereRecarga,
    mensajeEstado
  };
}

/**
 * Determina si el estudiante puede seleccionar una salida o requiere recarga (HU-02 / HU-06).
 */
function intentarSeleccionarSalida(usuario) {
  const estado = evaluarSaldo(usuario);
  if (estado.puedeReservarViaje) {
    return {
      permitido: true,
      redirigirARecarga: false,
      mensaje: MENSAJE_SALIDA_PERMITIDA
    };
  }
  return {
    permitido: false,
    redirigirARecarga: true,
    mensaje: MENSAJE_SIN_PASAJES_SALIDA
  };
}

module.exports = {
  MENSAJE_SIN_PASAJES_ESTADO,
  MENSAJE_CON_PASAJES_ESTADO,
  MENSAJE_SIN_PASAJES_SALIDA,
  MENSAJE_SALIDA_PERMITIDA,
  evaluarSaldo,
  intentarSeleccionarSalida
};
