// src/auth/registerValidator.js

const DOMINIOS_BUAP = ['alumno.buap.mx', 'alm.buap.mx', 'correo.buap.mx'];

function validarRegistro(email, matricula, password, baseDeDatos = []) {
  // 1. Verificar campos vacíos (PR12)
  if (!email || !matricula || !password) {
    return {
      ok: false,
      campoError: 'todos',
      mensaje: 'Completa tu correo, matrícula y contraseña para registrarte'
    };
  }

  // 2. Validar formato del correo (PR8)
  if (!email.includes('@')) {
    return { ok: false, campoError: 'email', mensaje: 'Formato inválido de correo electrónico' };
  }

  const partes = email.split('@');
  if (partes.length !== 2) {
    return { ok: false, campoError: 'email', mensaje: 'Formato de correo no permitido' };
  }

  const [usuario, dominio] = partes;

  // Límites de 1 a 30 caracteres antes del @ (PR3, PR4)
  if (usuario.length === 0) {
    return { ok: false, campoError: 'email', mensaje: 'Ingresa un usuario válido antes del @' };
  }
  if (usuario.length > 30) {
    return { ok: false, campoError: 'email', mensaje: 'El correo excede el máximo de 30 caracteres antes del @' };
  }

  // Dominio institucional BUAP (PR7)
  if (!DOMINIOS_BUAP.includes(dominio.toLowerCase())) {
    return { ok: false, campoError: 'email', mensaje: 'Usa un dominio institucional BUAP válido' };
  }

  // 3. Validar matrícula: exactamente 9 dígitos numéricos (PR5, PR9)
  if (!/^\d+$/.test(matricula)) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula solo admite dígitos numéricos' };
  }
  if (matricula.length !== 9) {
    return { ok: false, campoError: 'matricula', mensaje: 'La matrícula debe tener exactamente 9 dígitos' };
  }

  // 4. Validar contraseña: exactamente 6 dígitos numéricos DDMMAA (PR6, PR9)
  if (!/^\d{6}$/.test(password)) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña debe contener 6 dígitos (DDMMAA)' };
  }

  const dia = parseInt(password.slice(0, 2), 10);
  const mes = parseInt(password.slice(2, 4), 10);
  if (dia < 1 || dia > 31 || mes < 1 || mes > 12) {
    return { ok: false, campoError: 'password', mensaje: 'La contraseña no corresponde a una fecha válida' };
  }

  // 5. Verificar no duplicidad en el padrón (Reference - CORRECT) (PR10, PR11)
  const matriculaExiste = baseDeDatos.some(u => u.matricula === matricula);
  if (matriculaExiste) {
    return {
      ok: false,
      campoError: 'matricula',
      mensaje: 'La matrícula ya cuenta con un registro previo'
    };
  }

  const correoExiste = baseDeDatos.some(u => u.email.toLowerCase() === email.toLowerCase());
  if (correoExiste) {
    return {
      ok: false,
      campoError: 'email',
      mensaje: 'El correo institucional ya está registrado'
    };
  }

  // 6. Crear nuevo usuario con saldo inicial de 0 pasajes (PR1, PR2)
  const nuevoUsuario = {
    email,
    matricula,
    password,
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
