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
- In fondo, anche senza una settimana pianificata, una card apre un pannello dal basso su come si compone un pasto sano: il piatto (½ verdura e frutta, ¼ cereali, ¼ proteine), le frequenze settimanali della piramide, l'organizzazione della giornata e il riepilogo numerico di [`linee_guida.md`](piano-alimentare/linee_guida.md). Il testo è fisso: le frequenze della piramide non dipendono dalle Regole.

### Generazione

- Avviene solo su richiesta, dopo un passo vincoli in cui si segnano i pasti fuori, in mensa o a scelta.
- I pasti bloccati restano fissi.
- Criteri di varietà:
  - mai la stessa proteina due volte nello stesso giorno, se i pasti fissi lo permettono;
  - una proteina mangiata un giorno (a pranzo o a cena) non torna il giorno dopo, compreso il passaggio da domenica a lunedì;
  - al massimo una ripetizione in giorni consecutivi a settimana, e i min/max restano sempre rispettati. Con i default i posti liberi senza ripetizioni sono esattamente 14, quindi quasi sempre il posto in più va a caso a una categoria sotto il max (di solito legumi o pesce, al quarto giorno) e crea l'unica ripetizione;
  - le ripetizioni causate dai pasti fissi o bloccati non contano nel limite di una;
  - carboidrati il più possibile vari.

### Modifiche

- Cambio di categoria e di carboidrato, con l'anteprima dell'effetto sul bilancio.
- Blocco del pasto.
- Pasto senza categoria.
- Scambio di due pasti o di due giorni, trascinando oppure toccando.

### Regole

- I min/max e le liste di alternative con le porzioni si modificano dall'app.
- I min/max si possono riportare ai valori predefiniti, dopo una conferma. Le alternative non hanno un ripristino: tornano quelle del piano solo cancellando i dati dell'app.
- I promemoria di colazione, spuntino e merenda sono i tipi fissi del piano. Le liste "Colazione" e "Spuntini e merende" delle alternative servono solo come riferimento per le porzioni e non li modificano.

### Navigazione

Quattro tab: Oggi · Settimana · Bilancio · Impostazioni. L'app si apre sul giorno reale. Impostazioni contiene le Regole e, in fondo, la versione installata con il pulsante "Controlla aggiornamenti".

### Altre scelte

- Interfaccia solo in italiano.
- Solo tema chiaro.
- Uso personale: niente account, sincronizzazione o tracciamento.

### Casi limite

- **Stepper delle regole:** permettono di impostare:
  - il minimo, da "nessuno" in su;
  - il massimo, fino a 0 ("mai");
  - "Nessun limite", con un pulsante apposito. Il mockup toglieva il limite premendo "−" su 1: è stato cambiato perché faceva il contrario di quanto ci si aspetta.
- **Regole impossibili:** gli stepper non permettono min > max. Se i pasti con categoria non bastano a coprire tutti i minimi (per esempio troppi pasti in mensa), il generatore riempie quello che può e il deficit compare nel Bilancio.
- **Limiti indipendenti:** carne rossa e affettati hanno ciascuno il proprio max, 1 e 1. La tabella delle frequenze dava 2 agli affettati, ma la piramide indica meno di una porzione a settimana. Il limite combinato della piramide non viene applicato. Le regole già salvate non cambiano da sole: i nuovi valori si applicano con "Ripristina i valori predefiniti".
- **Promemoria esclusi dal bilancio:** formaggi o affettati a merenda non vengono conteggiati, anche se ci sono.
- **Carboidrati:** il generatore li varia. Il consiglio "il carboidrato della cena si ripete nel pranzo del giorno dopo" non viene applicato in automatico; si ottiene modificando il pasto a mano.
- **Lucchetto:** il blocco segue il pasto. Se scambi un pasto bloccato, il blocco si sposta con lui. Solo i pasti con categoria si bloccano.
- **Nuova settimana:**
  - i pasti bloccati partono come "Fisso";
  - i pasti senza categoria della settimana attuale partono uguali, perché è probabile che si ripetano;
  - tutto il resto parte come "Genera".
- **Alternative del carboidrato:** ogni porzione da pasto del gruppo "Cereali e carboidrati" dice quali carboidrati sostituisce (es. "Riso / pasta / orzo" vale per pasta, riso e orzo). Il dettaglio di un pasto mostra solo quelle. Le porzioni di contorno ("Pane e sostituti") e i misti restano nella lista completa.

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

### Rilascio

- **Quando:** ogni push su `main` diventa una release GitHub `v1.0.N`, dove N è il numero di esecuzione della Action e fa anche da `versionCode`. Esce una release anche per modifiche che non toccano l'app.
- **Aggiornamenti:** il pulsante "Controlla aggiornamenti" in Impostazioni legge l'ultima release del repo dall'API di GitHub (il repo è pubblico, non serve un token). Se il suo N è maggiore del `versionCode` installato, scarica l'APK e apre l'installer di sistema. La prima volta Android chiede di permettere a Meal Map di installare app.
- **Firma:** una chiave di release dedicata, nei secret del repo. Le build locali senza chiave restano non firmate.
- **Database:** l'aggiornamento conserva i dati, quindi ogni modifica allo schema Room richiede una migrazione. Senza, l'app aggiornata non si apre.

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
