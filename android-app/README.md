# STU BUAP — Aplicación Móvil Android (HU-01 / BICEP & CORRECT)

Proyecto nativo de **Android (Kotlin + Jetpack Compose + JUnit 4)** con arquitectura **MVVM (Model-View-ViewModel)** y **Navigation Compose**, compatible desde **Android 7.0 (API 24)** hasta **Android 17 (API 37)**.

---

## 📁 Estructura del Proyecto (Módulo Único `:app`)

El código está organizado de forma limpia por responsabilidades en paquetes dedicados:

```
android-app/
├── gradle/
│   └── libs.versions.toml                     # Catálogo de dependencias oficiales
├── app/
│   ├── build.gradle.kts                       # Configuración y dependencias de Jetpack Compose
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/mx/buap/stu/
│       │       ├── auth/                      # Lógica de validación pura BICEP/CORRECT
│       │       │   └── ValidadorLogin.kt
│       │       ├── data/                      # Modelos y contratos de datos (preparado para Room/SQLite)
│       │       │   ├── UsuarioSTU.kt
│       │       │   ├── ResultadoLogin.kt
│       │       │   └── RepositorioUsuario.kt
│       │       ├── ui/
│       │       │   ├── navigation/            # Rutas y control de navegación con NavHost
│       │       │   │   └── NavegacionApp.kt
│       │       │   ├── screens/               # Pantallas e interfaces Compose (Figma)
│       │       │   │   ├── PantallaIngreso.kt
│       │       │   │   └── PantallaPerfil.kt
│       │       │   ├── viewmodels/            # Lógica de presentación y estado reactivo (MVVM)
│       │       │   │   └── IngresoViewModel.kt
│       │       │   └── theme/                 # Colores institucionales BUAP y estilos
│       │       │       └── TemaStu.kt
│       │       └── MainActivity.kt            # Punto de entrada de la app
│       └── test/
│           └── java/mx/buap/stu/auth/
│               └── ValidadorLoginTest.kt      # 19 pruebas unitarias automatizadas (PF1 a PF19)
```

---

## 🧪 Pruebas Unitarias Automatizadas (JUnit 4)
Para ejecutar las **19 pruebas funcionales BICEP/CORRECT**:

```powershell
cd "d:\Ingieneria de software\Ingieneria de software ii\stu-buap\android-app"
.\gradlew.bat testDebugUnitTest --rerun-tasks
```

Reporte esperado:
```text
 APROBADO  app/src/test/java/mx/buap/stu/auth/ValidadorLoginTest.kt
  Módulo de Inicio de Sesión STU Android (HU-01 - BICEP/CORRECT)
  √ PF1 a PF19 (19 aprobadas, 19 total | Tiempo: < 2 s)
```

---

## 📱 Guía para Compilar, Depurar y Ejecutar vía USB-C en tu Celular

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

## 🔑 Credenciales válidas para ingresar
- **Correo institucional:** `Javier.sangines@alumno.buap.mx` (o `Javier.sangines@correo.buap.mx`)
- **Matrícula:** `202268046`
- **Contraseña (Fecha de nacimiento DDMMAA):** `290504`
