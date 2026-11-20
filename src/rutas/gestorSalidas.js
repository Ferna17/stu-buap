const { intentarSeleccionarSalida } = require('../saldo/evaluadorSaldo');

const CAPACIDAD_AUTOBUS_STU = 36;

const EstadoSalida = {
  DISPONIBLE: 'DISPONIBLE',
  PROXIMO: 'PROXIMO',
  LLENO: 'LLENO'
};

const EstadoAsiento = {
  DISPONIBLE: 'DISPONIBLE',
  OCUPADO: 'OCUPADO',
  MI_ASIENTO: 'MI_ASIENTO'
};

const MENSAJE_SALIDA_PROXIMA = 'Este autobús es próximo y aún tiene hora por confirmar';
const MENSAJE_ASIENTO_DUPLICADO = 'Ya tienes un asiento asignado en esta salida';
const MENSAJE_AUTOBUS_LLENO = 'El autobús se encuentra lleno (36 de 36 asientos ocupados)';
const MENSAJE_MATRICULA_INVALIDA = 'No se pudo identificar la matrícula del estudiante para asignar un asiento';

const CATALOGO_RUTAS_OFICIAL = [
  { id: 'CU-CU2', nombre: 'CU-CU2', origen: 'Ciudad Universitaria', destino: 'CU2 Ecocampus Valsequillo' },
  { id: 'CU2-CU', nombre: 'CU2-CU', origen: 'CU2 Ecocampus Valsequillo', destino: 'Ciudad Universitaria' },
  { id: 'CU-Amalucan', nombre: 'CU-Amalucan', origen: 'Ciudad Universitaria', destino: 'Amalucan' },
  { id: 'CU-CAPU', nombre: 'CU-CAPU', origen: 'Ciudad Universitaria', destino: 'Terminal CAPU' },
  { id: 'CU-Cuautlancingo', nombre: 'CU-Cuautlancingo', origen: 'Ciudad Universitaria', destino: 'Cuautlancingo' },
  { id: 'CU-Maravillas', nombre: 'CU-Maravillas', origen: 'Ciudad Universitaria', destino: 'Maravillas' },
  { id: 'CU-San Ramón', nombre: 'CU-San Ramón', origen: 'Ciudad Universitaria', destino: 'San Ramón' },
  { id: 'CU-Héroes', nombre: 'CU-Héroes', origen: 'Ciudad Universitaria', destino: 'Los Héroes' },
  { id: 'CU-Tlaxcalancingo', nombre: 'CU-Tlaxcalancingo', origen: 'Ciudad Universitaria', destino: 'Tlaxcalancingo' }
];

function generarOcupacionInicial(cantidad, prefijoMatricula) {
  const mapa = {};
  const limite = Math.max(0, Math.min(CAPACIDAD_AUTOBUS_STU, cantidad));
  for (let i = 1; i <= limite; i++) {
    mapa[i] = `${prefijoMatricula}${1000 + i}`;
  }
  return mapa;
}

function crearSalidaAutobus({
  id,
  rutaId,
  nombreRuta,
  horaSalida,
  anden,
  estadoBase,
  asientosOcupadosPorMatricula = {}
}) {
  const copiaAsientos = { ...asientosOcupadosPorMatricula };
  const totalOcupados = Object.keys(copiaAsientos)
    .map(Number)
    .filter((n) => n >= 1 && n <= CAPACIDAD_AUTOBUS_STU).length;
  const totalDisponibles = Math.max(0, CAPACIDAD_AUTOBUS_STU - totalOcupados);

  let estadoActual = EstadoSalida.DISPONIBLE;
  if (estadoBase === EstadoSalida.PROXIMO) {
    estadoActual = EstadoSalida.PROXIMO;
  } else if (estadoBase === EstadoSalida.LLENO || totalOcupados >= CAPACIDAD_AUTOBUS_STU) {
    estadoActual = EstadoSalida.LLENO;
  }

  return {
    id,
    rutaId,
    nombreRuta,
    horaSalida,
    anden,
    estadoBase,
    asientosOcupadosPorMatricula: copiaAsientos,
    totalOcupados,
    totalDisponibles,
    estadoActual
  };
}

function obtenerMapaAsientos(salida, matriculaActual) {
  const matriculaLimpia = typeof matriculaActual === 'string' && matriculaActual.trim().length > 0
    ? matriculaActual.trim().toLowerCase()
    : null;
  const mapa = {};
  for (let asiento = 1; asiento <= CAPACIDAD_AUTOBUS_STU; asiento++) {
    const titular = salida.asientosOcupadosPorMatricula[asiento];
    if (!titular) {
      mapa[asiento] = EstadoAsiento.DISPONIBLE;
    } else if (matriculaLimpia && String(titular).trim().toLowerCase() === matriculaLimpia) {
      mapa[asiento] = EstadoAsiento.MI_ASIENTO;
    } else {
      mapa[asiento] = EstadoAsiento.OCUPADO;
    }
  }
  return mapa;
}

function buscarAsientoDeMatricula(salida, matricula) {
  const matriculaLimpia = typeof matricula === 'string' ? matricula.trim().toLowerCase() : '';
  if (!matriculaLimpia) return null;
  for (const [asiento, titular] of Object.entries(salida.asientosOcupadosPorMatricula)) {
    if (String(titular).trim().toLowerCase() === matriculaLimpia) {
      return Number(asiento);
    }
  }
  return null;
}

function confirmarSalidaYAsignarAsiento(usuario, salida) {
  const validacionSaldo = intentarSeleccionarSalida(usuario);
  if (!validacionSaldo.permitido) {
    return {
      ok: false,
      mensaje: validacionSaldo.mensaje,
      redirigirARecarga: true,
      reserva: null,
      salidaActualizada: null
    };
  }

  const matriculaLimpia = typeof usuario?.matricula === 'string' ? usuario.matricula.trim() : '';
  if (!matriculaLimpia) {
    return {
      ok: false,
      mensaje: MENSAJE_MATRICULA_INVALIDA,
      redirigirARecarga: false,
      reserva: null,
      salidaActualizada: null
    };
  }

  if (salida.estadoActual === EstadoSalida.PROXIMO) {
    return {
      ok: false,
      mensaje: MENSAJE_SALIDA_PROXIMA,
      redirigirARecarga: false,
      reserva: null,
      salidaActualizada: null
    };
  }

  if (buscarAsientoDeMatricula(salida, matriculaLimpia) !== null) {
    return {
      ok: false,
      mensaje: MENSAJE_ASIENTO_DUPLICADO,
      redirigirARecarga: false,
      reserva: null,
      salidaActualizada: null
    };
  }

  if (salida.estadoActual === EstadoSalida.LLENO) {
    return {
      ok: false,
      mensaje: MENSAJE_AUTOBUS_LLENO,
      redirigirARecarga: false,
      reserva: null,
      salidaActualizada: null
    };
  }

  let asientoLibre = null;
  for (let asiento = 1; asiento <= CAPACIDAD_AUTOBUS_STU; asiento++) {
    if (!salida.asientosOcupadosPorMatricula[asiento]) {
      asientoLibre = asiento;
      break;
    }
  }

  if (asientoLibre === null) {
    return {
      ok: false,
      mensaje: MENSAJE_AUTOBUS_LLENO,
      redirigirARecarga: false,
      reserva: null,
      salidaActualizada: null
    };
  }

  const nuevoMapa = {
    ...salida.asientosOcupadosPorMatricula,
    [asientoLibre]: matriculaLimpia
  };

  const salidaActualizada = crearSalidaAutobus({
    ...salida,
    asientosOcupadosPorMatricula: nuevoMapa
  });

  const reserva = {
    salidaId: salida.id,
    nombreRuta: salida.nombreRuta,
    horaSalida: salida.horaSalida,
    anden: salida.anden,
    asientoAsignado: asientoLibre,
    matriculaEstudiante: matriculaLimpia
  };

  return {
    ok: true,
    mensaje: `Salida confirmada. Tu asiento asignado es el ${asientoLibre} de ${CAPACIDAD_AUTOBUS_STU}.`,
    redirigirARecarga: false,
    reserva,
    salidaActualizada
  };
}

class RepositorioSalidasMemoria {
  constructor() {
    this.rutas = [...CATALOGO_RUTAS_OFICIAL];
    this.salidas = [
      crearSalidaAutobus({
        id: 'cu-cu2-1430',
        rutaId: 'CU-CU2',
        nombreRuta: 'CU-CU2',
        horaSalida: '14:30',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.DISPONIBLE,
        asientosOcupadosPorMatricula: generarOcupacionInicial(17, '20201')
      }),
      crearSalidaAutobus({
        id: 'cu-cu2-1500',
        rutaId: 'CU-CU2',
        nombreRuta: 'CU-CU2',
        horaSalida: '15:00',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.DISPONIBLE,
        asientosOcupadosPorMatricula: generarOcupacionInicial(35, '20202')
      }),
      crearSalidaAutobus({
        id: 'cu-cu2-1400',
        rutaId: 'CU-CU2',
        nombreRuta: 'CU-CU2',
        horaSalida: '14:00',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.LLENO,
        asientosOcupadosPorMatricula: generarOcupacionInicial(36, '20203')
      }),
      crearSalidaAutobus({
        id: 'cu-cu2-proximo',
        rutaId: 'CU-CU2',
        nombreRuta: 'CU-CU2',
        horaSalida: 'Hora por confirmar',
        anden: 'Andén por asignar',
        estadoBase: EstadoSalida.PROXIMO
      }),
      crearSalidaAutobus({
        id: 'cu-amalucan-1430',
        rutaId: 'CU-Amalucan',
        nombreRuta: 'CU-Amalucan',
        horaSalida: '14:30',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.DISPONIBLE,
        asientosOcupadosPorMatricula: generarOcupacionInicial(17, '20205')
      }),
      crearSalidaAutobus({
        id: 'cu-amalucan-1500',
        rutaId: 'CU-Amalucan',
        nombreRuta: 'CU-Amalucan',
        horaSalida: '15:00',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.DISPONIBLE,
        asientosOcupadosPorMatricula: generarOcupacionInicial(35, '20206')
      }),
      crearSalidaAutobus({
        id: 'cu-amalucan-1400',
        rutaId: 'CU-Amalucan',
        nombreRuta: 'CU-Amalucan',
        horaSalida: '14:00',
        anden: 'Andén 3',
        estadoBase: EstadoSalida.LLENO,
        asientosOcupadosPorMatricula: generarOcupacionInicial(36, '20207')
      }),
      crearSalidaAutobus({
        id: 'cu-amalucan-proximo',
        rutaId: 'CU-Amalucan',
        nombreRuta: 'CU-Amalucan',
        horaSalida: 'Hora por confirmar',
        anden: 'Andén por asignar',
        estadoBase: EstadoSalida.PROXIMO
      })
    ];
  }

  obtenerRutas() {
    return [...this.rutas];
  }

  obtenerSalidasPorRuta(rutaId) {
    const rutaLimpia = String(rutaId || '').trim().toLowerCase();
    return this.salidas.filter((s) => s.rutaId.toLowerCase() === rutaLimpia);
  }

  buscarSalidaPorId(salidaId) {
    return this.salidas.find((s) => s.id === String(salidaId || '').trim()) || null;
  }

  confirmarReserva(usuario, salidaId) {
    const salidaActual = this.buscarSalidaPorId(salidaId);
    if (!salidaActual) {
      return {
        ok: false,
        mensaje: 'No se encontró la salida seleccionada',
        redirigirARecarga: false,
        reserva: null,
        salidaActualizada: null
      };
    }

    const resultado = confirmarSalidaYAsignarAsiento(usuario, salidaActual);
    if (resultado.ok && resultado.salidaActualizada) {
      this.salidas = this.salidas.map((s) =>
        s.id === resultado.salidaActualizada.id ? resultado.salidaActualizada : s
      );
    }
    return resultado;
  }
}

module.exports = {
  CAPACIDAD_AUTOBUS_STU,
  EstadoSalida,
  EstadoAsiento,
  MENSAJE_SALIDA_PROXIMA,
  MENSAJE_ASIENTO_DUPLICADO,
  MENSAJE_AUTOBUS_LLENO,
  MENSAJE_MATRICULA_INVALIDA,
  CATALOGO_RUTAS_OFICIAL,
  crearSalidaAutobus,
  obtenerMapaAsientos,
  buscarAsientoDeMatricula,
  confirmarSalidaYAsignarAsiento,
  RepositorioSalidasMemoria
};
