# Piano di sviluppo

Meal Map è un'app Android personale che disegna la mappa della settimana dei pasti per **categorie**: proteina, carboidrato e verdure a pranzo e a cena. Non dice cosa mangiare esattamente. Le regole vengono dal piano della nutrizionista in [`piano-alimentare/`](piano-alimentare/). Il design è in [`design/`](design/).

## Requisiti

### Settimana tipo

- 7 giorni (Lun–Dom) × pranzo e cena, senza date e senza storico.
- Ogni pasto è uno di questi:
  - una **categoria proteica** (legumi, pesce, uova, carne bianca, carne rossa, affettati, formaggi) con un carboidrato, più le verdure, sempre presenti;
  - un pasto **senza categoria**: Mensa, Fuori casa, A scelta.

### Promemoria

- Colazione, spuntino e merenda sono promemoria fissi, uguali ogni giorno.
- Non vengono generati né conteggiati.

### Bilancio

- Conta solo pranzi e cene con categoria, e li confronta con i min/max settimanali.
- Gli sforamenti vengono segnalati, mai impediti.

### Generazione

- Avviene solo su richiesta, dopo un passo vincoli in cui si segnano i pasti fuori, in mensa o a scelta.
- I pasti bloccati restano fissi.
- Criteri di varietà:
  - mai la stessa proteina due volte nello stesso giorno, se i pasti fissi lo permettono;
  - evitare la stessa proteina in pasti consecutivi, compreso il passaggio da domenica sera a lunedì a pranzo;
  - carboidrati il più possibile vari.

### Modifiche

- Cambio di categoria e di carboidrato, con l'anteprima dell'effetto sul bilancio.
- Blocco del pasto.
- Pasto senza categoria.
- Scambio di due pasti o di due giorni, trascinando oppure toccando.

### Regole

- I min/max e le liste di alternative con le porzioni si modificano dall'app.
- Si possono ripristinare i valori del piano.

### Navigazione

Quattro tab: Oggi · Settimana · Bilancio · Regole. L'app si apre sul giorno reale.

### Altre scelte

- Interfaccia solo in italiano.
- Solo tema chiaro.
- Uso personale: niente account, sincronizzazione o tracciamento.

### Casi limite

- **Regole impossibili:** gli stepper non permettono min > max. Se i pasti con categoria non bastano a coprire tutti i minimi (per esempio troppi pasti in mensa), il generatore riempie quello che può e il deficit compare nel Bilancio.
- **Limiti indipendenti:** carne rossa e affettati hanno ciascuno il proprio max (1 e 2), come nella tabella delle frequenze. Il limite combinato della piramide non viene applicato.
- **Promemoria esclusi dal bilancio:** formaggi o affettati a merenda non vengono conteggiati, anche se ci sono.
- **Carboidrati:** il generatore li varia. Il consiglio "il carboidrato della cena si ripete nel pranzo del giorno dopo" non viene applicato in automatico; si ottiene modificando il pasto a mano.
- **Lucchetto:** il blocco segue il pasto. Se scambi un pasto bloccato, il blocco si sposta con lui.

## Scelte tecniche

| Aspetto | Scelta |
|---|---|
| Linguaggio / UI | Kotlin, Jetpack Compose, Material 3 |
| Persistenza | Room, con un seed iniziale dagli asset |
| DI | Manuale, tramite `AppContainer` |
| Navigazione | Navigation Compose, con rotte type-safe |
| Test | JUnit per il dominio; Robolectric per Room e per la UI Compose |
| Qualità | ktlint, Android Lint |
| Build | Gradle (wrapper) con version catalog; JDK 21; minSdk 26 |

### Classificazione delle carni

La tabella porzioni ha un'unica voce "Carne"; l'app la divide secondo le linee guida italiane:
- **carne rossa:** manzo, vitello, maiale, agnello, cavallo, frattaglie;
- **affettati:** salumi, salsiccia e bresaola;
- **carne bianca:** pollo, tacchino, coniglio e gallina.

Le "associazioni" della tabella (es. "Uova + feta") non vengono importate. Le voci "Pane (invece della pasta)" sono marcate "· invece della pasta", per distinguerle dalle porzioni piccole di "Pane e sostituti".

### Dati iniziali

Al primo avvio il database contiene le regole e le alternative del piano (`app/src/main/assets/seed/foods.json`, generato con `python3 scripts/build_seed_foods.py`). La settimana resta vuota finché non la generi da "Nuova settimana".

## Architettura

Un solo modulo `app`, diviso in tre livelli:

```
it.mealmap
  domain/   Kotlin puro: modello, bilancio, modifiche alla settimana, generatore
  data/     Room (entity, DAO, database), seed, repository, promemoria
  ui/       tema, componenti, navigazione, una cartella per schermata
            (Screen stateless + ViewModel con StateFlow<UiState>)
```

Il dominio non dipende da Android, quindi le regole e il generatore si testano con test JVM veloci.

## Milestone

Ogni milestone si chiude con build, test e lint verdi, una code review e un commit.

| # | Contenuto |
|---|---|
| M0 | Documenti: design e questo piano |
| M1 | Scaffold Gradle, tema e bottom bar a 4 tab |
| M2 | Dominio: modello, bilancio, modifiche, generatore |
| M3 | Dati: Room, seed delle alternative, repository |
| M4 | Settimana (griglia) e Bilancio |
| M5 | Oggi, Dettaglio pasto e Promemoria |
| M6 | Modifica: cambia categoria, blocca, trascina per scambiare |
| M7 | Nuova settimana con vincoli |
| M8 | Regole: frequenze e alternative |
| M9 | Rifinitura, accessibilità e istruzioni di build |

## Verifica

```sh
./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
```

L'APK di debug finisce in `app/build/outputs/apk/debug/`.
