# Design dell'app

Mockup delle 10 schermate di Meal Map, uno per file `*.dc.html`. `canvas.json` descrive come sono disposti sulla tavola: tre sezioni, i titoli delle schermate e le note.

Versione navigabile: https://claude.ai/artifact/JkdKc1nh2GtodJAEAzPU17

I file sono il riferimento visivo dell'implementazione. Lo stato e i dati che contengono sono d'esempio.

| Sezione | Schermate |
|---|---|
| 1 · Consultare | `Main` (Oggi), `Dettaglio`, `Promemoria`, `Bilancio` |
| 2 · Pianificare e modificare | `Settimana`, `Trascina`, `CambiaCategoria`, `NuovaSettimana` |
| 3 · Regole della nutrizionista | `Regole`, `Alternative` |

## Limiti dei mockup

- **Runtime esterno:** i file dipendono da `support.js`, il runtime dell'artefatto, che non è nel repo. Per vederli renderizzati usa il link qui sopra.
- **Dati duplicati:** ogni schermata ha la propria copia di dati e icone, e alcune copie non coincidono. Per esempio `CambiaCategoria` mostra "Ora: Legumi" su sabato pranzo, e in `Trascina` manca il lucchetto. L'app calcola tutto dallo stato reale; le regole valide sono in [`../sviluppo.md`](../sviluppo.md).
