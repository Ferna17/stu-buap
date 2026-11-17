const DOMINIOS_BUAP = ['alumno.buap.mx', 'alm.buap.mx', 'correo.buap.mx'];
const LONGITUD_MIN_PASSWORD = 8;
const LONGITUD_MAX_PASSWORD = 20;
const MENSAJE_PASSWORD_INVALIDA = 'La contraseña debe tener entre 8 y 20 caracteres sin espacios';

/**
 * Valida la regla de contraseña personalizada (8 a 20 caracteres sin espacios en blanco).
 */
function validarContrasena(password) {
  if (typeof password !== 'string') {
    return { ok: false, campoError: 'password', mensaje: MENSAJE_PASSWORD_INVALIDA };
  }
  if (
    password.includes(' ') ||
    /\s/.test(password) ||
    password.length < LONGITUD_MIN_PASSWORD ||
    password.length > LONGITUD_MAX_PASSWORD
  ) {
    return { ok: false, campoError: 'password', mensaje: MENSAJE_PASSWORD_INVALIDA };
  }
  return { ok: true };
}

function validarLogin(email, matricula, password, baseDeDatos = []) {
  // 1. Validación de tipos primitivos (Defensa contra Type Confusion / DoS)
  if (typeof email !== 'string' || typeof matricula !== 'string' || typeof password !== 'string') {
    return { ok: false, campoError: 'todos', mensaje: 'Completa tu correo, matrícula y contraseña para entrar' };
  }

  // Rechazar inyecciones de caracteres de control (CRLF, saltos de línea, null bytes)
  if (/[\r\n\0]/.test(email) || /[\r\n\0]/.test(matricula) || /[\r\n\0]/.test(password)) {
    const campo = /[\r\n\0]/.test(matricula) ? 'matricula' : (/[\r\n\0]/.test(email) ? 'email' : 'password');
    return { ok: false, campoError: campo, mensaje: 'Entrada con caracteres de control no permitidos' };
  }

  // Sanitización de correo y matrícula contra espacios accidentales; la contraseña no admite espacios
  const emailLimpio = email.trim();
  const matriculaLimpia = matricula.trim();

  if (emailLimpio.length === 0 || matriculaLimpia.length === 0 || password.trim().length === 0) {
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

  // 4. Contraseña: 8 a 20 caracteres sin espacios en blanco
  const resPassword = validarContrasena(password);
  if (!resPassword.ok) {
    return resPassword;
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

  const uPassword = typeof usuarioEncontrado.password === 'string' ? usuarioEncontrado.password : '';
  if (uPassword !== password) {
    return { ok: false, campoError: 'password', mensaje: 'Contraseña incorrecta' };
  }

  return { ok: true, usuario: usuarioEncontrado };
}

module.exports = {
  validarLogin,
  validarContrasena,
  LONGITUD_MIN_PASSWORD,
  LONGITUD_MAX_PASSWORD,
  MENSAJE_PASSWORD_INVALIDA
};
