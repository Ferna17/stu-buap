const { MENSAJE_SIN_PASAJES_SALIDA } = require('../../src/saldo/evaluadorSaldo');
const {
  EstadoSalida,
  EstadoAsiento,
  MENSAJE_ASIENTO_DUPLICADO,
  crearSalidaAutobus,
  obtenerMapaAsientos,
  buscarAsientoDeMatricula,
  confirmarSalidaYAsignarAsiento,
  RepositorioSalidasMemoria
} = require('../../src/rutas/gestorSalidas');

function crearUsuarioConSaldo(matricula = '202345678', pasajes = 10) {
  return {
    email: `estudiante.${matricula}@alumno.buap.mx`,
    matricula,
    nombre: 'Estudiante STU',
    pasajesDisponibles: pasajes,
    estado: 'ACTIVO'
  };
}

function crearOcupacionParcial(cantidad) {
  const mapa = {};
  for (let i = 1; i <= cantidad; i++) {
    mapa[i] = `2019${10000 + i}`;
  }
  return mapa;
}

describe('Módulo de Rutas, Horarios y Mapa de 36 Asientos STU (HU-06 - BICEP/CORRECT)', () => {

  test('PRU1: Consultar horarios de una ruta existente con estados DISPONIBLE, PROXIMO y LLENO', () => {
    const repositorio = new RepositorioSalidasMemoria();
    expect(repositorio.obtenerRutas()).toHaveLength(9);

    const salidasAmalucan = repositorio.obtenerSalidasPorRuta('CU-Amalucan');
    const salidasCu2 = repositorio.obtenerSalidasPorRuta('CU-CU2');

    expect(salidasAmalucan.some((s) => s.estadoActual === EstadoSalida.DISPONIBLE)).toBe(true);
    expect(
      salidasAmalucan.some(
        (s) => s.estadoActual === EstadoSalida.PROXIMO && s.horaSalida === 'Hora por confirmar'
      )
    ).toBe(true);
    expect(salidasAmalucan.some((s) => s.estadoActual === EstadoSalida.LLENO)).toBe(true);

    expect(salidasCu2.some((s) => s.estadoActual === EstadoSalida.DISPONIBLE)).toBe(true);
    expect(
      salidasCu2.some(
        (s) => s.estadoActual === EstadoSalida.PROXIMO && s.horaSalida === 'Hora por confirmar'
      )
    ).toBe(true);
  });

  test('PRU2: Reservar con saldo >= 1 asignando y bloqueando el menor asiento libre', () => {
    const usuario = crearUsuarioConSaldo('202345678', 10);
    const salida = crearSalidaAutobus({
      id: 'cu-amalucan-1430',
      rutaId: 'CU-Amalucan',
      nombreRuta: 'CU-Amalucan',
      horaSalida: '14:30',
      anden: 'Andén 3',
      estadoBase: EstadoSalida.DISPONIBLE,
      asientosOcupadosPorMatricula: crearOcupacionParcial(17)
    });

    const resultado = confirmarSalidaYAsignarAsiento(usuario, salida);

    expect(resultado.ok).toBe(true);
    expect(resultado.redirigirARecarga).toBe(false);
    expect(resultado.reserva).not.toBeNull();
    expect(resultado.reserva.asientoAsignado).toBe(18);
    expect(resultado.reserva.matriculaEstudiante).toBe('202345678');

    const salidaActualizada = resultado.salidaActualizada;
    expect(salidaActualizada.totalOcupados).toBe(18);
    expect(salidaActualizada.totalDisponibles).toBe(18);
    expect(salidaActualizada.asientosOcupadosPorMatricula[18]).toBe('202345678');

    const mapaTitular = obtenerMapaAsientos(salidaActualizada, '202345678');
    expect(mapaTitular[18]).toBe(EstadoAsiento.MI_ASIENTO);
    expect(mapaTitular[19]).toBe(EstadoAsiento.DISPONIBLE);
  });

  test('PRU3: Reservar el último asiento disponible (35/36) y cambiar estado a LLENO', () => {
    const usuario = crearUsuarioConSaldo('202345678', 5);
    const salida = crearSalidaAutobus({
      id: 'cu-amalucan-1500',
      rutaId: 'CU-Amalucan',
      nombreRuta: 'CU-Amalucan',
      horaSalida: '15:00',
      anden: 'Andén 3',
      estadoBase: EstadoSalida.DISPONIBLE,
      asientosOcupadosPorMatricula: crearOcupacionParcial(35)
    });

    const resultado = confirmarSalidaYAsignarAsiento(usuario, salida);

    expect(resultado.ok).toBe(true);
    expect(resultado.reserva.asientoAsignado).toBe(36);
    expect(resultado.salidaActualizada.totalOcupados).toBe(36);
    expect(resultado.salidaActualizada.totalDisponibles).toBe(0);
    expect(resultado.salidaActualizada.estadoActual).toBe(EstadoSalida.LLENO);
  });

  test('PRU4: Rechazar reserva en autobús lleno (36/36) sin alterar mapa ni contadores', () => {
    const usuario = crearUsuarioConSaldo('202345678', 10);
    const salidaLlena = crearSalidaAutobus({
      id: 'cu-amalucan-1400',
      rutaId: 'CU-Amalucan',
      nombreRuta: 'CU-Amalucan',
      horaSalida: '14:00',
      anden: 'Andén 3',
      estadoBase: EstadoSalida.LLENO,
      asientosOcupadosPorMatricula: crearOcupacionParcial(36)
    });

    const resultado = confirmarSalidaYAsignarAsiento(usuario, salidaLlena);

    expect(resultado.ok).toBe(false);
    expect(resultado.redirigirARecarga).toBe(false);
    expect(resultado.reserva).toBeNull();
    expect(resultado.salidaActualizada).toBeNull();
    expect(salidaLlena.totalOcupados).toBe(36);
    expect(salidaLlena.totalDisponibles).toBe(0);
    expect(buscarAsientoDeMatricula(salidaLlena, '202345678')).toBeNull();
  });

  test('PRU5: Rechazar reserva con 0 pasajes y redirigir a recarga sin bloquear asientos', () => {
    const usuarioSinSaldo = crearUsuarioConSaldo('202500001', 0);
    const repositorio = new RepositorioSalidasMemoria();

    const resultado = repositorio.confirmarReserva(usuarioSinSaldo, 'cu-amalucan-1430');
    const salidaTrasIntento = repositorio.buscarSalidaPorId('cu-amalucan-1430');

    expect(resultado.ok).toBe(false);
    expect(resultado.redirigirARecarga).toBe(true);
    expect(resultado.mensaje).toBe(MENSAJE_SIN_PASAJES_SALIDA);
    expect(resultado.reserva).toBeNull();
    expect(salidaTrasIntento.totalOcupados).toBe(17);
    expect(salidaTrasIntento.totalDisponibles).toBe(19);
    expect(buscarAsientoDeMatricula(salidaTrasIntento, '202500001')).toBeNull();
  });

  test('PRU6: Rechazar selección de salida PROXIMO con hora por confirmar', () => {
    const usuario = crearUsuarioConSaldo('202345678', 10);
    const salidaProxima = crearSalidaAutobus({
      id: 'cu-amalucan-proximo',
      rutaId: 'CU-Amalucan',
      nombreRuta: 'CU-Amalucan',
      horaSalida: 'Hora por confirmar',
      anden: 'Andén por asignar',
      estadoBase: EstadoSalida.PROXIMO
    });

    const resultado = confirmarSalidaYAsignarAsiento(usuario, salidaProxima);

    expect(resultado.ok).toBe(false);
    expect(resultado.redirigirARecarga).toBe(false);
    expect(resultado.mensaje.toLowerCase()).toContain('hora por confirmar');
    expect(resultado.reserva).toBeNull();
    expect(salidaProxima.totalOcupados).toBe(0);
  });

  test('PRU7: Evitar doble reserva de la misma matrícula en la misma salida', () => {
    const repositorio = new RepositorioSalidasMemoria();
    const usuario = crearUsuarioConSaldo('202345678', 10);

    const primerResultado = repositorio.confirmarReserva(usuario, 'cu-amalucan-1430');
    expect(primerResultado.ok).toBe(true);
    expect(primerResultado.reserva.asientoAsignado).toBe(18);

    const segundoResultado = repositorio.confirmarReserva(usuario, 'cu-amalucan-1430');
    const salidaFinal = repositorio.buscarSalidaPorId('cu-amalucan-1430');

    expect(segundoResultado.ok).toBe(false);
    expect(segundoResultado.redirigirARecarga).toBe(false);
    expect(segundoResultado.mensaje).toBe(MENSAJE_ASIENTO_DUPLICADO);
    expect(salidaFinal.totalOcupados).toBe(18);
    expect(buscarAsientoDeMatricula(salidaFinal, '202345678')).toBe(18);
  });

  test('PRU8: Garantizar consistencia en tiempo real entre dos usuarios en el repositorio', () => {
    const repositorio = new RepositorioSalidasMemoria();
    const alumnoA = crearUsuarioConSaldo('202345678', 10);
    const alumnoB = crearUsuarioConSaldo('202311111', 4);

    const reservaA = repositorio.confirmarReserva(alumnoA, 'cu-amalucan-1430');
    expect(reservaA.ok).toBe(true);
    expect(reservaA.reserva.asientoAsignado).toBe(18);

    const salidaVistaPorB = repositorio.buscarSalidaPorId('cu-amalucan-1430');
    const mapaParaB = obtenerMapaAsientos(salidaVistaPorB, alumnoB.matricula);
    expect(mapaParaB[18]).toBe(EstadoAsiento.OCUPADO);
    expect(mapaParaB[19]).toBe(EstadoAsiento.DISPONIBLE);

    const reservaB = repositorio.confirmarReserva(alumnoB, 'cu-amalucan-1430');
    const salidaFinal = repositorio.buscarSalidaPorId('cu-amalucan-1430');

    expect(reservaB.ok).toBe(true);
    expect(reservaB.reserva.asientoAsignado).toBe(19);
    expect(salidaFinal.totalOcupados).toBe(19);
    expect(salidaFinal.totalDisponibles).toBe(17);
    expect(salidaFinal.asientosOcupadosPorMatricula[18]).toBe('202345678');
    expect(salidaFinal.asientosOcupadosPorMatricula[19]).toBe('202311111');
  });

});
