// tests/auth/adversarialValidator.test.js
/**
 * Suite de Pruebas Adversariales para STU BUAP
 * Evalúa robustez, seguridad, resistencia a DoS, inyecciones y validaciones lógicas de calendario.
 */

const { validarLogin } = require('../../src/auth/loginValidator');
const { validarRegistro } = require('../../src/auth/registerValidator');

const padronBase = [
  { email: 'Javier.sangines@alumno.buap.mx', matricula: '202268046', password: '290504', saldoPasajes: 5, estado: 'ACTIVO' },
  { email: 'maria.gonzalez@correo.buap.mx', matricula: '202112345', password: '150802', saldoPasajes: 2, estado: 'ACTIVO' }
];

describe('Suite de Pruebas Adversariales - Robustez y Seguridad STU BUAP', () => {

  describe('Vector 1: Fechas Imposibles y Lógica de Calendario en Contraseña (DDMMAA)', () => {
    test('ADV-CAL-1: Rechazar 31 de febrero (310204) en login y registro', () => {
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '310204', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('password');
      expect(resLogin.mensaje).toBe('La contraseña no corresponde a una fecha válida');

      const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', '310204', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('password');
      expect(resReg.mensaje).toBe('La contraseña no corresponde a una fecha válida');
    });

    test('ADV-CAL-2: Rechazar 30 de febrero (300204) en login y registro', () => {
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '300204', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('password');

      const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', '300204', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('password');
    });

    test('ADV-CAL-3: Rechazar día 31 en meses de 30 días (310404, 310604, 310904, 311104)', () => {
      const meses30 = ['310404', '310604', '310904', '311104'];
      for (const pass of meses30) {
        const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', pass, padronBase);
        expect(resLogin.ok).toBe(false);
        expect(resLogin.campoError).toBe('password');

        const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', pass, padronBase);
        expect(resReg.ok).toBe(false);
        expect(resReg.campoError).toBe('password');
      }
    });

    test('ADV-CAL-4: Rechazar 29 de febrero en años no bisiestos (290203)', () => {
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290203', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('password');

      const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', '290203', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('password');
    });

    test('ADV-CAL-5: Aceptar 29 de febrero en año bisiesto real (290204)', () => {
      // 2004 es año bisiesto
      const padronBisiesto = [
        { email: 'bisiesto@alumno.buap.mx', matricula: '202412345', password: '290204', saldoPasajes: 0 }
      ];
      const resLogin = validarLogin('bisiesto@alumno.buap.mx', '202412345', '290204', padronBisiesto);
      expect(resLogin.ok).toBe(true);

      const resReg = validarRegistro('otro.bisiesto@alumno.buap.mx', '202499999', '290204', padronBisiesto);
      expect(resReg.ok).toBe(true);
    });
  });

  describe('Vector 2: Confusión de Tipos (Type Juggling) y Resistencia a DoS', () => {
    test('ADV-TYP-1: No crashear si email no es string (número o booleano)', () => {
      expect(() => validarLogin(12345, '202268046', '290504', padronBase)).not.toThrow();
      const resLogin = validarLogin(12345, '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro(true, '202268046', '290504', padronBase)).not.toThrow();
      const resReg = validarRegistro(true, '202268046', '290504', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-2: No crashear si email es un array u objeto', () => {
      expect(() => validarLogin(['Javier.sangines@alumno.buap.mx'], '202268046', '290504', padronBase)).not.toThrow();
      const resLogin = validarLogin(['Javier.sangines@alumno.buap.mx'], '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro({ email: 'Javier@alumno.buap.mx' }, '202268046', '290504', padronBase)).not.toThrow();
      const resReg = validarRegistro({ email: 'Javier@alumno.buap.mx' }, '202268046', '290504', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-3: No crashear si matricula no es string (número u objeto)', () => {
      expect(() => validarLogin('Javier.sangines@alumno.buap.mx', 202268046, '290504', padronBase)).not.toThrow();
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', 202268046, '290504', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', { id: 202268046 }, '290504', padronBase)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', { id: 202268046 }, '290504', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-4: No crashear si password no es string (número o null)', () => {
      expect(() => validarLogin('Javier.sangines@alumno.buap.mx', '202268046', 290504, padronBase)).not.toThrow();
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', 290504, padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', 290504, padronBase)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', 290504, padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-5: No crashear si baseDeDatos es null o undefined', () => {
      expect(() => validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290504', null)).not.toThrow();
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290504', null);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', '290504', null)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', '290504', null);
      expect(resReg.ok).toBe(true);
    });
  });

  describe('Vector 3: Evasión por Espacios en Blanco (Whitespace Smuggling & Bypass de Duplicados)', () => {
    test('ADV-WHT-1: Espacios iniciales/finales NO deben evadir la verificación de duplicidad en registro', () => {
      // Intento malicioso de registrar el mismo correo existente agregando espacios
      const resReg = validarRegistro('  Javier.sangines@alumno.buap.mx  ', '202599999', '170301', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
      expect(resReg.mensaje).toBe('El correo institucional ya está registrado');
    });

    test('ADV-WHT-2: Matrícula con espacios alrededor debe ser sanitizada y no evadir duplicidad en registro', () => {
      const resReg = validarRegistro('alumno.nuevo@alumno.buap.mx', '  202268046  ', '170301', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
      expect(resReg.mensaje).toBe('La matrícula ya cuenta con un registro previo');
    });

    test('ADV-WHT-3: Login con espacios accidentales alrededor de las credenciales debe autenticar con éxito', () => {
      const res = validarLogin('  Javier.sangines@alumno.buap.mx  ', '  202268046  ', '  290504  ', padronBase);
      expect(res.ok).toBe(true);
    });

    test('ADV-WHT-4: Rechazar espacios internos en el nombre de usuario del correo', () => {
      const resLogin = validarLogin('javier sangines@alumno.buap.mx', '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro('javier sangines@alumno.buap.mx', '202511111', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });
  });

  describe('Vector 4: Inyecciones de Código y Payloads Maliciosos en Nombre de Usuario', () => {
    test('ADV-INJ-1: Rechazar inyección XSS (<script>alert(1)</script>) en email', () => {
      const xssEmail = '<script>alert(1)</script>@alumno.buap.mx';
      const resLogin = validarLogin(xssEmail, '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(xssEmail, '202511111', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-2: Rechazar inyección SQL (\' OR 1=1 --) en email', () => {
      const sqliEmail = "' OR 1=1 --@alumno.buap.mx";
      const resLogin = validarLogin(sqliEmail, '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(sqliEmail, '202511111', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-3: Rechazar Path Traversal (../../etc/passwd) en email', () => {
      const pathEmail = '../../etc/passwd@alumno.buap.mx';
      const resLogin = validarLogin(pathEmail, '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(pathEmail, '202511111', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-4: Rechazar inyección de Null Byte en email', () => {
      const nullByteEmail = 'admin\0user@alumno.buap.mx';
      const resLogin = validarLogin(nullByteEmail, '202268046', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(nullByteEmail, '202511111', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });
  });

  describe('Vector 5: Resiliencia ante Registros Corruptos o Envenenados en Base de Datos', () => {
    test('ADV-PBN-1: No crashear ante registros con propiedades null o undefined en el padrón', () => {
      const padronCorrupto = [
        { email: null, matricula: undefined, password: null },
        { email: 'Javier.sangines@alumno.buap.mx', matricula: '202268046', password: '290504' }
      ];

      expect(() => validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290504', padronCorrupto)).not.toThrow();
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046', '290504', padronCorrupto);
      expect(resLogin.ok).toBe(true);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', '290504', padronCorrupto)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', '290504', padronCorrupto);
      expect(resReg.ok).toBe(true);
    });
  });

  describe('Vector 6: Matrículas Adversariales (Ceros y Caracteres de Control)', () => {
    test('ADV-MAT-1: Rechazar matrícula compuesta únicamente por ceros (000000000)', () => {
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '000000000', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('matricula');

      const resReg = validarRegistro('nuevo@alumno.buap.mx', '000000000', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
    });

    test('ADV-MAT-2: Rechazar matrícula con saltos de línea inyectados', () => {
      const resLogin = validarLogin('Javier.sangines@alumno.buap.mx', '202268046\n', '290504', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('matricula');

      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202268046\n', '290504', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
    });
  });

});
