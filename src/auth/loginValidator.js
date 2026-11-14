const DOMINIOS_BUAP = ['alumno.buap.mx', 'alm.buap.mx', 'correo.buap.mx'];

/**
 * Validador estricto de fecha de nacimiento en formato DDMMAA.
 * Evalúa días válidos según el mes (28, 29, 30 o 31) y años bisiestos.
 */
function esFechaNacimientoValida(ddmmaa) {
  if (typeof ddmmaa !== 'string' || !/^\d{6}$/.test(ddmmaa)) {
    return false;
  }
  const dia = parseInt(ddmmaa.slice(0, 2), 10);
  const mes = parseInt(ddmmaa.slice(2, 4), 10);
  const aa = parseInt(ddmmaa.slice(4, 6), 10);

  if (mes < 1 || mes > 12 || dia < 1) {
    return false;
  }

  const diasPorMes = [0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  const esBisiesto = (aa % 4 === 0);
  const maxDias = (mes === 2 && esBisiesto) ? 29 : diasPorMes[mes];

  return dia <= maxDias;
}

function validarLogin(email, matricula, password, baseDeDatos = []) {
  // 1. Validación de tipos primitivos (Defensa contra Type Confusion / DoS)
  if (typeof email !== 'string' || typeof matricula !== 'string' || typeof password !== 'string') {
    return { ok: false, campoError: 'todos', mensaje: 'Completa tu correo, matrícula y contraseña para entrar' };
  }

  // Rechazar inyecciones de caracteres de control (CRLF, saltos de línea, null bytes)
  if (/[\r\n\0]/.test(email) || /[\r\n\0]/.test(matricula) || /[\r\n\0]/.test(password)) {
    const campo = /[\r\n\0]/.test(matricula) ? 'matricula' : (/[ \r\n\0]/.test(email) ? 'email' : 'password');
    return { ok: false, campoError: campo, mensaje: 'Entrada con caracteres de control no permitidos' };
  }

  // Sanitización contra espacios accidentales iniciales/finales
  const emailLimpio = email.trim();
  const matriculaLimpia = matricula.trim();
  const passwordLimpia = password.trim();

  if (emailLimpio.length === 0 || matriculaLimpia.length === 0 || passwordLimpia.length === 0) {
    return { ok: false, campoError: 'todos', mensaje: 'Completa tu correo, matrícula y contraseña para entrar' };
  }

  // 2. Formato del correo
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

  // 3. Matrícula: 9 dígitos numéricos no ceros absolutos
  if (!/^\d+$/.test(matriculaLimpia)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula solo admite dígitos numéricos' };
  }
  if (matriculaLimpia.length !== 9) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula debe tener exactamente 9 dígitos' };
  }
  if (/^0+$/.test(matriculaLimpia)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula no puede consistir únicamente en ceros' };
  }

  // 4. Contraseña: 6 dígitos y fecha de calendario válida
  if (!/^\d{6}$/.test(passwordLimpia)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña debe contener 6 dígitos (DDMMAA)' };
  }

  if (!esFechaNacimientoValida(passwordLimpia)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña no corresponde a una fecha válida' };
  }

  // 5. Búsqueda segura en padrón (tolerante a registros corruptos en baseDeDatos)
  const db = Array.isArray(baseDeDatos) ? baseDeDatos : [];

  const usuarioEncontrado = db.find(
    u => u && typeof u.matricula === 'string' && u.matricula.trim() === matriculaLimpia &&
         typeof u.email === 'string' && u.email.trim().toLowerCase() === emailLimpio.toLowerCase()
  ) || db.find(
    u => u && ((typeof u.matricula === 'string' && u.matricula.trim() === matriculaLimpia) ||
               (typeof u.email === 'string' && u.email.trim().toLowerCase() === emailLimpio.toLowerCase()))
  );

  if (!usuarioEncontrado) {
    return { ok: false, campoError: 'registro', mensaje: 'Sin registro previo', puedeRegistrarse: true };
  }

  const uEmail = typeof usuarioEncontrado.email === 'string' ? usuarioEncontrado.email.trim().toLowerCase() : '';
  if (uEmail !== emailLimpio.toLowerCase()) {
    return { ok: false, campoError: 'email', mensaje: 'El correo no coincide' };
  }

  const uMatricula = typeof usuarioEncontrado.matricula === 'string' ? usuarioEncontrado.matricula.trim() : '';
  if (uMatricula !== matriculaLimpia) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula no coincide' };
  }

  const uPassword = typeof usuarioEncontrado.password === 'string' ? usuarioEncontrado.password.trim() : usuarioEncontrado.password;
  if (uPassword !== passwordLimpia) {
    return { ok: false, campoError: 'password', mensaje: 'Contraseña incorrecta' };
  }

  return { ok: true, usuario: usuarioEncontrado };
}

module.exports = { validarLogin, esFechaNacimientoValida };
