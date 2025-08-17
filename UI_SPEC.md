# 🎨 **UI SPECIFICATION - KybersStream V2**

## ✅ **ETAPA 9: Rediseño Integral de la App**

### **Especificación detallada por pantalla que se apoya en ETAPAS 1–8**

---

## 🔁 **Fundamentos Reutilizados de Etapas Anteriores**

### **ETAPA 1**: Arquitectura y Base Técnica
- ✅ Clean Architecture por capas
- ✅ Jetpack Compose + Material 3
- ✅ Navegación base con Navigation Compose
- ✅ Accesibilidad completa
- ✅ Idioma ES-419 (español latino)

### **ETAPA 2**: Sistema de Perfiles
- ✅ Perfiles en DataStore cifrado
- ✅ Flujo `Splash → Login → Home`
- ✅ Gestión segura de credenciales

### **ETAPA 3**: Integración de Datos
- ✅ Contrato Xtream Codes (categorías, listas, detalle, EPG)
- ✅ Cancelación inteligente de llamadas
- ✅ Manejo de errores de red

### **ETAPA 4**: Sistema de Reproducción
- ✅ `PlaybackManager` **@Singleton**
- ✅ **Una sola conexión activa** estricta
- ✅ Soporte HLS/TS/progresivo con ExoPlayer

### **ETAPA 5**: Experiencia de Usuario
- ✅ Sistema de favoritos
- ✅ Continuar viendo con progreso
- ✅ Búsqueda cancelable con debounce
- ✅ Vistas lista/cuadrícula intercambiables
- ✅ Ajustes persistentes

### **ETAPA 6**: Contenido Avanzado
- ✅ EPG avanzado (Now/Next, timeline)
- ✅ Fichas ricas VOD/Series
- ✅ Gestión de temporadas/episodios
- ✅ Carruseles de discovery

### **ETAPA 7**: Robustez y Seguridad
- ✅ Control parental con PIN cifrado
- ✅ Resiliencia de red con reintentos
- ✅ Telemetría anónima opt-in
- ✅ UX pulido con accesibilidad

### **ETAPA 8**: Preparación para Producción
- ✅ Hardening de seguridad
- ✅ Documentación completa
- ✅ Migraciones y mantenimiento

---

## 🎛️ **Sistema de Diseño (Base del Rediseño)**

### **1. Tokens y Temas (Material 3)**

#### **Colores**
```kotlin
// Usar roles Material 3 estrictos
primary/secondary/tertiary
surface/surfaceContainer/surfaceVariant
outline/outlineVariant
error/errorContainer
onPrimary/onSecondary/onSurface/onError

// Soporte completo para:
- Tema claro/oscuro automático
- Dynamic Color (Android 12+)
- Paleta estática como fallback
```

#### **Tipografía (Roles M3)**
```kotlin
// Jerarquía tipográfica estricta
displayLarge    // Hero sections
titleLarge      // Encabezados de pantalla
titleMedium     // Títulos de sección
bodyLarge       // Texto principal
bodyMedium      // Texto por defecto
bodySmall       // Metadatos y descripcioes
labelLarge      // Botones principales
labelMedium     // Chips y botones pequeños
labelSmall      // Captions y timestamps
```

#### **Forma y Espaciado**
```kotlin
// Radios de forma (Shape tokens)
small = 8.dp    // Chips, small buttons
medium = 12.dp  // Cards, buttons
large = 16.dp   // Containers, sheets

// Escala de espaciado (dp)
4, 8, 12, 16, 24, 32, 48

// Mínimos táctiles
touchTarget = 48.dp × 48.dp
```

### **2. Componentes Base**

#### **Navegación**
- `TopAppBar` (center-aligned por defecto)
- `NavigationBar` para tabs principales
- `NavigationRail` en landscape/tablets
- `NavigationDrawer` opcional para funciones avanzadas

#### **Contenedores**
- `ElevatedCard` para elementos seleccionables
- `OutlinedCard` para listados densos
- `Surface` para fondos y separadores

#### **Interacción**
- `FilterChip` para categorías/géneros
- `SuggestionChip` para búsquedas
- `AssistChip` para acciones secundarias
- Estado seleccionado con `containerColor`

#### **Listas**
- `LazyColumn` con paginación automática
- `LazyVerticalGrid` adaptable
- Skeleton loading integrado
- Pull-to-refresh estándar

### **3. Accesibilidad Obligatoria**

#### **Semántica**
```kotlin
// Obligatorio en todos los componentes
contentDescription = "Descripción clara"
semantics {
    role = Role.Button
    stateDescription = "Estado actual"
}
```

#### **Contraste y Tamaño**
- Contraste AA (4.5:1) mínimo
- Soporte fuentes grandes (≥200%)
- Touch targets 48dp mínimo

#### **Navegación**
- Soporte completo TalkBack
- Navegación con teclado
- Focus management correcto

### **4. Motion y Transiciones**

#### **Duraciones Estándar**
```kotlin
short = 200.ms     // Feedback inmediato
medium = 300.ms    // Transiciones de estado
long = 500.ms      // Transiciones de pantalla
```

#### **Easing**
- `EaseStandard` para transiciones normales
- `EaseEmphasized` para cambios importantes
- `EaseDecelerated` para entrada de elementos

---

## 📐 **Patrones Transversales**

### **Estados Globales**
1. **Cargando**: Skeleton components específicos por contenido
2. **Vacío**: Ilustración + mensaje + CTA contextual
3. **Error**: Icono + mensaje claro + botón "Reintentar"
4. **Offline**: Banner persistente + modo degradado

### **Gestión de Red**
- Banner "Sin conexión" no intrusivo
- Reintentos automáticos con backoff exponencial
- Indicadores de calidad de conexión
- Modo offline para contenido cacheado

### **Persistencia de UI**
- Recordar vista lista/cuadrícula por sección
- Última posición de scroll
- Filtros aplicados por sesión
- Tema y preferencias globales

### **Telemetría Mínima (Opt-in)**
```kotlin
// Eventos esenciales solamente
screen_view(screenName)
click_action(element, context)
error_occurred(type, context)
performance_metric(name, value)
```

---

## 🖥️ **Especificación Detallada por Pantalla**

### **1. Splash Screen**

#### **Layout**
```
┌─────────────────────┐
│                     │
│        LOGO         │ ← Centrado, 120dp
│    KybersStream     │ ← titleLarge
│                     │
│   [Cargando...]     │ ← bodyMedium, opcional
└─────────────────────┘
```

#### **Especificaciones**
- **Tiempo máximo**: 2000ms o hasta resolver perfil
- **Logo**: 120dp, adaptive icon
- **Fondo**: `surface` color
- **Estados**:
  - Normal: Logo + nombre
  - Sin conexión: Solo logo
  - Migración: Logo + "Actualizando datos..."

#### **Comportamiento**
- Auto-navegación a Login o Home según sesión
- Sin interacción del usuario
- Respeta tema claro/oscuro del sistema

---

### **2. Login Screen**

#### **Layout**
```
┌─────────────────────────┐
│   ← [Logo] Login        │ ← TopAppBar
├─────────────────────────┤
│                         │
│  Servidor               │ ← OutlinedTextField
│  ┌─────────────────────┐ │
│  │http://example.com   │ │
│  └─────────────────────┘ │
│                         │
│  Usuario                │
│  ┌─────────────────────┐ │
│  │                     │ │
│  └─────────────────────┘ │
│                         │
│  Contraseña             │
│  ┌─────────────────────┐ │
│  │••••••••••••••••••••│ │
│  └─────────────────────┘ │
│                         │
│  ☐ Recordar usuario     │ ← Checkbox
│                         │
│  ┌─────────────────────┐ │
│  │   INICIAR SESIÓN    │ ← Button primary
│  └─────────────────────┘ │
│                         │
│  Ver perfiles guardados │ ← TextButton
└─────────────────────────┘
```

#### **Especificaciones**
- **Campos**: Obligatorios, validación en tiempo real
- **Servidor**: Acepta http/https, validación de URL
- **Error handling**: Mensajes claros bajo cada campo
- **Perfil selector**: BottomSheet con lista de perfiles

#### **Validaciones**
```kotlin
// Servidor
- No vacío
- Formato URL válido
- Protocolo http/https

// Usuario/Contraseña
- No vacíos
- Mínimo 3 caracteres
```

#### **Estados de Error**
- Credenciales incorrectas
- Servidor no accesible
- Límite de conexiones
- Error de red

---

### **3. Home Screen (Dashboard)**

#### **Bottom Navigation**
```
┌─────┬─────┬─────┬─────┬─────┐
│Home │ TV  │Movie│Serie│Sett.│
└─────┴─────┴─────┴─────┴─────┘
```

#### **Layout Inicio**
```
┌───────────────────────────────┐
│ KybersStream    🔍 ⚙️         │ ← TopAppBar
├───────────────────────────────┤
│                               │
│ ┌─────────────────────────┐   │
│ │    HERO CAROUSEL        │   │ ← 16:9 ratio
│ │  [Auto-playing banner]  │   │
│ └─────────────────────────┘   │
│                               │
│ Continuar viendo         →    │ ← titleMedium
│ ○○○○○○○○○○○○○○○○○○           │ ← Horizontal scroll
│                               │
│ Favoritos               →     │
│ ○○○○○○○○○○○○○○○○○○           │
│                               │
│ Recomendados           →      │
│ ○○○○○○○○○○○○○○○○○○           │
│                               │
│ Recientes              →      │
│ ○○○○○○○○○○○○○○○○○○           │
└───────────────────────────────┘
```

#### **Especificaciones Hero Carousel**
- **Ratio**: 16:9 obligatorio
- **Auto-play**: Configurable en ajustes (off por defecto)
- **Indicadores**: Dots en bottom con current/total
- **Contenido**: Destacados, estrenos, recomendados

#### **Carruseles Horizontales**
```kotlin
// Dimensiones estándar
itemWidth = 160.dp      // Pósters
itemHeight = 240.dp     // Ratio 2:3
spacing = 12.dp         // Entre items
contentPadding = 16.dp  // Lateral
```

#### **Secciones Dinámicas**
1. **Continuar viendo** - Solo si hay progreso
2. **Favoritos** - Solo si hay items marcados
3. **Recomendados** - Algoritmo o contenido destacado
4. **Por categoría** - Top de cada categoría
5. **Recientes** - Últimos añadidos

---

### **4. TV Screen**

#### **Layout Principal**
```
┌─────────────────────────────┐
│ TV          🔍 ┌─────┐ ⚙️  │ ← TopAppBar + filtros
│                │ Cat ▼│     │
│                └─────┘     │
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │    VIDEO PLAYER         │ │ ← 16:9, expandible
│ │    [Live stream]        │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│                             │
│ ┌────────────────────────┐  │ ← Lista de canales
│ │[📺] Canal 1     [♥]   │  │   72-80dp altura
│ │     Ahora: 14:30-15:00 │  │
│ │     Sig: 15:00-16:30   │  │
│ └────────────────────────┘  │
│ ┌────────────────────────┐  │
│ │[📺] Canal 2     [♡]   │  │
│ │     Ahora: Película X  │  │
│ │     Sig: Noticias      │  │
│ └────────────────────────┘  │
│ ...                         │
└─────────────────────────────┘
```

#### **Especificaciones Player**
- **Zona**: 16:9 ratio, expandible a fullscreen
- **Controles**: Tap para mostrar/ocultar (3s timeout)
- **Single connection**: Solo un stream activo
- **Estados**: Loading, Playing, Error, No signal

#### **Item de Canal (Anatomía)**
```
┌─────┬─────────────────────────┬───┐
│Logo │ Nombre Canal            │ ♥ │ ← 72-80dp total
│56dp │ Ahora: 14:30 - Programa │   │
│     │ Sig: 15:00 - Siguiente  │   │
│     │ ▓▓▓▓▓░░░░░░░░░░░░░░░░   │   │ ← Progress bar
└─────┴─────────────────────────┴───┘
```

#### **Especificaciones Lista**
- **Logo**: 56dp cuadrado, fallback a inicial
- **Nombre**: 3 líneas máximo, ellipsis
- **EPG**: Now/Next inmediato desde API
- **Progress**: Opcional, 2-4dp altura
- **Touch**: 48dp mínimo para accesibilidad

#### **Filtros y Búsqueda**
- **Search**: Debounce 300ms, cancela anterior
- **Categorías**: Chips horizontales, selección múltiple
- **Estados**: All, Favoritos, Por categoría

---

### **5. Movies Screen**

#### **Layout con Toggle Vista**
```
┌─────────────────────────────┐
│ Películas   🔍 ⚙️ [≡][⚏]  │ ← Toggle lista/grid
├─────────────────────────────┤
│ ┌─────┬─────┬─────┬─────┐   │ ← Filtros (chips)
│ │ All │Cat1 │Cat2 │...  │   │
│ └─────┴─────┴─────┴─────┘   │
├─────────────────────────────┤
│                             │
│ VISTA CUADRÍCULA (2-3 cols) │
│ ┌─────┐ ┌─────┐ ┌─────┐    │
│ │ [🖼] │ │ [🖼] │ │ [🖼] │    │ ← Pósters 2:3
│ │Título│ │Título│ │Título│    │
│ │2023•│ │2023•│ │2023•│     │ ← Metadatos
│ │120m │ │95m  │ │110m │     │
│ └─────┘ └─────┘ └─────┘    │
│                             │
│ VISTA LISTA (alternativa)   │
│ ┌─────┬─────────────────┬─┐ │
│ │[🖼] │ Título película │♥│ │ ← 72dp altura
│ │ 80dp│ 2023•120m•HD   │ │ │
│ │     │ Género1, Gen2   │ │ │
│ └─────┴─────────────────┴─┘ │
└─────────────────────────────┘
```

#### **Especificaciones Póster (Grid)**
```kotlin
// Dimensiones grid
columns = adaptiveColumns(minWidth = 160.dp)
posterRatio = 2f/3f  // Altura/Ancho
posterWidth = 160.dp
posterHeight = 240.dp

// Metadatos obligatorios
- Título (2 líneas máx)
- Año • Duración • Calidad
- Género principal
- Estado favorito
```

#### **Especificaciones Lista**
```
┌─────┬──────────────────────────┬───┐
│Póst.│ Título de la Película    │ ♥ │ ← 72dp altura
│80dp │ 2023 • 120min • HD      │   │ ← Metadatos línea 1
│     │ Acción, Drama           │   │ ← Géneros línea 2
└─────┴──────────────────────────┴───┘
```

#### **Filtros Avanzados**
- **Categorías**: API Xtream categories
- **Género**: Extraído de metadata
- **Calidad**: HD/FHD/4K/SD
- **Año**: Range picker
- **Alfabético**: A-Z sorting

---

### **6. Series Screen**

#### **Layout Similar a Movies**
```
┌─────────────────────────────┐
│ Series      🔍 ⚙️ [≡][⚏]  │
├─────────────────────────────┤
│ ┌─────┬─────┬─────┬─────┐   │
│ │ All │Cat1 │Cat2 │...  │   │
│ └─────┴─────┴─────┴─────┘   │
├─────────────────────────────┤
│ ┌─────┐ ┌─────┐ ┌─────┐    │
│ │ [🖼] │ │ [🖼] │ │ [🖼] │    │
│ │Título│ │Título│ │Título│    │
│ │3T•24E│ │2T•18E│ │1T•10E│    │ ← Temporadas/Episodios
│ │Drama │ │Comed.│ │Sci-Fi│    │
│ └─────┘ └─────┘ └─────┘    │
└─────────────────────────────┘
```

#### **Especificaciones Series**
- **Igual que Movies** pero metadatos específicos:
  - Temporadas • Episodios
  - Género principal
  - Estado de visualización
  - Progreso de temporada actual

---

### **7. Ficha VOD (Película)**

#### **Layout Detalle**
```
┌─────────────────────────────────┐
│ ← Título Película        ♥ ⚙️  │ ← TopAppBar
├─────────────────────────────────┤
│ ┌─────┐  TÍTULO PRINCIPAL       │
│ │     │  ★★★★☆ 2023•120min     │ ← Header info
│ │Póst.│  Acción, Drama, Thrill  │
│ │200px│                         │
│ │     │  ┌─────────┬─────────┐  │ ← Botones acción
│ └─────┘  │▶ REPROD.│♥ FAVORIT│  │
│          └─────────┴─────────┘  │
├─────────────────────────────────┤
│                                 │
│ Sinopsis                        │ ← Expandible
│ Esta es la descripción de la    │
│ película que puede ser larga... │
│ [Ver más]                       │
│                                 │
├─────────────────────────────────┤
│ Detalles Técnicos              │
│ Director: Fulano De Tal         │
│ Reparto: Actor1, Actor2...      │
│ Duración: 120 minutos           │
│ Calidad: Full HD               │
│ Idioma: Español                │
├─────────────────────────────────┤
│ Relacionadas               →    │
│ ○○○○○○○○○○○○○○○○○○○○○○○      │ ← Carrusel
└─────────────────────────────────┘
```

#### **Especificaciones Header**
- **Póster**: 200×300px fijo
- **Rating**: Estrellas si disponible en metadata
- **Metadatos**: Año • Duración • Calidad en línea
- **Géneros**: Máximo 3, separados por comas

#### **Botones de Acción**
```kotlin
// Primarios (prominentes)
REPRODUCIR     // o CONTINUAR si hay progreso
FAVORITO       // Toggle estado

// Secundarios (outlined)
DESCARGAR      // Opcional, si está habilitado
COMPARTIR      // Opcional, share sheet nativo
```

---

### **8. Ficha Serie**

#### **Layout con Temporadas**
```
┌─────────────────────────────────┐
│ ← Nombre de la Serie     ♥ ⚙️  │
├─────────────────────────────────┤
│ ┌─────┐  TÍTULO SERIE           │
│ │     │  ★★★★☆ 2020-2023       │
│ │Thumb│  Drama, Thriller        │ ← Miniatura 16:9
│ │200px│  3 temporadas           │
│ └─────┘                         │
│                                 │
│ ┌─────┬─────┬─────┬─────────┐   │ ← Selector temporada
│ │ T1  │ T2  │ T3  │  All ▼ │   │
│ └─────┴─────┴─────┴─────────┘   │
├─────────────────────────────────┤
│                                 │
│ LISTA DE EPISODIOS              │
│ ┌─────┬─────────────────────┬─┐ │
│ │[img]│ 1x01 - Pilot        │▶│ │ ← 72dp altura
│ │16:9 │ 45min • Visto       │ │ │
│ └─────┴─────────────────────┴─┘ │
│ ┌─────┬─────────────────────┬─┐ │
│ │[img]│ 1x02 - Episodio 2   │▶│ │
│ │     │ 42min • ▓▓▓░░░ 60%  │ │ │ ← Progreso
│ └─────┴─────────────────────┴─┘ │
│ ...                             │
└─────────────────────────────────┘
```

#### **Especificaciones Temporadas**
- **Selector**: Chips o Dropdown, persistir selección
- **Estado**: Todo/Por ver/Visto
- **Autoplay**: Configuración global para siguiente episodio

#### **Lista Episodios**
```
┌─────┬──────────────────────────┬─┐
│Thumb│ SxEy - Título Episodio   │▶│ ← 72dp altura
│16:9 │ 45min • Estado/Progreso  │ │
│60dp │                          │ │
└─────┴──────────────────────────┴─┘
```

---

### **9. Reproductor Fullscreen**

#### **Layout Overlay (Auto-oculto)**
```
┌─────────────────────────────────┐
│ ← Canal/Título        ♥    ⋮   │ ← Top overlay
├─────────────────────────────────┤
│                                 │
│              ⏸                 │ ← Center controls
│         ⏪   ⏸   ⏩           │   (tap to show)
│                                 │
├─────────────────────────────────┤
│ ▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░     │ ← Bottom overlay
│ 14:30        1:15:30     2:30:00│   Progress + times
│ [📺] [🔊] [⚙️] [📱] [⏏]      │   Controls
└─────────────────────────────────┘
```

#### **Top Overlay**
- **Navegación**: Botón back (salir player)
- **Título**: Canal/contenido actual
- **Favorito**: Toggle inmediato
- **Menú**: Info, calidad, fuente alternativa

#### **Center Controls**
- **Play/Pause**: Tap principal
- **Skip**: ±10s para VOD/Series (doble tap)
- **Auto-hide**: 3 segundos sin interacción

#### **Bottom Overlay**

**Para Live TV:**
```
Buffer: [████████░░] Canal Local
[📺 Canales] [🔊 Audio] [⚙️ Ajustes] [📱 Cast] [⏏ Exit]
```

**Para VOD/Series:**
```
▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░
14:30        1:15:30     2:30:00
[⏮ Anterior] [🔊] [⚙️] [📱] [⏭ Siguiente]
```

#### **Estados del Player**
1. **Loading**: Spinner centrado + "Cargando stream..."
2. **Playing**: Controles normales
3. **Buffering**: Spinner + "Buffer... X%"
4. **Error**: Mensaje + "Reintentar" + "Cambiar fuente"
5. **Paused**: Controles permanentes

---

### **10. EPG Timeline (Avanzado)**

#### **Layout Horizontal**
```
┌──────┬────┬────┬────┬────┬────┬──→
│CANALES│14:0│14:3│15:0│15:3│16:0│
├──────┼────┼────┼────┼────┼────┤
│[📺]C1│████│Prog│rama│A───│────│ ← Programa actual
├──────┼────┼────┼────┼────┼────┤
│[📺]C2│────│Noti│cias│────│Peli│ ← Programas futuros
├──────┼────┼────┼────┼────┼────┤
│  │   │    │ │  │    │    │    │
│  ↓   │    │ │  │    │    │    │ ← Now line (roja)
```

#### **Especificaciones Timeline**
- **Columna canales**: 120dp fija
- **Granularidad**: 15min = 64dp
- **Now line**: Línea vertical roja, botón "Go to now"
- **Scroll**: Horizontal libre, snap a hora completa

#### **Interacciones**
- **Tap programa**: Sheet con detalles + "Ver canal"
- **Long press**: Recordatorio (si implementado)
- **Pinch zoom**: Cambiar granularidad (30min/15min/5min)

---

### **11. Búsqueda Global**

#### **Layout Overlay**
```
┌─────────────────────────────────┐
│ [🔍] Buscar contenido...    [X] │ ← Search input
├─────────────────────────────────┤
│                                 │
│ Términos recientes              │ ← Historial
│ ┌─────┬─────┬─────┬─────────┐   │
│ │fútbol│notic│pelíc│drama ×  │   │
│ └─────┴─────┴─────┴─────────┘   │
│                                 │
│ ┌─────┬─────┬─────┬─────────┐   │ ← Tabs resultados
│ │ TV  │Movie│Serie│ Todo    │   │
│ └─────┴─────┴─────┴─────────┘   │
├─────────────────────────────────┤
│                                 │
│ RESULTADOS TAB ACTIVO           │
│ ┌─────────────────────────────┐ │
│ │ [thumb] Resultado 1         │ │
│ │ [thumb] Resultado 2         │ │
│ │ [thumb] Resultado 3         │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### **Especificaciones Búsqueda**
- **Input**: Debounce 300ms, cancelar llamadas previas
- **Scope**: Global (TV + Movies + Series)
- **Filtros**: Por tipo de contenido
- **Recientes**: Máximo 8 términos, persistir en preferences

#### **Resultados por Tipo**
- **TV**: Lista con logo + nombre + EPG actual
- **Movies**: Lista con póster + metadatos
- **Series**: Lista con temporadas + progreso

---

### **12. Ajustes (Settings)**

#### **Layout por Secciones**
```
┌─────────────────────────────────┐
│ ← Ajustes                       │
├─────────────────────────────────┤
│                                 │
│ 👤 CUENTA                       │
│ Usuario actual: user@server     │
│ Cambiar usuario            →    │
│ Gestionar perfiles         →    │
│                                 │
├─────────────────────────────────┤
│ 🎨 APARIENCIA                   │
│ Tema                  Sistema ▼ │ ← Light/Dark/System
│ Tamaño de fuente     Normal ▼  │ ← Pequeño/Normal/Grande
│ Idioma              Español ▼  │
│                                 │
├─────────────────────────────────┤
│ ▶️ REPRODUCCIÓN                  │
│ Calidad preferida      Auto ▼  │
│ Idioma de audio    Español ▼   │ 
│ Subtítulos             Off ▼   │
│ Autoplay series          ☑     │ ← Switch
│ Mantener pantalla        ☑     │
│                                 │
├─────────────────────────────────┤
│ 📺 EPG                          │
│ Formato de hora        24h ▼   │
│ Zona horaria      Auto ▼       │
│ Mostrar progreso       ☑       │
│                                 │
├─────────────────────────────────┤
│ 👨‍👩‍👧‍👦 CONTROL PARENTAL            │
│ Estado            Inactivo      │
│ Configurar PIN            →     │
│ Categorías bloqueadas     →     │
│                                 │
├─────────────────────────────────┤
│ 🗄️ ALMACENAMIENTO               │
│ Caché de imágenes    125 MB     │
│ Limpiar caché             →     │
│ Datos offline         45 MB     │
│ Limpiar datos offline     →     │
│                                 │
├─────────────────────────────────┤
│ 🔒 PRIVACIDAD                   │
│ Telemetría               ☐      │ ← Opt-in explícito
│ Reportes de errores      ☑      │
│ Análisis de uso          ☐      │
│                                 │
├─────────────────────────────────┤
│ ℹ️ INFORMACIÓN                   │
│ Versión de la app    1.0.0      │
│ Última actualización  Hoy       │
│ Términos de uso           →     │
│ Política de privacidad    →     │
│ Acerca de                 →     │
└─────────────────────────────────┘
```

#### **Secciones Obligatorias**

**1. CUENTA**
- Usuario actual (server/username)
- Cambiar perfil activo
- Gestionar perfiles guardados

**2. APARIENCIA**
- Tema: Light/Dark/System (seguir sistema)
- Tamaño de fuente: Pequeño/Normal/Grande/Muy grande
- Idioma: Español (futuro: más idiomas)

**3. REPRODUCCIÓN**
- Calidad preferida: Auto/SD/HD/FHD/4K
- Idioma de audio: Lista de idiomas disponibles
- Subtítulos: Off/Auto/idiomas disponibles
- Autoplay series: Reproducir siguiente episodio
- Mantener pantalla: No apagar durante reproducción

**4. EPG**
- Formato hora: 12h/24h
- Zona horaria: Auto/manual
- Mostrar progreso: Barra en lista de canales

**5. CONTROL PARENTAL** (ETAPA 7)
- Estado: Activo/Inactivo
- Configurar PIN
- Categorías bloqueadas
- Palabras clave bloqueadas

**6. ALMACENAMIENTO**
- Tamaño caché actual
- Limpiar caché (imágenes, metadatos)
- Datos offline
- Limpiar todos los datos

**7. PRIVACIDAD** (ETAPA 7)
- Telemetría: Opt-in explícito
- Reportes de crash
- Análisis de uso

**8. INFORMACIÓN**
- Versión actual
- Fecha última actualización
- Enlaces legales
- Información del desarrollador

---

### **13. Gestión de Perfiles**

#### **Lista de Perfiles**
```
┌─────────────────────────────────┐
│ ← Perfiles                 [+]  │
├─────────────────────────────────┤
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 👤 Perfil Principal       ✓ │ │ ← Activo
│ │    user@server.com:8000     │ │
│ │    [Editar] [Eliminar]      │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 👤 Perfil Secundario        │ │
│ │    user2@otroserver.com     │ │
│ │    [Activar] [Editar] [❌]  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │        + AGREGAR PERFIL     │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### **Formulario Perfil**
```
┌─────────────────────────────────┐
│ ← Nuevo Perfil          [💾]    │
├─────────────────────────────────┤
│                                 │
│ Alias del perfil                │
│ ┌─────────────────────────────┐ │
│ │Mi Servidor IPTV             │ │
│ └─────────────────────────────┘ │
│                                 │
│ Servidor                        │
│ ┌─────────────────────────────┐ │
│ │http://servidor.com:8000     │ │
│ └─────────────────────────────┘ │
│                                 │
│ Usuario                         │
│ ┌─────────────────────────────┐ │
│ │mi_usuario                   │ │
│ └─────────────────────────────┘ │
│                                 │
│ Contraseña                 [👁] │
│ ┌─────────────────────────────┐ │
│ │••••••••••••••••••••••••••  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │      PROBAR CONEXIÓN        │ │ ← Test button
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │        GUARDAR PERFIL       │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

### **14. Estados de Error/Offline**

#### **Pantalla de Error**
```
┌─────────────────────────────────┐
│                                 │
│              ❌                 │ ← 96dp icon
│                                 │
│        Sin conexión             │ ← titleLarge
│                                 │
│    Verifica tu conexión a       │ ← bodyMedium
│    internet e intenta           │
│    nuevamente.                  │
│                                 │
│ ┌─────────────────────────────┐ │
│ │         REINTENTAR          │ │ ← Primary button
│ └─────────────────────────────┘ │
│                                 │
│        Configurar red           │ ← TextButton
│                                 │
└─────────────────────────────────┘
```

#### **Estados por Tipo**

**1. Sin Conexión**
```
Icon: 📶❌ (network off)
Title: "Sin conexión"
Message: "Verifica tu conexión Wi-Fi o datos móviles"
Actions: [Reintentar] [Configurar red]
```

**2. Error de Servidor**
```
Icon: 🔧 (server error)
Title: "Error del servidor"
Message: "El servidor no responde. Intenta más tarde."
Actions: [Reintentar] [Cambiar servidor]
```

**3. Contenido No Encontrado**
```
Icon: 🔍❌ (search off)
Title: "Contenido no disponible"
Message: "Este contenido ya no está disponible."
Actions: [Volver atrás] [Buscar similar]
```

**4. Lista Vacía**
```
Icon: 📺 (contextual)
Title: "No hay contenido"
Message: "No se encontró contenido para mostrar."
Actions: [Cambiar filtros] [Actualizar]
```

---

### **15. Pantalla PIN Parental**

#### **Layout Teclado Numérico**
```
┌─────────────────────────────────┐
│ ← Control Parental              │
├─────────────────────────────────┤
│                                 │
│        🔒 Contenido             │
│         Restringido             │
│                                 │
│    Ingresa tu PIN para          │
│    desbloquear este contenido   │
│                                 │
│    ┌───┬───┬───┬───┐           │ ← PIN input
│    │ ● │ ● │ ● │ ○ │           │   (dots)
│    └───┴───┴───┴───┘           │
│                                 │
│    ┌───┬───┬───┐               │ ← Numeric keypad
│    │ 1 │ 2 │ 3 │               │   48dp mínimo
│    ├───┼───┼───┤               │
│    │ 4 │ 5 │ 6 │               │
│    ├───┼───┼───┤               │
│    │ 7 │ 8 │ 9 │               │
│    ├───┼───┼───┤               │
│    │   │ 0 │⌫ │               │
│    └───┴───┴───┘               │
│                                 │
│    Intentos restantes: 3        │ ← Warning
│                                 │
│    ¿Olvidaste tu PIN?           │ ← Recovery link
└─────────────────────────────────┘
```

#### **Especificaciones PIN**
- **Input**: 4 dígitos, puntos como placeholder
- **Teclado**: 3×4 grid, botones 48dp mínimo
- **Feedback**: Vibración en error (si habilitado)
- **Intentos**: Mostrar restantes, lockout temporal
- **Recovery**: Flujo de pregunta secreta

---

## 📊 **Métricas de Performance por Pantalla**

### **Objetivos de Rendimiento**
```kotlin
// Time to Interactive (TTI)
Splash: < 800ms
Login: < 1.2s
Home: < 1.5s (with cache)
TV: < 2.0s (categories + channels)
Movies/Series: < 1.8s

// Time to First Frame (TTFF)
Live TV: < 2.5s (network dependent)
VOD: < 3.0s (network dependent)

// Scroll Performance
List scrolling: 60fps sustained
Grid scrolling: 60fps with 50+ items
```

### **Memory Usage**
```kotlin
// Memory targets
Idle state: < 80MB
With player: < 150MB
Heavy scrolling: < 200MB
Background: < 60MB
```

---

## 🧪 **Checklist de QA por Pantalla**

### **Universal (Todas las Pantallas)**
- [ ] Soporte tema claro/oscuro
- [ ] Escalado de fuentes 100%-200%
- [ ] TalkBack navigation completa
- [ ] Touch targets 48dp mínimo
- [ ] Estados de carga/error/vacío
- [ ] Rotación de pantalla correcta
- [ ] Back button behavior apropiado

### **Específico por Pantalla**
- [ ] **Login**: Validación en tiempo real, manejo de errores de red
- [ ] **Home**: Carruseles fluidos, lazy loading de imágenes
- [ ] **TV**: Single connection enforcement, EPG actualizado
- [ ] **Movies/Series**: Toggle vista persistente, filtros funcionando
- [ ] **Player**: Controles responsivos, overlay timing correcto
- [ ] **Search**: Debounce working, resultados actualizados
- [ ] **Settings**: Todas las opciones persistidas correctamente

---

## 📦 **Entregables de ETAPA 9**

1. ✅ **Esta especificación UI completa** (`UI_SPEC.md`)
2. 🔄 **Checklist de QA detallado** (`UI_QA_CHECKLIST.md`)
3. 🔄 **Tokens de diseño** (Código Kotlin)
4. 🔄 **Componentes base** (Sistema de diseño)
5. 🔄 **Navegación actualizada** (Nav graph)

---

**Esta especificación debe seguirse al pie de la letra en cualquier implementación de UI, manteniendo la compatibilidad con todas las ETAPAS anteriores (1-8) y asegurando una experiencia de usuario consistente y accesible.**