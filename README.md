# Lumina ✍️ — Note per Galaxy Tab S10 Ultra

[![Releases](https://img.shields.io/github/v/release/ad2300508-blip/The-NoteTaking?label=APK&sort=semver)](https://github.com/ad2300508-blip/The-NoteTaking/releases/latest)

App di note Android **nativa**, pensata e ottimizzata per il **Samsung Galaxy Tab S10 Ultra**:
schermo grande, alte frequenze di aggiornamento e **S Pen** come cittadini di prima classe.

> Costruita con Kotlin + Jetpack Compose + Material 3 (Material You).

## ✨ Caratteristiche

- **Inchiostro S Pen a pressione reale** — i tratti si assottigliano/ingrossano in base alla
  pressione della penna; campionamento ad alta frequenza con punti storici (`historical`)
  per linee fluide anche a 120 Hz.
- **Strumenti**: penna, evidenziatore (semitrasparente, fusione *multiply*) e **gomma per tratto**.
- **Tasto laterale S Pen = gomma** — quando il puntatore è di tipo `Eraser`, cancella al volo.
- **Rifiuto del palmo** — durante il disegno con la penna i tocchi delle dita vengono ignorati
  (attivabile dalle impostazioni).
- **Undo / Redo / Cancella tutto** sull'inchiostro.
- **Testo + disegno nella stessa nota** — passa tra modalità *Testo* e *Inchiostro*.
- **Layout adattivo a due pannelli** — su tablet la lista e l'editor sono affiancati
  (list–detail); su schermi stretti diventa a schermo intero con transizioni animate.
- **Material You** — colori dinamici dal sistema, tema chiaro/scuro/automatico.
- **Griglia sfalsata** (staggered) per le note, con anteprima, pin e preferiti.
- **Ricerca** istantanea su titolo e contenuto.
- **Autosave** con *debounce* — niente pulsante "salva", tutto persiste su Room.
- **Splash screen**, icona adattiva e monochrome.

## 🏗️ Architettura

```
com.lumina.notes
├── data
│   ├── local        # Room: NoteEntity, NoteDao, LuminaDatabase
│   ├── ink          # Stroke, StrokePoint, PenTool, InkSerializer (JSON compatto)
│   ├── repository   # NotesRepository
│   └── settings     # SettingsRepository (DataStore)
├── di               # AppContainer (service locator manuale, niente annotation processor)
└── ui
    ├── theme        # Material 3 + dynamic color + palette inchiostro/accenti
    ├── ink          # InkController (stato/undo/redo) + InkCanvas (input & rendering)
    ├── notes        # lista + ViewModel
    ├── editor       # editor (testo/inchiostro) + toolbar S Pen
    └── settings     # preferenze
```

- **MVVM** con `ViewModel` + `StateFlow`.
- **Persistenza**: Room (note) + DataStore (preferenze).
- **DI**: service locator leggero (`AppContainer`) — nessun KAPT, solo KSP per Room.

## 📦 Download dell'APK (Releases)

Ogni push pubblica automaticamente un APK installabile nella sezione
**[Releases](../../releases)** della repo (workflow `.github/workflows/release.yml`).
Sul Galaxy Tab: abilita "installa app sconosciute" per il browser/file manager,
scarica l'`.apk` dall'ultima release e aprilo. L'APK è *debug-signed* (per
sideloading personale, non per il Play Store).

📖 Guida passo-passo (download, `adb` via USB, S Pen, troubleshooting): **[INSTALL.md](INSTALL.md)**.

## 🚀 Build

Requisiti: **Android Studio** (Ladybug+) con Android SDK (compileSdk 35), JDK 17+.

```bash
# Apri il progetto in Android Studio, oppure da terminale:
./gradlew :app:assembleDebug      # APK debug
./gradlew :app:installDebug       # installa su un dispositivo collegato
./gradlew test                    # unit test JVM
```

`minSdk 29` (Android 10) · `targetSdk 35`.

## 🖊️ Note sulla S Pen

L'input penna è gestito tramite il sistema di puntatori di Compose
(`PointerType.Stylus` / `PointerType.Eraser`, `change.pressure`, `change.historical`),
quindi pressione e palm rejection funzionano senza interop con `MotionEvent`.
Hook futuri (low-latency front-buffer via `androidx.graphics`, predizione del moto)
possono essere innestati in `InkCanvas` senza cambiare il resto.

---

> ⚠️ **Nota di trasparenza**: il progetto è stato generato in un ambiente **senza Android SDK**,
> quindi *non* è stato compilato lì. La struttura, le dipendenze e il codice sono completi e
> coerenti; apri in Android Studio e lascia sincronizzare Gradle per la prima build.
