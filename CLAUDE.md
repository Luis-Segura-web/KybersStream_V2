# CLAUDE.md

Este archivo proporciona la guía para Claude Code (claude.ai/code) al trabajar con el código de este repositorio.

---

## 📌 Descripción del Proyecto

KybersStream es una aplicación de IPTV para Android construida con prácticas modernas de desarrollo.  
Utiliza la API de **Xtream Codes** para la entrega de contenido y cuenta con un sistema de perfiles multiusuario con almacenamiento seguro de credenciales.  
Debe implementar un reproductor con **una sola conexión activa** y una interfaz basada en **Jetpack Compose**, **Hilt**, **Media3/ExoPlayer**, **Navigation Compose** y arquitectura **Clean MVVM**.

---

## 📦 Sistema de Build y Comandos

### Construcción de la aplicación
```bash
# Windows (PowerShell)
.\gradlew.bat clean
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
.\gradlew.bat installDebug

# Linux/macOS
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew installDebug
````

### Ejecución de pruebas

```bash
# Pruebas unitarias
./gradlew testDebugUnitTest

# Pruebas UI
./gradlew connectedDebugAndroidTest

# Reporte de cobertura
./gradlew jacocoTestReport
```

### Lint y calidad de código

```bash
./gradlew lint
./gradlew lintDebug
```

---

## 🧰 Arquitectura

**Clean Architecture con MVVM**
Capas:

1. **Presentación**: UI con Jetpack Compose Material 3, ViewModels con StateFlow, Navigation Compose.
2. **Dominio**: Entidades, interfaces de repositorios, casos de uso, modelos de negocio.
3. **Datos**: Integración Xtream Codes (Retrofit/OkHttp/Kotlinx Serialization), DataStore/Room, repositorios concretos.

---

## 🔑 Componentes Clave

### Inyección de dependencias (Hilt)

* `AppModule.kt`: Dependencias centrales (DataStore, ExoPlayer, PlayerManager).
* `NetworkModule.kt`: Configuración de red.
* `RepositoryModule.kt`: Implementaciones de repositorios.
* `PlayerModule.kt`: Dependencias de reproductor.

### Gestión del reproductor

* `PlayerManager` singleton con ExoPlayer.
* Regla **"Una sola conexión activa"**.
* Cierre de streams previos antes de iniciar uno nuevo.

### Seguridad y red

* Soporte HTTP/HTTPS sin restricción de dominio.
* Credenciales encriptadas (`SecurityManager`).
* `AuthenticationInterceptor` para autenticación API.

---

## 🖥️ Pantallas y Funcionalidad

### 1) Pantalla de inicio (Splash)

* Logo y nombre centrados.
* Redirección automática a Login o Home según sesión.

### 2) Pantalla de login

* Campos: Servidor (http o https), Usuario, Contraseña.
* Selector de perfil guardado.
* Botón para iniciar sesión.
* Validación y mensajes claros.
* Guardado seguro de credenciales.

### 3) Pantalla principal con Bottom Navigation

Pestañas: **Inicio**, **TV**, **Películas**, **Series**.

**Inicio:**

* Banner/carrusel de recomendados.
* Continuar viendo.
* Recientes por categoría.

**TV:**

* Reproductor en parte superior.
* Barra de búsqueda.
* Categorías desplegables.
* Lista de canales (tabla abajo).

**Películas:**

* Barra de búsqueda.
* Categorías desplegables.
* Botón para alternar vista (cuadrícula/lista).
* Lista de películas (tabla abajo).

**Series:**

* Barra de búsqueda.
* Categorías desplegables.
* Botón para alternar vista.
* Lista de series (tabla abajo).
* Pantalla de detalle con temporadas y episodios.

### 4) Pantalla de ajustes

* Cambiar usuario.
* Preferencias de reproducción (calidad, subtítulos).
* Preferencias de EPG (formato de hora, zona horaria).
* Tema claro/oscuro.
* Gestión de favoritos.
* Limpiar caché / cerrar sesión.
* Información de la app.

---

## 📋 Elementos obligatorios en listas

| Sección       | Elementos                                                                                                                                                                | Notas                                                       |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------- |
| **TV**        | - Logo del canal (48–64px).<br>- Nombre (máx. 3 líneas).<br>- EPG actual: `hora - programa`.<br>- EPG siguiente: `hora - programa`.<br>- Botón de favorito a la derecha. | Barra de progreso opcional para programa actual.            |
| **Películas** | - Póster (200×300px).<br>- Título (máx. 2 líneas).<br>- Año.<br>- Duración.<br>- Calidad.<br>- Género.                                                                   | Vista lista/cuadrícula intercambiable.                      |
| **Series**    | - Miniatura.<br>- Título (máx. 2 líneas).<br>- Temporadas/Episodios.<br>- Género.<br>- Calidad.                                                                          | Vista lista/cuadrícula; en detalle, temporadas y episodios. |

---

## ⚙️ Regla de una sola conexión

1. Antes de iniciar nueva reproducción, liberar la anterior.
2. Detener y limpiar ExoPlayer.
3. Cerrar streams HLS y conexiones de red.
4. Cancelar coroutines relacionadas.
5. Al cambiar usuario/servidor, invalidar sesión y limpiar estado.
6. Notificar estado a la UI mediante StateFlow.

---

## 📦 Tecnologías y dependencias

* **Android**: minSdk 24, targetSdk 36, compileSdk 36
* **Kotlin**: 2.2.x con Compose Compiler
* **AGP**: 8.12.0
* **Hilt**: versión actual en `libs.versions.toml` (KSP)
* **Media3 ExoPlayer**
* **Retrofit + OkHttp** o **Ktor**
* **Kotlinx Serialization**
* **DataStore** (Preferences) y opcionalmente Room
* **Coil Compose**

---

## 📜 Reglas de desarrollo

* Seguir la estructura de paquetes `com.kybers.stream.*`.
* Nombres claros y siguiendo convenciones de Kotlin.
* Comentarios y cadenas en español latino.
* Mantener la arquitectura Clean.
* No registrar datos sensibles en logs.
* Usar `SecurityManager` para almacenamiento seguro.
* Validar URLs con `NetworkSecurityUtils`.

---

## 🧪 Estrategia de pruebas

* Pruebas unitarias con MockK, Truth, Turbine.
* Pruebas UI con Compose.
* Pruebas de integración con MockWebServer.
* Uso de `jacocoTestReport` para cobertura.

---

## 📑 Entregables esperados

1. Código funcional con todas las pantallas.
2. Módulos de Hilt completos.
3. Navegación completa.
4. Integración Xtream Codes (auth, live, vod, series, epg).
5. Reproductor con gestión de conexión única.
6. Persistencia de usuarios, favoritos y ajustes.
7. README actualizado.
8. Pruebas pasando.