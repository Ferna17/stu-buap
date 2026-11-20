const { validarRegistro } = require('../../src/auth/registerValidator');
const { MENSAJE_PASSWORD_INVALIDA } = require('../../src/auth/loginValidator');

const padronExistente = [
  { email: 'mateo.morales@alumno.buap.mx', matricula: '202345678', password: 'AccesoStu99', saldoPasajes: 4 }
];

describe('Módulo de Registro de Nuevo Estudiante STU (HU-01.1 - BICEP/CORRECT)', () => {

  test('PR1: Registrar exitosamente a un alumno nuevo con dominio @alumno.buap.mx y saldo inicial 0', () => {
    const res = validarRegistro('valeria.soto@alumno.buap.mx', '202456789', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(true);
    expect(res.usuario.saldoPasajes).toBe(0);
    expect(res.usuario.estado).toBe('ACTIVO');
  });

  test('PR2: Registrar exitosamente a un alumno nuevo con dominio @correo.buap.mx', () => {
    const res = validarRegistro('diego.perez@correo.buap.mx', '202154321', 'Diego2026', padronExistente);
    expect(res.ok).toBe(true);
    expect(res.usuario.saldoPasajes).toBe(0);
    expect(res.usuario.estado).toBe('ACTIVO');
  });

  test('PR3: Rechazar registro si el correo tiene 0 caracteres antes del @', () => {
    const res = validarRegistro('@alumno.buap.mx', '202456789', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR4: Rechazar registro si el correo supera 30 caracteres antes del @ (31 caracteres)', () => {
    const correo31 = 'valeriasotoparedes1234567890123@alumno.buap.mx';
    const res = validarRegistro(correo31, '202456789', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR5: Rechazar registro si la matrícula no tiene exactamente 9 dígitos (8 dígitos)', () => {
    const res = validarRegistro('valeria.soto@alumno.buap.mx', '20245678', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
  });

  test('PR6: Rechazar registro si la contraseña tiene menos de 8 caracteres (7 caracteres)', () => {
    const res = validarRegistro('valeria.soto@alumno.buap.mx', '202456789', 'Vale123', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('password');
    expect(res.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
  });

  test('PR7: Rechazar registro con dominio externo no permitido (@gmail.com)', () => {
    const res = validarRegistro('valeria.soto@gmail.com', '202456789', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
  });

  test('PR8: Rechazar registro cuando la matrícula contiene letras', () => {
    const res = validarRegistro('valeria.soto@alumno.buap.mx', '20245678A', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
  });

  test('PR9: Rechazar registro si la contraseña supera 20 caracteres o contiene espacios', () => {
    const resExceso = validarRegistro('valeria.soto@alumno.buap.mx', '202456789', 'ValeriaStu88ClaveLarg', padronExistente);
    expect(resExceso.ok).toBe(false);
    expect(resExceso.campoError).toBe('password');
    expect(resExceso.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);

    const resEspacios = validarRegistro('valeria.soto@alumno.buap.mx', '202456789', 'Valeria 2026', padronExistente);
    expect(resEspacios.ok).toBe(false);
    expect(resEspacios.campoError).toBe('password');
    expect(resEspacios.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
  });

  test('PR10: Rechazar registro si la matrícula ya está registrada en el padrón', () => {
    const res = validarRegistro('nuevo.correo@alumno.buap.mx', '202345678', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('matricula');
    expect(res.mensaje).toBe('La matrícula ya cuenta con un registro previo');
  });

  test('PR11: Rechazar registro si el correo institucional ya está registrado', () => {
    const res = validarRegistro('mateo.morales@alumno.buap.mx', '202599999', 'ValeriaStu88', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('email');
    expect(res.mensaje).toBe('El correo institucional ya está registrado');
  });

  test('PR12: Rechazar registro si los tres campos están vacíos', () => {
    const res = validarRegistro('', '', '', padronExistente);
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('todos');
  });

  test('PR13: Registrar exitosamente con Nombre(s) y Apellidos normalizados en el perfil', () => {
    const res = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      padronExistente,
      '  Valeria ',
      ' Soto   Paredes '
    );
    expect(res.ok).toBe(true);
    expect(res.usuario.nombre).toBe('Valeria Soto Paredes');
    expect(res.usuario.saldoPasajes).toBe(0);
    expect(res.usuario.estado).toBe('ACTIVO');
  });

  test('PR14: Rechazar registro cuando Nombre(s) está vacío', () => {
    const res = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      padronExistente,
      '',
      'Soto Paredes'
    );
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('nombres');
  });

  test('PR15: Rechazar registro cuando Apellidos está vacío', () => {
    const res = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      padronExistente,
      'Valeria',
      '   '
    );
    expect(res.ok).toBe(false);
    expect(res.campoError).toBe('apellidos');
  });

  test('PR16: Rechazar registro cuando Nombre(s) o Apellidos contienen dígitos o símbolos', () => {
    const resNombre = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      padronExistente,
      'Valeria123',
      'Soto'
    );
    expect(resNombre.ok).toBe(false);
    expect(resNombre.campoError).toBe('nombres');

    const resApellido = validarRegistro(
      'valeria.soto@alumno.buap.mx',
      '202456789',
      'ValeriaStu88',
      padronExistente,
      'Valeria',
      'Soto@99'
    );
    expect(resApellido.ok).toBe(false);
    expect(resApellido.campoError).toBe('apellidos');
  });

});
