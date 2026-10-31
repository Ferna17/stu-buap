const { validarLogin } = require('../../src/auth/loginValidator');

const padronUsuarios = [
  { email: 'Javier.sangines@alumno.buap.mx', matricula: '202268046', password: '290504' },
  { email: 'Javier.sangines@correo.buap.mx', matricula: '202268046', password: '290504' }
];

describe('Módulo de Inicio de Sesión STU (HU-01 - BICEP/CORRECT)', () => {
  test('PF1: Validar inicio de sesión con credenciales válidas (@alumno.buap.mx)', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(true);
  });

  test('PF2: Validar inicio de sesión con credenciales válidas (@correo.buap.mx)', () => {
    const res = validarLogin('Javier.sangines@correo.buap.mx', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(true);
  });

  test('PF3: Rechazar si el email tiene 0 caracteres antes del @', () => {
    const res = validarLogin('@alumno.buap.mx', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PF4: Aceptar longitud mínima de 1 carácter antes del @', () => {
    const res = validarLogin('j@alumno.buap.mx', '202268046', '290504', [{ email: 'j@alumno.buap.mx', matricula: '202268046', password: '290504' }]);
    expect(res.ok).toBe(true);
  });

  test('PF5: Aceptar longitud máxima de 30 caracteres antes del @', () => {
    const correo30 = 'javiersangines1234567890123456@alumno.buap.mx';
    const res = validarLogin(correo30, '202268046', '290504', [{ email: correo30, matricula: '202268046', password: '290504' }]);
    expect(res.ok).toBe(true);
  });

  test('PF6: Rechazar si el email supera 30 caracteres antes del @', () => {
    const correo31 = 'javiersangines12345678901234567@alumno.buap.mx';
    const res = validarLogin(correo31, '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF7: Rechazar matrícula con 8 dígitos', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '20226804', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF8: Rechazar matrícula con 10 dígitos', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '2022680460', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF9: Rechazar contraseña con 5 dígitos', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '29050', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF10: Rechazar contraseña con 7 dígitos', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '2905041', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF11: Rechazar dominio externo (@gmail.com)', () => {
    const res = validarLogin('Javier.sangines@gmail.com', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF12: Rechazar correo sin arroba (@)', () => {
    const res = validarLogin('Javier.sanginesalumno.buap.mx', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF13: Rechazar matrícula con letras', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '20226804A', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF14: Rechazar contraseña que no sea fecha válida', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '999999', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF15: Rechazar contraseña incorrecta', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '150803', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.mensaje).toBe('Contraseña incorrecta');
  });

  test('PF16: Rechazar matrícula incorrecta', () => {
    const res = validarLogin('Javier.sangines@alumno.buap.mx', '202299999', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF17: Rechazar correo incorrecto', () => {
    const res = validarLogin('otro.nombre@alumno.buap.mx', '202268046', '290504', padronUsuarios);
    expect(res.ok).toBe(false);
  });

  test('PF18: Indicar falta de registro previo si el alumno no existe', () => {
    const res = validarLogin('nuevo.alumno@alumno.buap.mx', '202512345', '101006', padronUsuarios);
    expect(res.ok).toBe(false);
    expect(res.puedeRegistrarse).toBe(true);
  });

  test('PF19: Rechazar si los tres campos están vacíos', () => {
    const res = validarLogin('', '', '', padronUsuarios);
    expect(res.ok).toBe(false);
  });
});