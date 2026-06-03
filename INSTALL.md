# Installare e testare Lumina sul Galaxy Tab S10 Ultra

Due modi: **A)** scarica l'APK già pronto (più semplice), **B)** via USB con `adb`.

---

## A) Installazione diretta dell'APK (consigliata)

1. Sul tablet apri la pagina delle release:
   **https://github.com/ad2300508-blip/The-NoteTaking/releases/latest**
2. Scarica il file `lumina-*.apk`.
3. Aprilo dal pannello notifiche / app File.
4. Android chiederà di abilitare **"Installa app sconosciute"** per il
   browser o il file manager → consenti → **Installa**.
5. Apri **Lumina**.

> L'APK è *debug-signed*: va benissimo per uso personale (sideload), non è
> pensato per la distribuzione sul Play Store.

---

## B) Installazione via USB con `adb`

Sul **tuo computer** (non serve l'Android SDK completo, bastano i
*Platform-Tools*):

1. Sul tablet: Impostazioni → Info sul tablet → tocca 7 volte "Numero build"
   per sbloccare le **Opzioni sviluppatore**, poi attiva **Debug USB**.
2. Collega il Tab via USB e sbloccalo; accetta il prompt "Consenti debug USB".
3. Sul computer:

   ```bash
   adb devices                       # deve elencare il tuo Tab
   # scarica lumina-x.y.z.apk dalla release, poi:
   adb install -r lumina-1.0.0.apk   # -r = reinstalla mantenendo i dati
   ```

Se `adb` non è installato: scarica *Android SDK Platform-Tools* da Google,
estrai e usa l'eseguibile `adb` da quella cartella.

### Build dal sorgente (opzionale)

Con Android Studio (Ladybug+) o da terminale con l'SDK configurato:

```bash
./gradlew :app:installDebug        # builda e installa sul Tab collegato
./gradlew test                     # unit test (JVM)
./gradlew connectedAndroidTest     # test strumentati sul Tab collegato
```

---

## Provare la S Pen

- **+** in basso a destra → nuova nota.
- Passa alla modalità **Inchiostro** (switch in alto).
- Disegna con la S Pen: il tratto segue la **pressione**.
- Prova **evidenziatore** e **gomma** (anche il **tasto laterale** della
  S Pen cancella al volo).
- **Due dita**: pinch per **zoom** e pan; tocca il badge `%` per tornare 1:1.
- **Rifiuto del palmo** è attivo di default (Impostazioni per cambiarlo):
  appoggia la mano mentre scrivi con la penna, i tocchi delle dita vengono
  ignorati.

### Gesti S Pen a mano libera

- **Doppio tocco** col pennino → alterna penna / gomma.
- **Scarabocchio** (avanti-indietro veloce) → cancella i tratti sotto.
- **Raddrizza forma** (pulsante ✦) → l'ultimo tratto diventa linea/rettangolo/cerchio.
- **Lazo** (strumento Seleziona) → cerchia tratti, poi trascinali o eliminali.

### Scrittura a mano → testo

Il pulsante **"Riconosci testo"** (icona testo) nella toolbar inchiostro
converte la scrittura della pagina in testo digitato e lo accoda al corpo
della nota (modalità Tastiera). Usa **ML Kit Digital Ink** on-device: la
prima volta scarica il modello (serve rete), poi funziona offline.

### Scorciatoie da tastiera (cover-tastiera del Tab)

- **Ctrl + N** — nuova nota
- **Esc** — torna alla lista

### Air Actions (pulsante S Pen)

Nell'editor, in modalità Scrittura, il **pulsante laterale della S Pen**:

- **Click singolo** → alterna penna ↔ gomma
- **Doppio click** → passa all'evidenziatore

Richiede il servizio *S Pen Remote* di Samsung (presente sui Galaxy con
S Pen). Su dispositivi senza, la funzione è semplicemente inattiva.

### Air Command / nota rapida

L'app registra l'azione **CREATE_NOTE**: dal menu Air Command della S Pen
(o da una scorciatoia "Crea nota") Lumina si apre direttamente su una nota
nuova.

---

## Disinstallare

```bash
adb uninstall com.lumina.notes
```

oppure tieni premuta l'icona → Disinstalla.
