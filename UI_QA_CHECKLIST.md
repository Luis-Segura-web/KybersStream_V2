# 🧪 **UI QA CHECKLIST - KybersStream V2**

## **Control de Calidad para ETAPA 9: Rediseño Integral**

### **📋 Estado General del Checklist**

- **Pantallas Totales**: 15
- **Componentes Base**: 12  
- **Estados**: 4 (Loading/Empty/Error/Offline)
- **Temas**: 2 (Light/Dark + Dynamic Color)
- **Tamaños de Fuente**: 4 (100%, 130%, 150%, 200%)

---

## 🌐 **Tests Universales (Aplican a Todas las Pantallas)**

### **Accesibilidad Obligatoria**
- [ ] **TalkBack Navigation**: Todos los elementos navegables con reader
- [ ] **Keyboard Navigation**: Tab order lógico en todas las pantallas
- [ ] **Touch Targets**: Mínimo 48×48dp en todos los botones/links
- [ ] **Content Descriptions**: Imágenes y botones tienen description clara
- [ ] **Semantic Roles**: Buttons, Links, Headers correctamente marcados
- [ ] **Focus Management**: Focus visible y estados clear/focused
- [ ] **High Contrast**: Contraste AA (4.5:1) en todos los textos
- [ ] **Motion Sensitivity**: Respeta preferencias de movimiento reducido

### **Responsive Design**
- [ ] **Phone Portrait**: 360×640dp mínimo, layouts correctos
- [ ] **Phone Landscape**: Elementos reflow apropiadamente
- [ ] **Tablet Portrait**: 768×1024dp, aprovecha espacio extra
- [ ] **Tablet Landscape**: NavigationRail + contenido optimizado
- [ ] **Font Scaling 100%**: Layout normal, todos los textos legibles
- [ ] **Font Scaling 130%**: Textos más grandes, no overlap
- [ ] **Font Scaling 150%**: Adaptive text working, scrolls aumentan
- [ ] **Font Scaling 200%**: Máximo soporte, layout degrada gracefully

### **Temas y Colores**
- [ ] **Light Theme**: Todos los elementos visibles y correctos
- [ ] **Dark Theme**: Colores apropiados, sin problemas de contraste
- [ ] **Dynamic Color** (Android 12+): Paleta se adapta al wallpaper
- [ ] **Static Fallback**: Funciona en devices sin Dynamic Color
- [ ] **Theme Switching**: Cambio inmediato sin restart
- [ ] **System Theme**: Sigue configuración del sistema automáticamente

### **Performance Universal**
- [ ] **60fps Scrolling**: Listas y grids mantienen framerate
- [ ] **Memory < 200MB**: Uso de memoria bajo control en uso normal
- [ ] **Cold Start < 3s**: App inicia completamente en menos de 3 segundos
- [ ] **Navigation < 500ms**: Transiciones entre pantallas fluidas
- [ ] **Image Loading**: Progressive loading, placeholders apropiados
- [ ] **Network Timeouts**: Requests cancelados apropiadamente

### **Estados Globales**
- [ ] **Loading State**: Skeletons específicos por tipo de contenido
- [ ] **Error State**: Mensajes claros + acción "Reintentar"
- [ ] **Empty State**: Ilustración + mensaje + CTA apropiado
- [ ] **Offline State**: Banner + modo degradado cuando aplique
- [ ] **Back Button**: Comportamiento correcto en toda la navegación
- [ ] **Deep Links**: URLs manejadas correctamente

---

## 📱 **Tests Específicos por Pantalla**

### **1. Splash Screen**
#### **Funcionalidad**
- [ ] **Logo Display**: Logo centrado, 120dp, adaptive icon
- [ ] **Auto Navigation**: Navega a Login o Home según sesión activa
- [ ] **Timing**: Máximo 2000ms o hasta resolver perfil
- [ ] **Offline Handling**: Solo muestra logo si no hay conexión
- [ ] **Migration State**: Badge "Actualizando..." si hay migración

#### **Visual**
- [ ] **Background**: Color `surface` correcto en ambos temas
- [ ] **Typography**: `titleLarge` para nombre de app
- [ ] **Centering**: Logo y texto perfectamente centrados
- [ ] **Animation**: Fade in suave del logo (opcional)

#### **Performance**
- [ ] **TTI < 800ms**: Time to Interactive bajo
- [ ] **Memory < 50MB**: Uso mínimo de memoria
- [ ] **Thread Blocking**: UI thread no bloqueado durante init

---

### **2. Login Screen**
#### **Funcionalidad**
- [ ] **Field Validation**: Servidor/Usuario/Contraseña validados en real-time
- [ ] **URL Validation**: Acepta http/https, rechaza formatos inválidos
- [ ] **Error Display**: Mensajes claros bajo cada campo con error
- [ ] **Remember User**: Checkbox funciona, persiste en próximo login
- [ ] **Profile Selection**: BottomSheet muestra perfiles guardados
- [ ] **Login Success**: Navega a Home al autenticar correctamente
- [ ] **Network Errors**: Maneja timeout, connection refused, 404, etc.

#### **Edge Cases**
- [ ] **Xtream API Errors**: 401, 403, límite de conexiones
- [ ] **Malformed URLs**: No crash con URLs inválidas
- [ ] **Empty Fields**: Previene submit con campos vacíos
- [ ] **Very Long URLs**: Trunca o scrollea horizontalmente
- [ ] **Special Characters**: Maneja correctamente en usuario/password

#### **UX**
- [ ] **Focus Flow**: Tab order: Servidor → Usuario → Contraseña → Button
- [ ] **Password Toggle**: Botón "ver/ocultar" funciona
- [ ] **Loading State**: Button muestra loading durante auth
- [ ] **Auto-fill**: Compatible con password managers

#### **Security**
- [ ] **Password Hiding**: Por defecto passwords ocultos
- [ ] **No Logging**: Credenciales no aparecen en logs
- [ ] **Secure Storage**: Passwords encriptados en DataStore

---

### **3. Home Screen (Dashboard)**
#### **Navigation**
- [ ] **Bottom Navigation**: 5 tabs: Home, TV, Movies, Series, Settings
- [ ] **Tab State**: Tab activo claramente marcado
- [ ] **Badge Support**: Badges en tabs si hay notificaciones
- [ ] **Swipe Navigation**: Swipe entre tabs habilitado (opcional)

#### **Hero Carousel**
- [ ] **16:9 Ratio**: Aspect ratio correcto en todos los screen sizes
- [ ] **Auto-play**: Configurable en Settings, off por defecto
- [ ] **Indicators**: Dots showing current/total items
- [ ] **Manual Swipe**: Usuario puede swipe manualmente
- [ ] **Content Loading**: Placeholders mientras carga contenido

#### **Horizontal Carousels**
- [ ] **Smooth Scrolling**: 60fps en scroll horizontal
- [ ] **Edge Padding**: 16dp padding lateral consistente
- [ ] **Item Spacing**: 12dp entre items
- [ ] **Poster Ratio**: 2:3 ratio (160×240dp) en pósters
- [ ] **Loading State**: Skeleton items mientras carga

#### **Content Sections**
- [ ] **Continuar Viendo**: Solo visible si hay progreso guardado
- [ ] **Favoritos**: Solo visible si hay items marcados como favoritos
- [ ] **Dynamic Sections**: Secciones aparecen/desaparecen según contenido
- [ ] **Empty Sections**: No muestran secciones vacías
- [ ] **Tap Actions**: Tap en item navega a detalle correcto

#### **Search Integration**
- [ ] **Search Icon**: TopAppBar tiene icono search funcionando
- [ ] **Global Search**: Busca en TV + Movies + Series
- [ ] **Search Overlay**: Overlay completo con input y resultados

---

### **4. TV Screen**
#### **Player Integration**
- [ ] **16:9 Player**: Zona de player con ratio correcto
- [ ] **Single Connection**: Solo un stream activo a la vez
- [ ] **Auto Play**: Primer canal se reproduce automáticamente
- [ ] **Player Controls**: Tap muestra/oculta controles (3s timeout)
- [ ] **Fullscreen**: Player expandible a pantalla completa
- [ ] **Error Handling**: Maneja streams no disponibles gracefully

#### **Channel List**
- [ ] **Item Height**: 72-80dp altura por canal
- [ ] **Logo Display**: 56dp logos de canal con fallback a initials
- [ ] **EPG Information**: Now/Next programs con horarios
- [ ] **Progress Bar**: Barra de progreso del programa actual (opcional)
- [ ] **Favorite Toggle**: Botón heart funciona inmediatamente
- [ ] **Tap Action**: Tap reproduce canal en player
- [ ] **Long Press**: Long press muestra EPG del día (opcional)

#### **Filtering**
- [ ] **Search Input**: Debounce 300ms, cancela requests anteriores
- [ ] **Category Chips**: Scroll horizontal, selección múltiple
- [ ] **Filter Persistence**: Filtros aplicados persisten en navegación
- [ ] **Clear Filters**: Manera de limpiar todos los filtros

#### **Performance**
- [ ] **Large Lists**: 1000+ canales sin jank en scroll
- [ ] **Image Loading**: Logos cargan progressively con placeholders
- [ ] **Memory Management**: No leaks con muchos canales
- [ ] **Smooth Playback**: Cambio de canal < 3s en red normal

---

### **5. Movies Screen**
#### **View Toggle**
- [ ] **List/Grid Toggle**: Botones en TopAppBar funcionan
- [ ] **View Persistence**: Recuerda última vista seleccionada
- [ ] **Smooth Transition**: Transición entre vistas sin jank
- [ ] **Grid Columns**: 2-3 columnas adaptables a screen width

#### **Grid View**
- [ ] **Poster Ratio**: 2:3 ratio (160×240dp) consistente
- [ ] **Metadata Display**: Título (2 líneas), año, duración, calidad
- [ ] **Favorite Indicator**: Heart en esquina superior derecha
- [ ] **Tap Action**: Tap abre ficha de película
- [ ] **Long Press**: Long press action (reproducir inmediato)

#### **List View**
- [ ] **Item Height**: 72dp altura consistente
- [ ] **Poster Size**: 80dp wide póster
- [ ] **Triple Line**: Título, metadatos, géneros en 3 líneas
- [ ] **Favorite Toggle**: Heart en lado derecho

#### **Filtering**
- [ ] **Search Bar**: Busca por título con debounce
- [ ] **Category Filters**: Chips con categorías de API
- [ ] **Quality Filter**: SD/HD/FHD/4K/All
- [ ] **Year Filter**: Range picker o dropdown
- [ ] **Genre Filter**: Extraído de metadata cuando disponible
- [ ] **Sort Options**: A-Z, año, fecha agregado

#### **Performance**
- [ ] **Lazy Loading**: Carga items según scroll position
- [ ] **Image Cache**: Pósters cached apropiadamente
- [ ] **Smooth Scroll**: 60fps con 500+ películas
- [ ] **Memory Efficient**: Recicla views fuera de viewport

---

### **6. Series Screen**
#### **Funcionalidad Similar a Movies**
- [ ] **View Toggle**: List/Grid views disponibles
- [ ] **Series Metadata**: Temporadas/Episodios en lugar de duración
- [ ] **Season Progress**: Indica progreso por temporada
- [ ] **Continue Watching**: Muestra último episodio visto

#### **Específico de Series**
- [ ] **Episode Count**: "3T•24E" formato para temporadas/episodios
- [ ] **Genre Display**: Géneros principales mostrados
- [ ] **Watch Status**: Indica series completadas/en progreso
- [ ] **Tap Action**: Abre ficha de serie con temporadas

---

### **7. Ficha VOD (Película)**
#### **Header Section**
- [ ] **Poster Display**: 200×300px póster a la izquierda
- [ ] **Title Display**: Título principal grande y claro
- [ ] **Rating**: Estrellas si disponible en metadata
- [ ] **Metadata Line**: Año • Duración • Calidad en una línea
- [ ] **Genres**: Máximo 3 géneros, separados por comas
- [ ] **Favorite Toggle**: Heart button funciona inmediatamente

#### **Action Buttons**
- [ ] **Primary Play**: "REPRODUCIR" prominente, funciona inmediatamente
- [ ] **Continue Button**: Muestra "CONTINUAR" si hay progreso guardado
- [ ] **Secondary Actions**: Favorito, compartir (opcional)
- [ ] **Button States**: Loading states durante acciones

#### **Content Sections**
- [ ] **Synopsis**: Expandible si es muy largo (3 líneas + "Ver más")
- [ ] **Technical Details**: Director, reparto, duración, calidad
- [ ] **Related Content**: Carrusel horizontal de contenido relacionado
- [ ] **Scrollable Layout**: Todo el contenido scrolleable verticalmente

#### **Navigation**
- [ ] **Back Button**: Regresa a lista de películas
- [ ] **Share Action**: Share sheet nativo con URL
- [ ] **Deep Link**: URL directa funciona correctamente

---

### **8. Ficha Serie**
#### **Season Management**
- [ ] **Season Selector**: Chips o dropdown para seleccionar temporada
- [ ] **Season Persistence**: Recuerda última temporada vista
- [ ] **All Seasons**: Opción para ver todos los episodios juntos
- [ ] **Season Metadata**: Info específica por temporada

#### **Episode List**
- [ ] **Episode Layout**: Thumbnail 16:9 + título + metadata
- [ ] **Episode Naming**: "SxEy - Título" formato consistente
- [ ] **Progress Indicators**: Barra de progreso por episodio visto parcialmente
- [ ] **Watch Status**: Checkmark para episodios completados
- [ ] **Play Actions**: Tap reproduce, botón play visible

#### **Autoplay**
- [ ] **Next Episode**: Configurable en Settings
- [ ] **Season Boundary**: Pregunta antes de cambiar temporada
- [ ] **Autoplay Timer**: Countdown 15s antes de autoplay
- [ ] **Cancel Autoplay**: Usuario puede cancelar countdown

#### **Series Features**
- [ ] **Binge Mode**: Reproductor optimizado para múltiples episodios
- [ ] **Episode Skip**: Fácil navegación entre episodios
- [ ] **Series Progress**: Progreso general de toda la serie

---

### **9. Reproductor Fullscreen**
#### **Overlay Management**
- [ ] **Auto Hide**: Overlays se ocultan después de 3s sin interaction
- [ ] **Tap to Show**: Tap en cualquier lugar muestra overlays
- [ ] **Touch Zones**: Areas definidas para diferentes acciones
- [ ] **Overlay Transparency**: Overlays no bloquean completamente el video

#### **Top Overlay**
- [ ] **Back Button**: Regresa a pantalla anterior, para reproductor
- [ ] **Title Display**: Canal/contenido actual visible
- [ ] **Favorite Toggle**: Heart button funciona en reproductor
- [ ] **Menu Button**: Acceso a opciones (calidad, audio, subtítulos)

#### **Center Controls**
- [ ] **Play/Pause**: Tap central para play/pause
- [ ] **Double Tap Skip**: ±10s para VOD/Series (no para Live)
- [ ] **Loading Indicator**: Spinner durante buffering
- [ ] **Gesture Areas**: Zonas táctiles bien definidas

#### **Bottom Overlay**
- [ ] **Progress Bar**: Para VOD/Series, buffer bar para Live
- [ ] **Time Display**: Current/total time para VOD
- [ ] **Control Buttons**: Específicos por tipo de contenido
- [ ] **Cast Button**: Google Cast integration si disponible

#### **Content Type Specific**
**Live TV:**
- [ ] **Channel Info**: Nombre de canal visible
- [ ] **Buffer Indicator**: Muestra buffer level
- [ ] **Channel List**: Acceso rápido a lista de canales
- [ ] **No Seek**: No permite seek en streams live

**VOD/Series:**
- [ ] **Seek Bar**: Permite seek a cualquier posición
- [ ] **Chapter Markers**: Si disponible en metadata
- [ ] **Previous/Next**: Navegación entre episodios
- [ ] **Speed Control**: Velocidad de reproducción (opcional)

#### **Error States**
- [ ] **Network Error**: Mensaje claro + botón reintentar
- [ ] **Format Error**: "Formato no soportado" + alternativas
- [ ] **Connection Limit**: Mensaje específico de límite alcanzado
- [ ] **Stream Offline**: "Canal no disponible" + sugerencias

#### **Performance**
- [ ] **Smooth Playback**: Sin stuttering en redes normales
- [ ] **Quick Start**: TTFF < 3s en condiciones normales
- [ ] **Buffer Management**: Buffer apropiado para calidad de red
- [ ] **Memory Usage**: No leaks durante playback prolongado

---

### **10. EPG Timeline (Avanzado)**
#### **Layout Management**
- [ ] **Channel Column**: 120dp width fija, scroll independiente
- [ ] **Time Grid**: 15min = 64dp, scroll horizontal suave
- [ ] **Now Line**: Línea roja vertical en tiempo actual
- [ ] **Grid Alignment**: Programs alineados correctamente con time slots

#### **Navigation**
- [ ] **Smooth Scroll**: Scroll horizontal fluido sin jank
- [ ] **Snap to Hour**: Snap automático a horas completas
- [ ] **Go to Now**: Botón que lleva a tiempo actual
- [ ] **Time Navigation**: Fast scroll a diferentes horarios

#### **Program Display**
- [ ] **Program Blocks**: Anchos proporcionales a duración
- [ ] **Text Fitting**: Títulos se ajustan al espacio disponible
- [ ] **Current Program**: Highlight visual del programa actual
- [ ] **Program Overlap**: Manejo correcto de solapamientos

#### **Interactions**
- [ ] **Tap Program**: Sheet con detalles + acción "Ver canal"
- [ ] **Long Press**: Recordatorio o información adicional
- [ ] **Zoom**: Pinch para cambiar granularidad (30/15/5 min)
- [ ] **Channel Tap**: Reproduce canal inmediatamente

---

### **11. Búsqueda Global**
#### **Search Input**
- [ ] **Debounce**: 300ms delay antes de search
- [ ] **Cancel Previous**: Cancela requests anteriores correctamente
- [ ] **Loading State**: Indica cuando está buscando
- [ ] **Empty Input**: Muestra términos recientes y sugerencias

#### **Results Organization**
- [ ] **Tabbed Results**: TV, Movies, Series, All tabs
- [ ] **Result Count**: Muestra número de resultados por tab
- [ ] **Mixed Results**: Tab "All" mezcla todos los tipos
- [ ] **No Results**: Estado vacío con sugerencias

#### **Search History**
- [ ] **Recent Terms**: Máximo 8 términos recientes
- [ ] **Term Deletion**: Swipe o X para eliminar términos
- [ ] **Persistence**: Términos persisten entre sesiones
- [ ] **Privacy**: Términos se limpian en logout

#### **Performance**
- [ ] **Fast Results**: Resultados aparecen < 1s
- [ ] **Incremental Load**: Carga más resultados on scroll
- [ ] **Cancel on Exit**: Cancela search al salir de pantalla
- [ ] **Memory Efficient**: No acumula results indefinidamente

---

### **12. Ajustes (Settings)**
#### **Section Organization**
- [ ] **Logical Grouping**: Settings agrupados por categoría
- [ ] **Clear Labels**: Etiquetas descriptivas y claras
- [ ] **Consistent Spacing**: 16dp padding consistente
- [ ] **Section Dividers**: Separadores claros entre secciones

#### **Account Section**
- [ ] **Current User**: Muestra usuario/servidor actual
- [ ] **Switch User**: Cambia a otro perfil guardado
- [ ] **Manage Profiles**: Navega a gestión de perfiles
- [ ] **Logout**: Opción clara para cerrar sesión

#### **Appearance Section**
- [ ] **Theme Selection**: Light/Dark/System options
- [ ] **Theme Apply**: Cambio inmediato sin restart
- [ ] **Font Size**: 4 opciones (Pequeño/Normal/Grande/Muy grande)
- [ ] **Language**: Español por defecto, más idiomas futuro

#### **Playback Section**
- [ ] **Quality Preference**: Auto/SD/HD/FHD/4K
- [ ] **Audio Language**: Lista de idiomas disponibles
- [ ] **Subtitle Settings**: Off/Auto/idiomas
- [ ] **Autoplay Series**: Toggle para next episode
- [ ] **Keep Screen On**: Toggle para wakeLock durante playback

#### **EPG Section**
- [ ] **Time Format**: 12h/24h selection
- [ ] **Timezone**: Auto/manual timezone
- [ ] **Progress Bar**: Toggle para mostrar en channel list
- [ ] **EPG Update**: Frecuencia de actualización

#### **Parental Controls**
- [ ] **PIN Status**: Estado actual (Activo/Inactivo)
- [ ] **PIN Setup**: Flujo para configurar PIN
- [ ] **Blocked Categories**: Lista de categorías bloqueadas
- [ ] **Blocked Keywords**: Lista de palabras clave
- [ ] **Recovery**: Configurar pregunta de recuperación

#### **Storage Section**
- [ ] **Cache Size**: Tamaño actual de caché
- [ ] **Clear Cache**: Botón para limpiar caché de imágenes
- [ ] **Offline Data**: Tamaño de datos offline
- [ ] **Clear All**: Opción para limpiar todos los datos

#### **Privacy Section**
- [ ] **Telemetry Toggle**: Opt-in explícito para telemetría
- [ ] **Crash Reports**: Toggle para reportes de crash
- [ ] **Usage Analytics**: Toggle para análisis de uso
- [ ] **Data Export**: Opción para exportar datos del usuario

#### **Information Section**
- [ ] **App Version**: Versión actual visible
- [ ] **Last Update**: Fecha de última actualización
- [ ] **Legal Links**: Términos de uso, política de privacidad
- [ ] **About**: Información del desarrollador

#### **Setting Persistence**
- [ ] **Immediate Save**: Cambios se guardan inmediatamente
- [ ] **Restore on Launch**: Settings persisten entre sesiones
- [ ] **Default Values**: Valores por defecto sensatos
- [ ] **Validation**: Settings inválidos se revierten a defaults

---

### **13. Gestión de Perfiles**
#### **Profile List**
- [ ] **Current Profile**: Perfil activo claramente marcado
- [ ] **Profile Info**: Servidor y usuario visible
- [ ] **Action Buttons**: Editar/Eliminar/Activar por perfil
- [ ] **Add Button**: Botón para agregar nuevo perfil

#### **Profile Form**
- [ ] **Field Validation**: Validación en tiempo real
- [ ] **Test Connection**: Botón para probar conectividad
- [ ] **Password Toggle**: Ver/ocultar contraseña
- [ ] **Save State**: Desabilita save hasta que form sea válido

#### **Profile Actions**
- [ ] **Activate Profile**: Cambia perfil activo inmediatamente
- [ ] **Edit Profile**: Pre-carga datos existentes
- [ ] **Delete Profile**: Confirmación antes de eliminar
- [ ] **Duplicate Protection**: Previene perfiles duplicados

#### **Security**
- [ ] **Password Encryption**: Passwords encriptados en storage
- [ ] **Secure Display**: Passwords ocultos por defecto
- [ ] **No Logging**: Credenciales no aparecen en logs
- [ ] **Auto-logout**: Logout automático en inactividad (opcional)

---

### **14. Estados de Error/Offline**
#### **Error Display**
- [ ] **Clear Icons**: Iconos representativos del tipo de error
- [ ] **Descriptive Text**: Mensajes claros y accionables
- [ ] **Retry Actions**: Botones que realmente solucionan el problema
- [ ] **Alternative Actions**: Opciones secundarias útiles

#### **Network Errors**
- [ ] **No Connection**: Detecta correctamente falta de internet
- [ ] **Server Down**: Distingue entre no internet y servidor down
- [ ] **Timeout**: Maneja timeouts con opción de reintentar
- [ ] **DNS Errors**: Mensajes específicos para problemas DNS

#### **Content Errors**
- [ ] **404 Not Found**: Contenido no encontrado
- [ ] **401/403**: Errores de autenticación/autorización
- [ ] **Format Errors**: Formato de stream no soportado
- [ ] **Geo-blocking**: Contenido no disponible en región

#### **Empty States**
- [ ] **No Favorites**: Estado cuando no hay favoritos
- [ ] **No Search Results**: Estado cuando búsqueda no encuentra nada
- [ ] **No Content**: Estado cuando categoría está vacía
- [ ] **No History**: Estado cuando no hay historial

---

### **15. Pantalla PIN Parental**
#### **PIN Input**
- [ ] **4 Digit PIN**: Acepta exactamente 4 dígitos
- [ ] **Visual Feedback**: Dots se llenan según input
- [ ] **Haptic Feedback**: Vibración en error (si habilitado)
- [ ] **Auto Submit**: Submit automático al completar 4 dígitos

#### **Numeric Keypad**
- [ ] **Large Buttons**: 48dp mínimo para accesibilidad
- [ ] **Clear Layout**: 3×4 grid bien espaciado
- [ ] **Backspace**: Botón para borrar último dígito
- [ ] **Audio Feedback**: Sonidos de teclas (respeta settings)

#### **Security Features**
- [ ] **Attempt Limiting**: Máximo intentos antes de lockout
- [ ] **Lockout Timer**: Tiempo de espera después de max intentos
- [ ] **Recovery Option**: "¿Olvidaste tu PIN?" disponible
- [ ] **Session Timeout**: PIN session expira después de tiempo configurado

#### **Recovery Flow**
- [ ] **Security Question**: Pregunta configurada en setup
- [ ] **Answer Validation**: Valida respuesta correcta
- [ ] **Temporary PIN**: Genera PIN temporal tras recovery exitoso
- [ ] **Force Reset**: Obliga a configurar nuevo PIN permanente

---

## 🚀 **Tests de Performance y Estrés**

### **Memory Management**
- [ ] **Memory Leaks**: No leaks detectables con profiler
- [ ] **Background Memory**: < 60MB cuando app está en background
- [ ] **Peak Memory**: < 200MB en uso intensivo
- [ ] **GC Pressure**: Minimal garbage collection durante uso normal

### **Network Resilience**
- [ ] **Slow Networks**: Funciona en 2G/3G con timeouts apropiados
- [ ] **Network Switching**: Maneja cambio WiFi ↔ Cellular gracefully
- [ ] **Airplane Mode**: Maneja pérdida total de conectividad
- [ ] **Intermittent Connection**: Reintentos automáticos funcionan

### **Large Data Sets**
- [ ] **1000+ Channels**: Lista de canales TV no se degrada
- [ ] **500+ Movies**: Grid de películas mantiene performance
- [ ] **Complex EPG**: Timeline EPG con data de 7 días
- [ ] **Search Results**: 100+ resultados sin jank

### **Extended Use**
- [ ] **24h Playback**: Player funciona en sesiones largas
- [ ] **Background Play**: Audio continúa en background correctamente
- [ ] **Multiple Sessions**: Cambio entre contenidos sin acumulación
- [ ] **App Restoration**: Restaura estado después de kill del sistema

---

## 📊 **Métricas de Éxito**

### **Performance Targets**
```
✅ TTI (Time to Interactive)
- Splash: < 800ms
- Home: < 1.5s
- TV: < 2.0s
- Player: < 3.0s

✅ Memory Usage
- Idle: < 80MB
- Playing: < 150MB
- Peak: < 200MB

✅ Crash Rate
- < 0.5% (1 crash por 200 sesiones)

✅ User Ratings
- > 4.0 stars average
- < 5% 1-2 star reviews
```

### **Accessibility Compliance**
```
✅ WCAG 2.1 AA
- Contrast ratios > 4.5:1
- Touch targets > 48dp
- TalkBack navigation complete

✅ Font Scaling
- 100%-200% support
- No layout breaks
- All text readable

✅ Navigation
- Keyboard accessible
- Focus management correct
- Screen reader friendly
```

### **User Experience**
```
✅ Task Completion
- Login success rate > 95%
- Content discovery < 30s
- Playback start < 5s

✅ Error Recovery
- Clear error messages
- Actionable recovery steps
- Auto-retry where appropriate
```

---

## 📝 **Proceso de QA**

### **Testing Phases**
1. **Development Testing**: Cada componente durante desarrollo
2. **Integration Testing**: Pantallas completas con navegación
3. **Regression Testing**: Después de cada cambio significativo
4. **Performance Testing**: Con datasets reales grandes
5. **Accessibility Testing**: Con lectores de pantalla reales
6. **Device Testing**: En múltiples devices y versions Android

### **Testing Tools**
- **Layout Inspector**: Para validar layouts y constraints
- **Accessibility Scanner**: Para compliance automático
- **Memory Profiler**: Para detectar leaks y uso excesivo
- **Network Profiler**: Para validar requests y responses
- **TalkBack**: Para testing manual de accessibility
- **Large Font Settings**: Para testing de font scaling

### **Sign-off Criteria**
- [ ] **Todos los tests passing**: 100% de tests en green
- [ ] **Performance dentro de targets**: Métricas cumplidas
- [ ] **Zero critical bugs**: No bugs que impidan uso normal
- [ ] **Accessibility compliant**: WCAG 2.1 AA compliance
- [ ] **Design approval**: Sign-off del design team
- [ ] **Product approval**: Sign-off del product owner

---

**Este checklist debe completarse al 100% antes de considerar la ETAPA 9 como terminada. Cada item representa un aspecto crítico de la experiencia de usuario y la calidad del producto.**