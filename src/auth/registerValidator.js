const { esFechaNacimientoValida } = require('./loginValidator');

const DOMINIOS_BUAP = ['alumno.buap.mx', 'alm.buap.mx', 'correo.buap.mx'];
const REGEX_CONTROL = /[\r\n\0]/;
const REGEX_NOMBRE_PROPIO = /^\p{L}+([ '-]\p{L}+)*$/u;

function validarCampoNombre(valor, campoError, mensajeVacio, mensajeLongitud, mensajeCaracteres) {
  if (typeof valor !== 'string') {
    return { ok: false, campoError, mensaje: mensajeVacio };
  }
  if (REGEX_CONTROL.test(valor)) {
    return { ok: false, campoError, mensaje: 'Entrada con caracteres de control no permitidos' };
  }
  const limpio = valor.trim().replace(/\s+/g, ' ');
  if (limpio.length === 0) {
    return { ok: false, campoError, mensaje: mensajeVacio };
  }
  if (limpio.length < 2 || limpio.length > 50) {
    return { ok: false, campoError, mensaje: mensajeLongitud };
  }
  if (!REGEX_NOMBRE_PROPIO.test(limpio)) {
    return { ok: false, campoError, mensaje: mensajeCaracteres };
  }
  return { ok: true, valorLimpio: limpio };
}

function validarRegistro(email, matricula, password, baseDeDatos = [], nombres = null, apellidos = null) {
  const requiereNombreCompleto = nombres !== null || apellidos !== null;

  // 1. Validación de tipos primitivos (Defensa contra Type Confusion / DoS)
  if (typeof email !== 'string' || typeof matricula !== 'string' || typeof password !== 'string') {
    return {
      ok: false,
      campoError: 'todos',
      mensaje: requiereNombreCompleto
        ? 'Completa tu nombre, apellidos, correo, matrícula y contraseña para registrarte'
        : 'Completa tu correo, matrícula y contraseña para registrarte'
    };
  }

  // Rechazar inyecciones de caracteres de control (CRLF, saltos de línea, null bytes)
  if (REGEX_CONTROL.test(email) || REGEX_CONTROL.test(matricula) || REGEX_CONTROL.test(password)) {
    const campo = REGEX_CONTROL.test(matricula) ? 'matricula' : (REGEX_CONTROL.test(email) ? 'email' : 'password');
    return { ok: false, campoError: campo, mensaje: 'Entrada con caracteres de control no permitidos' };
  }

  // Sanitización contra espacios accidentales iniciales/finales
  const emailLimpio = email.trim();
  const matriculaLimpia = matricula.trim();
  const passwordLimpia = password.trim();
  let nombreCompletoValidado = null;

  if (requiereNombreCompleto) {
    const nombresTexto = typeof nombres === 'string' ? nombres : '';
    const apellidosTexto = typeof apellidos === 'string' ? apellidos : '';
    if (
      !REGEX_CONTROL.test(nombresTexto) &&
      !REGEX_CONTROL.test(apellidosTexto) &&
      nombresTexto.trim().length === 0 &&
      apellidosTexto.trim().length === 0 &&
      emailLimpio.length === 0 &&
      matriculaLimpia.length === 0 &&
      passwordLimpia.length === 0
    ) {
      return {
        ok: false,
        campoError: 'todos',
        mensaje: 'Completa tu nombre, apellidos, correo, matrícula y contraseña para registrarte'
      };
    }

    const resNombres = validarCampoNombre(
      nombres,
      'nombres',
      'Ingresa tu(s) nombre(s)',
      'El nombre debe tener entre 2 y 50 caracteres',
      'El nombre solo admite letras y espacios'
    );
    if (!resNombres.ok) {
      return { ok: false, campoError: resNombres.campoError, mensaje: resNombres.mensaje };
    }

    const resApellidos = validarCampoNombre(
      apellidos,
      'apellidos',
      'Ingresa tus apellidos',
      'Los apellidos deben tener entre 2 y 50 caracteres',
      'Los apellidos solo admiten letras y espacios'
    );
    if (!resApellidos.ok) {
      return { ok: false, campoError: resApellidos.campoError, mensaje: resApellidos.mensaje };
    }

    nombreCompletoValidado = `${resNombres.valorLimpio} ${resApellidos.valorLimpio}`;
  }

  if (emailLimpio.length === 0 || matriculaLimpia.length === 0 || passwordLimpia.length === 0) {
    return {
      ok: false,
      campoError: 'todos',
      mensaje: requiereNombreCompleto
        ? 'Completa tu nombre, apellidos, correo, matrícula y contraseña para registrarte'
        : 'Completa tu correo, matrícula y contraseña para registrarte'
    };
  }

  // 2. Validar formato del correo (PR3, PR4, PR7)
  if (!emailLimpio.includes('@')) {
    return { ok: false, campoError: 'email', mensaje: 'Formato inválido de correo electrónico' };
  }

  const partes = emailLimpio.split('@');
  if (partes.length !== 2) {
    return { ok: false, campoError: 'email', mensaje: 'Formato de correo no permitido' };
  }

  const [usuario, dominio] = partes;

  if (usuario.length === 0) {
    return { ok: false, campoError: 'email', mensaje: 'Ingresa un usuario válido antes del @' };
  }
  if (usuario.length > 30) {
    return { ok: false, campoError: 'email', mensaje: 'El correo excede el máximo de 30 caracteres antes del @' };
  }

  // Prevención de inyecciones (XSS, SQLi, Path Traversal, Null Bytes, Espacios internos)
  if (!/^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*$/.test(usuario)) {
    return { ok: false, campoError: 'email', mensaje: 'El correo contiene caracteres no permitidos' };
  }

  if (!DOMINIOS_BUAP.includes(dominio.toLowerCase())) {
    return { ok: false, campoError: 'email', mensaje: 'Usa un dominio institucional BUAP válido' };
  }

  // 3. Validar matrícula: exactamente 9 dígitos numéricos no ceros absolutos (PR5, PR8)
  if (!/^\d+$/.test(matriculaLimpia)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula solo admite dígitos numéricos' };
  }
  if (matriculaLimpia.length !== 9) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula debe tener exactamente 9 dígitos' };
  }
  if (/^0+$/.test(matriculaLimpia)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula no puede consistir únicamente en ceros' };
  }

  // 4. Validar contraseña: 6 dígitos y fecha de calendario válida (PR6, PR9)
  if (!/^\d{6}$/.test(passwordLimpia)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña debe contener 6 dígitos (DDMMAA)' };
  }
  if (!esFechaNacimientoValida(passwordLimpia)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña no corresponde a una fecha válida' };
  }

  // 5. Verificar no duplicidad en el padrón tolerante a registros corruptos (PR10, PR11)
  const db = Array.isArray(baseDeDatos) ? baseDeDatos : [];

  const matriculaExiste = db.some(
    u => u && typeof u.matricula === 'string' && u.matricula.trim() === matriculaLimpia
  );
  if (matriculaExiste) {
    return {
      ok: false,
      campoError: 'matricula',
      mensaje: 'La matrícula ya cuenta con un registro previo'
    };
  }

  const correoExiste = db.some(
    u => u && typeof u.email === 'string' && u.email.trim().toLowerCase() === emailLimpio.toLowerCase()
  );
  if (correoExiste) {
    return {
      ok: false,
      campoError: 'email',
      mensaje: 'El correo institucional ya está registrado'
    };
  }

  // 6. Crear nuevo usuario con saldo inicial de 0 pasajes y estado ACTIVO (PR1, PR2, PR13)
  const nuevoUsuario = {
    email: emailLimpio,
    matricula: matriculaLimpia,
    password: passwordLimpia,
    ...(nombreCompletoValidado !== null ? { nombre: nombreCompletoValidado } : {}),
    saldoPasajes: 0,
    estado: 'ACTIVO'
  };

  return {
    ok: true,
    mensaje: 'Registro exitoso',
    usuario: nuevoUsuario
  };
}

module.exports = { validarRegistro };
