# meal-map

App Android personale che disegna la mappa della settimana dei pasti per categorie: a pranzo e a cena, una fonte di proteine, una di carboidrati e le verdure. Non prescrive alimenti precisi. Le regole vengono dal piano della nutrizionista.

- **Requisiti, scelte e architettura:** [`docs/sviluppo.md`](docs/sviluppo.md)
- **Design delle schermate:** [`docs/design/`](docs/design/)
- **Piano alimentare di partenza:** [`docs/piano-alimentare/`](docs/piano-alimentare/)

## Compilare

Serve:
- JDK 21;
- Android SDK con la piattaforma 37;
- un file `local.properties` con `sdk.dir=/percorso/Android/Sdk`. Android Studio lo crea da solo.

```sh
./gradlew assembleDebug                                    # APK in app/build/outputs/apk/debug/
./gradlew ktlintCheck lintDebug testDebugUnitTest          # stile, lint e test (JVM + Robolectric)
./gradlew ktlintFormat                                     # corregge lo stile
```

I test non richiedono un emulatore: le schermate vengono provate con Robolectric.

## Installare sul telefono

Ogni push su `main` esegue i controlli e pubblica una release su GitHub con l'APK firmato (`.github/workflows/release.yml`).
- **Prima installazione:** apri con Chrome la [pagina delle release](https://github.com/LuigiFederico/meal-map/releases/latest), scarica l'APK e aprilo. Android chiede di permettere a Chrome di installare app.
- **Aggiornamenti:** in **Impostazioni** tocca **Controlla aggiornamenti**. Se c'è una versione nuova, l'app la scarica e apre l'installer. L'aggiornamento si installa sopra la versione vecchia e conserva i dati.

Per provare una build locale hai due strade:
- aprire il progetto in Android Studio ed eseguirlo;
- installare l'APK di debug con `adb install -r app/build/outputs/apk/debug/app-debug.apk`, che richiede il debug USB attivo.

L'APK di debug è firmato con un'altra chiave: per passare da una build all'altra bisogna disinstallare l'app, e si perdono i dati.

La chiave di release (`meal-map-release.p12`) e la sua password sono nei secret del repo (`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`). Senza la chiave non si possono pubblicare aggiornamenti per l'app installata: tienine una copia.

Al primo avvio la settimana è vuota: si genera da **Nuova settimana**.

## Dati iniziali

Le alternative con le porzioni vengono caricate da `app/src/main/assets/seed/foods.json`. Il file si rigenera dalla tabella della nutrizionista:

```sh
python3 scripts/build_seed_foods.py
```

Il seed viene letto solo quando il database viene creato. Le modifiche fatte nell'app restano sul telefono.
