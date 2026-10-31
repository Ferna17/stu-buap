const DOMINIOS_BUAP = ['alumno.buap.mx', 'alm.buap.mx', 'correo.buap.mx'];

function validarLogin(email, matricula, password, baseDeDatos = []) {
  if (!email || !matricula || !password) {
    return { ok: false, campoError: 'todos', mensaje: 'Completa tu correo, matrícula y contraseña para entrar' };
  }

  if (!email.includes('@')) {
    return { ok: false, campoError: 'email', mensaje: 'Formato inválido de correo electrónico' };
  }

  const partes = email.split('@');
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

  if (!DOMINIOS_BUAP.includes(dominio.toLowerCase())) {
    return { ok: false, campoError: 'email', mensaje: 'Usa un dominio institucional BUAP válido' };
  }

  if (!/^\d+$/.test(matricula)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula solo admite dígitos numéricos' };
  }
  if (matricula.length !== 9) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula debe tener exactamente 9 dígitos' };
  }

  if (!/^\d{6}$/.test(password)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña debe contener 6 dígitos (DDMMAA)' };
  }

  const dia = parseInt(password.slice(0, 2), 10);
  const mes = parseInt(password.slice(2, 4), 10);
  if (dia < 1 || dia > 31 || mes < 1 || mes > 12) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña no corresponde a una fecha válida' };
  }

 const usuarioEncontrado = baseDeDatos.find(
  u => u.matricula === matricula && u.email.toLowerCase() === email.toLowerCase()
) || baseDeDatos.find(
  u => u.matricula === matricula || u.email.toLowerCase() === email.toLowerCase()
);

  if (!usuarioEncontrado) {
    return { ok: false, campoError: 'registro', mensaje: 'Sin registro previo', puedeRegistrarse: true };
  }
  if (usuarioEncontrado.email.toLowerCase() !== email.toLowerCase()) {
    return { ok: false, campoError: 'email', mensaje: 'El correo no coincide' };
  }
  if (usuarioEncontrado.matricula !== matricula) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula no coincide' };
  }
  if (usuarioEncontrado.password !== password) {
    return { ok: false, campoError: 'password', mensaje: 'Contraseña incorrecta' };
  }

  return { ok: true, usuario: usuarioEncontrado };
}

module.exports = { validarLogin };