const { validarLogin, MENSAJE_PASSWORD_INVALIDA } = require('../../src/auth/loginValidator');

const padronUsuarios = [
  { email: 'mateo.morales@alumno.buap.mx', matricula: '202345678', password: 'AccesoStu99' },
  { email: 'mateo.morales@correo.buap.mx', matricula: '202345678', password: 'AccesoStu99' }
];

describe('Módulo de Inicio de Sesión STU (HU-01 - BICEP/CORRECT)', () => {
  test('PF1: Validar inicio de sesión con credenciales válidas (@alumno.buap.mx)', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(true);
  });

  test('PF2: Validar inicio de sesión con credenciales válidas (@correo.buap.mx)', () => {
    const res = validarLogin('mateo.morales@correo.buap.mx', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(true);
  });

  test('PF3: Rechazar si el email tiene 0 caracteres antes del @', () => {
    const res = validarLogin('@alumno.buap.mx', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PF4: Aceptar longitud mínima de 1 carácter antes del @', () => {
    const res = validarLogin('m@alumno.buap.mx', '202345678', 'AccesoStu99', [{ email: 'm@alumno.buap.mx', matricula: '202345678', password: 'AccesoStu99' }]);
    expect(res.ok).toBe(true);
  });

  test('PF5: Aceptar longitud máxima de 30 caracteres antes del @', () => {
    const correo30 = 'mateomorales123456789012345678@alumno.buap.mx';
    const res = validarLogin(correo30, '202345678', 'AccesoStu99', [{ email: correo30, matricula: '202345678', password: 'AccesoStu99' }]);
    expect(res.ok).toBe(true);
  });

  test('PF6: Rechazar si el email supera 30 caracteres antes del @', () => {
    const correo31 = 'mateomorales1234567890123456789@alumno.buap.mx';
    const res = validarLogin(correo31, '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF7: Rechazar matrícula con 8 dígitos', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '20234567', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF8: Rechazar matrícula con 10 dígitos', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '2023456780', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF9: Rechazar contraseña con menos de 8 caracteres (7 caracteres)', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'Clave99', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
    expect(res.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
  });

  test('PF10: Rechazar contraseña con más de 20 caracteres (21 caracteres)', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99Contrasena', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
    expect(res.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
  });

  test('PF11: Rechazar dominio externo (@gmail.com)', () => {
    const res = validarLogin('mateo.morales@gmail.com', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF12: Rechazar correo sin arroba (@)', () => {
    const res = validarLogin('mateo.moralesalumno.buap.mx', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF13: Rechazar matrícula con letras', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '20234567A', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF14: Rechazar contraseña que contenga espacios en blanco', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'Acceso Stu 99', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
    expect(res.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
  });

  test('PF15: Rechazar contraseña incorrecta', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'ClaveErronea99', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
    expect(res.mensaje).toBe('Contraseña incorrecta');
  });

  test('PF16: Rechazar matrícula incorrecta', () => {
    const res = validarLogin('mateo.morales@alumno.buap.mx', '202399999', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF17: Rechazar correo incorrecto', () => {
    const res = validarLogin('otro.nombre@alumno.buap.mx', '202345678', 'AccesoStu99', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF18: Indicar falta de registro previo si el alumno no existe', () => {
    const res = validarLogin('nuevo.alumno@alumno.buap.mx', '202512345', 'Alumno2026', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.puedeRegistrarse).toBe(true);
  });

  test('PF19: Rechazar si los tres campos están vacíos', () => {
    const res = validarLogin('', '', '', padronUsuarios);
    expect(res.ok).toBe(false);
  });
});
