// tests/auth/registerValidator.test.js

const { validarRegistro } = require('../../src/auth/registerValidator');

const padronExistente = [
  { email: 'Javier.sangines@alumno.buap.mx', matricula: '202268046', password: '290504', saldoPasajes: 4 }
];

describe('Módulo de Registro de Nuevo Estudiante STU (HU-01.1 - BICEP/CORRECT)', () => {

  test('PR1: Registrar exitosamente a un alumno nuevo con dominio @alumno.buap.mx y saldo inicial 0', () => {
    const res = validarRegistro('fernando.hernandez@alumno.buap.mx', '202112345', '170301', padronExistente);
    expect(res.ok).toBe(true);
    expect(res.usuario.saldoPasajes).toBe(0);
  });

  test('PR2: Registrar exitosamente a un alumno nuevo con dominio @correo.buap.mx', () => {
    const res = validarRegistro('fernando.hernandez@correo.buap.mx', '202154321', '170301', padronExistente);
    expect(res.ok).toBe(true);
    expect(res.usuario.estado).toBe('ACTIVO');
  });

  test('PR3: Rechazar registro si el correo tiene 0 caracteres antes del @', () => {
    const res = validarRegistro('@alumno.buap.mx', '202112345', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR4: Rechazar registro si el correo supera 30 caracteres antes del @ (31 caracteres)', () => {
    const correo31 = 'fernandohernandez12345678901234@alumno.buap.mx';
    const res = validarRegistro(correo31, '202112345', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR5: Rechazar registro si la matrícula no tiene exactamente 9 dígitos (8 dígitos)', () => {
    const res = validarRegistro('fernando.hernandez@alumno.buap.mx', '20211234', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
  });

  test('PR6: Rechazar registro si la contraseña no tiene 6 dígitos (5 dígitos)', () => {
    const res = validarRegistro('fernando.hernandez@alumno.buap.mx', '202112345', '17030', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
  });

  test('PR7: Rechazar registro con dominio externo no permitido (@gmail.com)', () => {
    const res = validarRegistro('fernando.hernandez@gmail.com', '202112345', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR8: Rechazar registro cuando la matrícula contiene letras', () => {
    const res = validarRegistro('fernando.hernandez@alumno.buap.mx', '20211234A', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
  });

  test('PR9: Rechazar registro cuando la contraseña no es una fecha válida (999999)', () => {
    const res = validarRegistro('fernando.hernandez@alumno.buap.mx', '202112345', '999999', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
  });

  test('PR10: Rechazar registro si la matrícula ya está registrada en el padrón', () => {
    const res = validarRegistro('nuevo.correo@alumno.buap.mx', '202268046', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
    expect(res.mensaje).toBe('La matrícula ya cuenta con un registro previo');
  });

  test('PR11: Rechazar registro si el correo institucional ya está registrado', () => {
    const res = validarRegistro('Javier.sangines@alumno.buap.mx', '202599999', '170301', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
    expect(res.mensaje).toBe('El correo institucional ya está registrado');
  });

  test('PR12: Rechazar registro si los tres campos están vacíos', () => {
    const res = validarRegistro('', '', '', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('todos');
  });

});
