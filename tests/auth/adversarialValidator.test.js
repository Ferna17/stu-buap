// tests/auth/adversarialValidator.test.js
/**
 * Suite de Pruebas Adversariales para STU BUAP
 * Evalúa robustez, seguridad, resistencia a DoS, inyecciones y reglas de contraseña (8–20 caracteres sin espacios).
 */

const { validarLogin, MENSAJE_PASSWORD_INVALIDA } = require('../../src/auth/loginValidator');
const { validarRegistro } = require('../../src/auth/registerValidator');

const padronBase = [
  { email: 'mateo.morales@alumno.buap.mx', matricula: '202345678', password: 'AccesoStu99', saldoPasajes: 5, estado: 'ACTIVO' },
  { email: 'maria.gonzalez@correo.buap.mx', matricula: '202112345', password: 'Maria2026!', saldoPasajes: 2, estado: 'ACTIVO' }
];

describe('Suite de Pruebas Adversariales - Robustez y Seguridad STU BUAP', () => {

  describe('Vector 1: Fronteras de Longitud y Contraseñas con Espacios (8 a 20 caracteres)', () => {
    test('ADV-PWD-1: Aceptar longitud mínima exacta de 8 caracteres sin espacios en login y registro', () => {
      const padronMinimo = [
        { email: 'minimo@alumno.buap.mx', matricula: '202412345', password: 'Clave99!', saldoPasajes: 0 }
      ];
      const resLogin = validarLogin('minimo@alumno.buap.mx', '202412345', 'Clave99!', padronMinimo);
      expect(resLogin.ok).toBe(true);

      const resReg = validarRegistro('nuevo.min@alumno.buap.mx', '202499999', 'Clave99!', padronMinimo);
      expect(resReg.ok).toBe(true);
    });

    test('ADV-PWD-2: Aceptar longitud máxima exacta de 20 caracteres sin espacios en login y registro', () => {
      const clave20 = 'AccesoStu99Seguro123';
      const padronMax = [
        { email: 'maximo@alumno.buap.mx', matricula: '202412345', password: clave20, saldoPasajes: 0 }
      ];
      const resLogin = validarLogin('maximo@alumno.buap.mx', '202412345', clave20, padronMax);
      expect(resLogin.ok).toBe(true);

      const resReg = validarRegistro('nuevo.max@alumno.buap.mx', '202499999', clave20, padronMax);
      expect(resReg.ok).toBe(true);
    });

    test('ADV-PWD-3: Rechazar 7 caracteres y 21 caracteres en login y registro', () => {
      for (const clave of ['Clave99', 'AccesoStu99Contrasena']) {
        const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', clave, padronBase);
        expect(resLogin.ok).toBe(false);
        expect(resLogin.campoError).toBe('password');
        expect(resLogin.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);

        const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', clave, padronBase);
        expect(resReg.ok).toBe(false);
        expect(resReg.campoError).toBe('password');
        expect(resReg.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
      }
    });

    test('ADV-PWD-4: Rechazar espacios iniciales, finales o internos en contraseña en login y registro', () => {
      for (const claveConEspacios of [' AccesoStu99', 'AccesoStu99 ', 'Acceso Stu 99']) {
        const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', claveConEspacios, padronBase);
        expect(resLogin.ok).toBe(false);
        expect(resLogin.campoError).toBe('password');
        expect(resLogin.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);

        const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', claveConEspacios, padronBase);
        expect(resReg.ok).toBe(false);
        expect(resReg.campoError).toBe('password');
        expect(resReg.mensaje).toBe(MENSAJE_PASSWORD_INVALIDA);
      }
    });

    test('ADV-PWD-5: Rechazar tabuladores o caracteres de control inyectados en contraseña', () => {
      for (const claveInsegura of ['Acceso\tStu99', 'Acceso\nStu99']) {
        const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', claveInsegura, padronBase);
        expect(resLogin.ok).toBe(false);
        expect(resLogin.campoError).toBe('password');

        const resReg = validarRegistro('nuevo.alumno@alumno.buap.mx', '202511111', claveInsegura, padronBase);
        expect(resReg.ok).toBe(false);
        expect(resReg.campoError).toBe('password');
      }
    });
  });

  describe('Vector 2: Confusión de Tipos (Type Juggling) y Resistencia a DoS', () => {
    test('ADV-TYP-1: No crashear si email no es string (número o booleano)', () => {
      expect(() => validarLogin(12345, '202345678', 'AccesoStu99', padronBase)).not.toThrow();
      const resLogin = validarLogin(12345, '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro(true, '202345678', 'AccesoStu99', padronBase)).not.toThrow();
      const resReg = validarRegistro(true, '202345678', 'AccesoStu99', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-2: No crashear si email es un array u objeto', () => {
      expect(() => validarLogin(['mateo.morales@alumno.buap.mx'], '202345678', 'AccesoStu99', padronBase)).not.toThrow();
      const resLogin = validarLogin(['mateo.morales@alumno.buap.mx'], '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro({ email: 'mateo@alumno.buap.mx' }, '202345678', 'AccesoStu99', padronBase)).not.toThrow();
      const resReg = validarRegistro({ email: 'mateo@alumno.buap.mx' }, '202345678', 'AccesoStu99', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-3: No crashear si matrícula no es string (número u objeto)', () => {
      expect(() => validarLogin('mateo.morales@alumno.buap.mx', 202345678, 'AccesoStu99', padronBase)).not.toThrow();
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', 202345678, 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', { id: 202345678 }, 'AccesoStu99', padronBase)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', { id: 202345678 }, 'AccesoStu99', padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-4: No crashear si password no es string (número o null)', () => {
      expect(() => validarLogin('mateo.morales@alumno.buap.mx', '202345678', 20262026, padronBase)).not.toThrow();
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 20262026, padronBase);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', null, padronBase)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', null, padronBase);
      expect(resReg.ok).toBe(false);
    });

    test('ADV-TYP-5: No crashear si baseDeDatos es null o undefined', () => {
      expect(() => validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99', null)).not.toThrow();
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99', null);
      expect(resLogin.ok).toBe(false);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', 'ValeriaStu88', null)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', 'ValeriaStu88', null);
      expect(resReg.ok).toBe(true);
    });
  });

  describe('Vector 3: Evasión por Espacios en Blanco (Whitespace Smuggling & Bypass de Duplicados)', () => {
    test('ADV-WHT-1: Espacios iniciales/finales NO deben evadir la verificación de duplicidad en registro', () => {
      const resReg = validarRegistro('  mateo.morales@alumno.buap.mx  ', '202599999', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
      expect(resReg.mensaje).toBe('El correo institucional ya está registrado');
    });

    test('ADV-WHT-2: Matrícula con espacios alrededor debe ser sanitizada y no evadir duplicidad en registro', () => {
      const resReg = validarRegistro('alumno.nuevo@alumno.buap.mx', '  202345678  ', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
      expect(resReg.mensaje).toBe('La matrícula ya cuenta con un registro previo');
    });

    test('ADV-WHT-3: Login con espacios accidentales en correo y matrícula debe autenticar con éxito', () => {
      const res = validarLogin('  mateo.morales@alumno.buap.mx  ', '  202345678  ', 'AccesoStu99', padronBase);
      expect(res.ok).toBe(true);
    });

    test('ADV-WHT-4: Rechazar espacios internos en el nombre de usuario del correo', () => {
      const resLogin = validarLogin('mateo morales@alumno.buap.mx', '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro('mateo morales@alumno.buap.mx', '202511111', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });
  });

  describe('Vector 4: Inyecciones de Código y Payloads Maliciosos en Nombre de Usuario', () => {
    test('ADV-INJ-1: Rechazar inyección XSS (<script>alert(1)</script>) en email', () => {
      const xssEmail = '<script>alert(1)</script>@alumno.buap.mx';
      const resLogin = validarLogin(xssEmail, '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(xssEmail, '202511111', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-2: Rechazar inyección SQL (\' OR 1=1 --) en email', () => {
      const sqliEmail = "' OR 1=1 --@alumno.buap.mx";
      const resLogin = validarLogin(sqliEmail, '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(sqliEmail, '202511111', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-3: Rechazar Path Traversal (../../etc/passwd) en email', () => {
      const pathEmail = '../../etc/passwd@alumno.buap.mx';
      const resLogin = validarLogin(pathEmail, '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(pathEmail, '202511111', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });

    test('ADV-INJ-4: Rechazar inyección de Null Byte en email', () => {
      const nullByteEmail = 'admin\0user@alumno.buap.mx';
      const resLogin = validarLogin(nullByteEmail, '202345678', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('email');

      const resReg = validarRegistro(nullByteEmail, '202511111', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('email');
    });
  });

  describe('Vector 5: Resiliencia ante Registros Corruptos o Envenenados en Base de Datos', () => {
    test('ADV-PBN-1: No crashear ante registros con propiedades null o undefined en el padrón', () => {
      const padronCorrupto = [
        { email: null, matricula: undefined, password: null },
        { email: 'mateo.morales@alumno.buap.mx', matricula: '202345678', password: 'AccesoStu99' }
      ];

      expect(() => validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99', padronCorrupto)).not.toThrow();
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678', 'AccesoStu99', padronCorrupto);
      expect(resLogin.ok).toBe(true);

      expect(() => validarRegistro('nuevo@alumno.buap.mx', '202511111', 'ValeriaStu88', padronCorrupto)).not.toThrow();
      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202511111', 'ValeriaStu88', padronCorrupto);
      expect(resReg.ok).toBe(true);
    });
  });

  describe('Vector 6: Matrículas Adversariales (Ceros y Caracteres de Control)', () => {
    test('ADV-MAT-1: Rechazar matrícula compuesta únicamente por ceros (000000000)', () => {
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '000000000', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('matricula');

      const resReg = validarRegistro('nuevo@alumno.buap.mx', '000000000', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
    });

    test('ADV-MAT-2: Rechazar matrícula con saltos de línea inyectados', () => {
      const resLogin = validarLogin('mateo.morales@alumno.buap.mx', '202345678\n', 'AccesoStu99', padronBase);
      expect(resLogin.ok).toBe(false);
      expect(resLogin.campoError).toBe('matricula');

      const resReg = validarRegistro('nuevo@alumno.buap.mx', '202345678\n', 'ValeriaStu88', padronBase);
      expect(resReg.ok).toBe(false);
      expect(resReg.campoError).toBe('matricula');
    });
  });

});
