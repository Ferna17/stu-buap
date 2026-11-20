const { validarRegistro } = require('../../src/auth/registerValidator');
const {
  MENSAJE_SIN_PASAJES_ESTADO,
  MENSAJE_SIN_PASAJES_SALIDA,
  evaluarSaldo,
  intentarSeleccionarSalida
} = require('../../src/saldo/evaluadorSaldo');

describe('Módulo de Consulta de Saldo STU (HU-02 - BICEP/CORRECT)', () => {

  test('PS1: Mostrar saldo positivo y habilitar selección de salida con 10 pasajes', () => {
    const usuario = {
      email: 'mateo.morales@alumno.buap.mx',
      matricula: '202345678',
      nombre: 'Mateo Morales Vega',
      pasajesDisponibles: 10,
      estado: 'ACTIVO'
    };

    const estado = evaluarSaldo(usuario);
    expect(estado.pasajesDisponibles).toBe(10);
    expect(estado.etiquetaSaldo).toBe('10 viajes disponibles');
    expect(estado.puedeReservarViaje).toBe(true);
    expect(estado.requiereRecarga).toBe(false);

    const seleccion = intentarSeleccionarSalida(usuario);
    expect(seleccion.permitido).toBe(true);
    expect(seleccion.redirigirARecarga).toBe(false);
  });

  test('PS2: Validar límite mínimo válido de 1 pasaje en singular y permitir selección de salida', () => {
    const usuario = {
      email: 'mateo.morales@alumno.buap.mx',
      matricula: '202345678',
      pasajesDisponibles: 1
    };

    const estado = evaluarSaldo(usuario);
    expect(estado.pasajesDisponibles).toBe(1);
    expect(estado.etiquetaSaldo).toBe('1 viaje disponible');
    expect(estado.puedeReservarViaje).toBe(true);
    expect(estado.requiereRecarga).toBe(false);

    const seleccion = intentarSeleccionarSalida(usuario);
    expect(seleccion.permitido).toBe(true);
    expect(seleccion.redirigirARecarga).toBe(false);
  });

  test('PS3: Bloquear selección de salida y redirigir a recarga con 0 pasajes', () => {
    const usuario = {
      email: 'nuevo.alumno@alumno.buap.mx',
      matricula: '202112345',
      pasajesDisponibles: 0
    };

    const estado = evaluarSaldo(usuario);
    expect(estado.pasajesDisponibles).toBe(0);
    expect(estado.etiquetaSaldo).toBe('0 viajes disponibles');
    expect(estado.puedeReservarViaje).toBe(false);
    expect(estado.requiereRecarga).toBe(true);
    expect(estado.mensajeEstado).toBe(MENSAJE_SIN_PASAJES_ESTADO);

    const seleccion = intentarSeleccionarSalida(usuario);
    expect(seleccion.permitido).toBe(false);
    expect(seleccion.redirigirARecarga).toBe(true);
    expect(seleccion.mensaje).toBe(MENSAJE_SIN_PASAJES_SALIDA);
  });

  test('PS4: Sanitizar saldo negativo corrupto a 0 y redirigir a recarga al seleccionar salida', () => {
    const usuarioCorrupto = {
      email: 'anomalo@alumno.buap.mx',
      matricula: '202299999',
      pasajesDisponibles: -3
    };

    const estado = evaluarSaldo(usuarioCorrupto);
    expect(estado.pasajesDisponibles).toBe(0);
    expect(estado.etiquetaSaldo).toBe('0 viajes disponibles');
    expect(estado.puedeReservarViaje).toBe(false);
    expect(estado.requiereRecarga).toBe(true);

    const seleccion = intentarSeleccionarSalida(usuarioCorrupto);
    expect(seleccion.permitido).toBe(false);
    expect(seleccion.redirigirARecarga).toBe(true);
    expect(seleccion.mensaje).toBe(MENSAJE_SIN_PASAJES_SALIDA);
  });

  test('PS5: Integración entre validarRegistro y evaluarSaldo con saldo inicial 0 y redirección a recarga', () => {
    const resultadoRegistro = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      [],
      'Valeria',
      'Soto Paredes'
    );
    expect(resultadoRegistro.ok).toBe(true);
    expect(resultadoRegistro.usuario).toBeDefined();

    const estado = evaluarSaldo(resultadoRegistro.usuario);
    expect(estado.pasajesDisponibles).toBe(0);
    expect(estado.etiquetaSaldo).toBe('0 viajes disponibles');
    expect(estado.puedeReservarViaje).toBe(false);
    expect(estado.requiereRecarga).toBe(true);

    const seleccion = intentarSeleccionarSalida(resultadoRegistro.usuario);
    expect(seleccion.permitido).toBe(false);
    expect(seleccion.redirigirARecarga).toBe(true);
    expect(seleccion.mensaje).toBe(MENSAJE_SIN_PASAJES_SALIDA);
  });

  test('PS6: Consultar saldo actualizado y evaluar cambio de usuario con y sin pasajes', () => {
    const usuarioConSaldo = {
      email: 'mateo.morales@alumno.buap.mx',
      matricula: '202345678',
      pasajesDisponibles: 10
    };
    const estadoConSaldo = evaluarSaldo(usuarioConSaldo);
    expect(estadoConSaldo.pasajesDisponibles).toBe(10);
    expect(estadoConSaldo.etiquetaSaldo).toBe('10 viajes disponibles');
    expect(intentarSeleccionarSalida(usuarioConSaldo).permitido).toBe(true);

    const usuarioSinSaldo = {
      email: 'sin.saldo@alumno.buap.mx',
      matricula: '202500001',
      pasajesDisponibles: 0
    };
    const estadoSinSaldo = evaluarSaldo(usuarioSinSaldo);
    expect(estadoSinSaldo.pasajesDisponibles).toBe(0);
    expect(estadoSinSaldo.etiquetaSaldo).toBe('0 viajes disponibles');

    const seleccionBloqueada = intentarSeleccionarSalida(usuarioSinSaldo);
    expect(seleccionBloqueada.permitido).toBe(false);
    expect(seleccionBloqueada.redirigirARecarga).toBe(true);
    expect(seleccionBloqueada.mensaje).toBe(MENSAJE_SIN_PASAJES_SALIDA);
  });

});
