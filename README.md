# Renuevo Radio 102.4 FM 📻✨

Aplicación oficial para Android de **Renuevo Radio (102.4 FM)**: *"Viviendo en su Presencia"*. Transmisión de audio en vivo de alta fidelidad, diseño moderno y minimalista inspirado en interfaces de radio de alto impacto, carátulas en tiempo real, versículo bíblico diario en versión Reina-Valera 1960 (RVR1960) y reproducción en segundo plano.

---

## 🚀 Características Principales

### 🎵 1. Transmisión en Vivo de Alta Fidelidad
- **Motor Media3 ExoPlayer (1.5.0):** Streaming continuo, baja latencia, reconexión inteligente ante pérdidas temporales de red y gestión de audio silencioso (*Audio Becoming Noisy*).
- **Metadatos ICY en Vivo:** Detección automática en tiempo real de títulos de canciones y artistas que se emiten en antena.
- **Búsqueda Automática de Carátulas:** Integración con iTunes Search API para descargar y mostrar la portada de alta resolución de cada tema en reproducción y en el historial.

### 🎨 2. Interfaz Minimalista y Visualmente Impactante
- **Aura Luminosa Radial (*Yellow Glow Halo*):** Efecto de resplandor animado detrás de la carátula que respira según el estado de reproducción.
- **Botón Central Icónico:** Botón principal de reproducción en amarillo eléctrico vibrante (`#D8FA25`) con ícono de alto contraste en negro azabache.
- **Fondo Obsidiana de Estudio (`#0D0D0F`):** Estética refinada que resalta los elementos visuales sin saturar la vista.
- **Volumen del Sistema:** Interfaz limpia sin controles deslizantes innecesarios en pantalla, delegando el ajuste de volumen a las teclas físicas del dispositivo.

### 📱 3. Mini Reproductor Persistente (`BottomMiniPlayer`)
- Al navegar fuera de la pantalla de reproducción (hacia **Playlist**, **Versículo** o **Connect**), aparece de forma fluida un **Mini Reproductor** anclado sobre la barra de navegación.
- Cuenta con:
  - Miniatura cuadrada de la carátula o logotipo con bordes redondeados.
  - Título y artista en reproducción.
  - Botón circular de reproducción/pausa directa.
  - Al presionar en cualquier parte del mini reproductor se expande inmediatamente a la vista completa (*Listen*).

### 📊 4. Visualizador de Audio Dinámico (`Canvas`)
- Animación de ondas reactiva implementada con Jetpack Compose `Canvas`.
- Responde directamente a los estados de ExoPlayer:
  - **`PLAYING`**: Síntesis armónica multi-frecuencia (graves, medios y agudos) con envolvente acústica de campana y destellos en picos.
  - **`BUFFERING`**: Onda progresiva de barrido rítmico que comunica visualmente la carga de buffer.
  - **`PAUSED`**: Transición suave de amortiguamiento (*damping*) hacia un estado de reposo.
  - **`IDLE` / `ERROR`**: Modo de espera con baja amplitud y respuesta sutil.

### 📖 5. Versículo Bíblico del Día (RVR1960)
- Consulta directa a la API bíblica (traducción tradicional Reina-Valera 1960).
- Modo aleatorio (*Explorar más versículos*) y versículos de respaldo curados sin conexión.
- Integración para compartir de forma directa en WhatsApp, X (Twitter), correo y portapapeles.

### ⏳ 6. Temporizador de Apagado (*Sleep Timer*)
- Presets rápidos de 15, 30 y 60 minutos con opción de incremento dinámico (`+15 min`).
- Desvanecimiento progresivo del volumen (*fade out*) antes de pausar la transmisión.
- Cuenta regresiva digital visible tanto en el reproductor como en los ajustes.

### 🎶 7. Historial de Canciones y Favoritos
- Base de datos local con **Room** para registrar las pistas emitidas recientemente.
- Búsqueda en tiempo real por título o artista.
- Marcado de canciones favoritas con filtro dedicado.
- Opciones para compartir el tema o buscar la canción en YouTube / Google.

### 🔔 8. Reproducción en Segundo Plano y Notificación Multimedia
- Servicio Foreground (`RadioPlaybackService`) compatible con Android 14/15.
- Control multimedia integrado con `MediaSessionCompat` y `NotificationCompat.MediaStyle`.
- Controles de Play/Pause y Stop desde la pantalla de bloqueo, barra de notificaciones y dispositivos Bluetooth / Android Auto.

---

## 🛠️ Arquitectura y Tecnologías

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje** | Kotlin 100% |
| **UI Framework** | Jetpack Compose con Material Design 3 |
| **Arquitectura** | MVVM (Model-View-ViewModel) + Repositorio |
| **Motor de Audio** | AndroidX Media3 ExoPlayer 1.5.0 |
| **Imágenes y Carátulas** | Coil Compose (con soporte para caché LruCache) |
| **Persistencia Local** | Room Database + DataStore Preferences |
| **Red** | OkHttp 4 + Coroutines Flow |
| **Pruebas** | Robolectric para JVM Tests |

---

## 📂 Estructura del Proyecto

```text
app/src/main/java/com/example/
├── MainActivity.kt               # Actividad principal con Edge-to-Edge y tema
├── RadioApp.kt                   # Inicialización de servicios y repositorio
├── data/
│   ├── local/                    # Entidades Room y SongDao
│   ├── preferences/              # UserPreferences con DataStore
│   ├── remote/                   # ArtworkService (iTunes) y BibleVerseService (RVR1960)
│   └── repository/               # RadioRepository unificando estado y datos
├── model/                        # Modelos de datos (SongTrack, StreamInfo, BibleVerse, PlaybackStatus)
├── player/                       # RadioPlayerManager con Media3 ExoPlayer
├── service/                      # RadioPlaybackService con notificación MediaStyle
└── ui/
    ├── MainViewModel.kt          # Gestión de estado UI y comandos del reproductor
    ├── RadioHomeScreen.kt        # Pantalla principal, TopBar, BottomBar y contenedor
    ├── components/
    │   ├── AudioVisualizer.kt    # Visualizador de audio reactivo con Canvas
    │   ├── BottomMiniPlayer.kt   # Mini reproductor persistente entre pestañas
    │   ├── DailyVerseScreen.kt   # Pantalla y tarjeta del versículo RVR1960
    │   ├── SongHistoryList.kt    # Lista de historial con búsqueda y favoritos
    │   └── SleepTimerDialog.kt   # Diálogo de configuración del temporizador
    └── theme/                    # Color.kt, Theme.kt y Type.kt oficiales
```

---

## 📲 Emisión Oficial
- **Estación:** Renuevo Radio
- **Frecuencia:** 102.4 FM
- **Streaming:** AAC Stereo 96 kbps
- **Lema:** *"Viviendo en su Presencia"*
