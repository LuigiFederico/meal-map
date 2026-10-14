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

Hai due strade:
- aprire il progetto in Android Studio ed eseguirlo;
- installare l'APK di debug con `adb install -r app/build/outputs/apk/debug/app-debug.apk`, che richiede il debug USB attivo.

Al primo avvio la settimana è vuota: si genera da **Nuova settimana**.

## Dati iniziali

Le alternative con le porzioni vengono caricate da `app/src/main/assets/seed/foods.json`. Il file si rigenera dalla tabella della nutrizionista:

```sh
python3 scripts/build_seed_foods.py
```

Il seed viene letto solo quando il database viene creato. Le modifiche fatte nell'app restano sul telefono.
