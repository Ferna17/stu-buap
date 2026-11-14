# STU BUAP — Aplicación Móvil Android (HU-01 & HU-01.1 / BICEP & CORRECT)

Proyecto nativo de **Android (Kotlin + Jetpack Compose + JUnit 4)** con arquitectura **MVVM (Model-View-ViewModel)** y **Navigation Compose**, compatible desde **Android 7.0 (API 24)** hasta **Android 17 (API 37)**.

Implementa el subsistema completo de autenticación institucional BUAP en pantallas separadas (`PantallaIngreso`, `PantallaRegistro` y `PantallaPerfil`), compatible con **Android 12, Android 14 y tablet Android 16** (`safeDrawingPadding()` + scroll adaptable):
- **HU-01 (Sprint 1):** Inicio de sesión (`PantallaIngreso`) con validación BICEP/CORRECT (`PF1`–`PF19`).
- **HU-01.1 (Sprint 2):** Registro de nuevo estudiante (`PantallaRegistro`) con validación BICEP/CORRECT (`PR1`–`PR16`), captura obligatoria de **`Nombre(s)`** y **`Apellidos`** reflejados en `Perfil`, bloqueo de matrícula/correo duplicados y alta con saldo inicial `0` pasajes y estado `ACTIVO`.

---

## 📁 Estructura del Proyecto (Módulo Único `:app`)

El código está organizado de forma limpia por responsabilidades en paquetes dedicados:

```text
android-app/
├── gradle/
│   └── libs.versions.toml                     # Catálogo de dependencias oficiales
├── app/
│   ├── build.gradle.kts                       # Configuración Compose + listener de pruebas PF/PR
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/mx/buap/stu/
│       │       ├── auth/                      # Lógica de validación pura BICEP/CORRECT
│       │       │   ├── ValidadorCredenciales.kt
│       │       │   ├── ValidadorLogin.kt      # HU-01 (PF1 a PF19)
│       │       │   └── ValidadorRegistro.kt   # HU-01.1 (PR1 a PR16)
│       │       ├── data/                      # Modelos y contratos de datos (preparado para Room/SQLite)
│       │       │   ├── UsuarioSTU.kt
│       │       │   ├── ResultadoLogin.kt
│       │       │   ├── ResultadoRegistro.kt
│       │       │   └── RepositorioUsuario.kt
│       │       ├── ui/
│       │       │   ├── navigation/            # Rutas y control de navegación con NavHost
│       │       │   │   └── NavegacionApp.kt   # Ingreso <-> Registro -> Ingreso (Flujo B) -> Perfil
│       │       │   ├── screens/               # Pantallas e interfaces Compose (Figma)
│       │       │   │   ├── ComponentesAuthStu.kt
│       │       │   │   ├── PantallaIngreso.kt
│       │       │   │   ├── PantallaRegistro.kt
│       │       │   │   └── PantallaPerfil.kt
│       │       │   ├── viewmodels/            # Lógica de presentación y estado reactivo (MVVM)
│       │       │   │   ├── MedidorMetricasCaptura.kt
│       │       │   │   ├── IngresoViewModel.kt
│       │       │   │   └── RegistroViewModel.kt
│       │       │   └── theme/                 # Colores institucionales BUAP y estilos
│       │       │       └── TemaStu.kt
│       │       └── MainActivity.kt            # Punto de entrada de la app
│       ├── androidTest/
│       │   └── java/mx/buap/stu/
│       │       └── AplicacionStuTest.kt       # Pruebas UI Compose (Ingreso, Registro, Flujo B y Perfil)
│       └── test/
│           └── java/mx/buap/stu/auth/
│               ├── ValidadorLoginTest.kt      # 19 pruebas unitarias (PF1 a PF19)
│               └── ValidadorRegistroTest.kt   # 16 pruebas unitarias (PR1 a PR16)
```

---

## 🧭 Flujo de Registro e Ingreso (Pantallas Separadas HU-01 y HU-01.1)

1. En **`PantallaIngreso`**, el estudiante puede ir a la pantalla separada de registro de dos maneras:
   - Enlace directo **`"¿No estás registrado? Regístrate"`** debajo del botón **"Inicio"**.
   - Aviso contextual cuando el validador de Login detecta **`PF18: Sin registro previo`** (`"Sin registro previo. No estás registrado, debes registrarte para acceder al STU."`) junto al botón resaltado **`"Registrarse en el padrón STU"`** (precargando correo, matrícula y contraseña en `PantallaRegistro`).
2. En **`PantallaRegistro`** (`"Registro de usuario"`), se capturan los **5 campos obligatorios** (**`Nombre(s)`**, **`Apellidos`**, `Correo electrónico`, `Matrícula`, `Contraseña DDMMAA`) y se presiona **"Registrarse"** (o **`"¿Ya tienes cuenta? Inicia sesión"`** para volver al Login).
3. Si los datos cumplen formato y no existen duplicados en el padrón (`PR1`, `PR2`, `PR13`), se registra al `UsuarioSTU` con su **nombre completo normalizado**, **`0 viajes activos`** y estado **`ACTIVO`**, retornando automáticamente a **`PantallaIngreso`** con sus credenciales listas y aviso de éxito (**Flujo B**) para ingresar con **"Inicio"** a **`PantallaPerfil`** (`"Perfil"`).

---

## 🧪 Pruebas Unitarias Automatizadas (JUnit 4 y Jest)

### Android (JUnit 4 — 35 pruebas funcionales: 19 PF + 16 PR)
```powershell
cd "d:\Ingieneria de software\Ingieneria de software ii\stu-buap\android-app"
.\gradlew.bat testDebugUnitTest --rerun-tasks
```

Reporte esperado:
```text
 APROBADO  app/src/test/java/mx/buap/stu/auth/ValidadorLoginTest.kt
  Módulo de Inicio de Sesión STU Android (HU-01 - BICEP/CORRECT)
  √ PF1 a PF19

 APROBADO  app/src/test/java/mx/buap/stu/auth/ValidadorRegistroTest.kt
  Módulo de Registro de Nuevo Estudiante STU Android (HU-01.1 - BICEP/CORRECT)
  √ PR1 a PR16

Suites de Prueba: 2 aprobadas, 2 total | Pruebas: 35 aprobadas, 35 total
```

### JavaScript / Node.js (Rama `tests` — Jest: 56 pruebas: 19 PF + 16 PR + 21 Adversariales)
```powershell
cd "d:\Ingieneria de software\Ingieneria de software ii\stu-buap"
git switch tests
npm test
```

---

## 📱 Guía para Instalar la APK en tus Dispositivos (WhatsApp o USB-C)

### Opción A: Envío directo por WhatsApp (Android 12, Android 14 y Tablet Android 16)
1. Compila la APK debug si realizaste cambios nuevos:
   ```powershell
   cd "d:\Ingieneria de software\Ingieneria de software ii\stu-buap\android-app"
   .\gradlew.bat assembleDebug
   ```
2. Archivo generado listo para instalar:
   `android-app\app\build\outputs\apk\debug\app-debug.apk`
3. Abre **WhatsApp Web / Desktop**, adjunta el archivo usando **Documento** (📎) para enviarlo a tu dispositivo, descárgalo y ábrelo (habilita *"Permitir desde esta fuente"* para WhatsApp la primera vez).

### Opción B: Depuración y ejecución vía USB-C

### 1. Activar Opciones de Desarrollador en tu teléfono
1. Abre **Ajustes** en tu celular.
2. Ve a **Acerca del teléfono** (o *Información de software*).
3. Pulsa **7 veces consecutivas** sobre **Número de compilación** (o *Versión de MIUI/HyperOS* si es Xiaomi).

### 2. Activar la Depuración por USB
1. Ve a **Ajustes** -> **Opciones de desarrollador**.
2. Activa **Depuración por USB** (*USB debugging*).
3. *(Si es Xiaomi/Redmi/POCO)*: Activa también **Instalar vía USB**.

### 3. Conectar el celular a tu PC
1. Conecta el cable **USB-C** a la computadora.
2. En la pantalla del celular aparecerá el aviso: **"¿Permitir depuración por USB?"**.
3. Marca **"Permitir siempre desde esta computadora"** y pulsa **Aceptar**.

### 4. Verificar la conexión en PowerShell
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```
Debe listar tu dispositivo en estado `device`.

### 5. Instalar y Ejecutar la App
* **Desde Android Studio**:
  Abre la carpeta `android-app` en Android Studio, selecciona tu dispositivo físico en la barra superior y pulsa el botón verde **Run (Shift + F10)**.
* **Desde la terminal**:
  ```powershell
  cd "d:\Ingieneria de software\Ingieneria de software ii\stu-buap\android-app"
  .\gradlew.bat installDebug
  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n mx.buap.stu/.MainActivity
  ```

---

## 🔑 Datos de prueba (Padrón inicial y Nuevo Registro)
- **Usuario precargado (Login / prueba de duplicados PR10–PR11):**
  - **Correo:** `Javier.sangines@alumno.buap.mx` (o `Javier.sangines@correo.buap.mx`)
  - **Matrícula:** `202268046`
  - **Contraseña (DDMMAA):** `290504`
- **Usuario nuevo válido (Registro PR1):**
  - **Correo:** `fernando.hernandez@alumno.buap.mx`
  - **Matrícula:** `202112345`
  - **Contraseña (DDMMAA):** `170301`
