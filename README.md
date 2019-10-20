# jBiblioScan

Companion App Android per jBiblio.

## Android

- minSDK 24
- targetSDK 29
- compileSDK 29

### Download

L'applicazione (l'apk) si può scaricare come release su GitHub.

## Uso

1. Avviare l'applicazione jBiblio.
2. Cliccare sulla voce di menù `jBiblio\Connetti App`
3. Si aprirà una piccola finestra con un codice QR
4. Avviare ora l'applicazione jBiblioScan sul vostro smartphone
    NB: il computer su cui è avviato jBiblio e lo smartphone devono essere collegati alla stessa rete WiFi!
5. Sull'applicazione, _tappare_ sul pulsante `CONNETTI A JBIBLIO`
6. Si aprirà una nuova schermata che mostra ciò che la fotocamera sta catturando.
    NB: la prima volta potrebbe richiedere il permesso d'uso della fotocamera, accettare.
7. Inquadrare il codice QR e attendere che venga confermata la connessione.

A questo punto jBiblio mosterà il messaggio (in verde) `App connessa` in alto a destra (sulla barra dei menù).

Ora il collegameno è stato stabilito: mettiamo il caso vogliamo inserire un nuovo libro.

1. Su jBiblio, aprire `Inventario\Nuovo Libro`.
2. Sull'applicazione, _tappare_ sul pulsante `SCANSIONE CODICE A BARRE`.
3. Si aprirà una finestra identica a quella della connessione
4. Inquadrare il codice a barre sul libro che volete inserire e attendere qualche istante.
    Un suono di notifica verrà riprodotto alla lettura del codice.
5. Su jBiblio, l'ISBN apparirà nel campo apposito e verrà lanciata la ricerca del libro su Google Books.
6. Sull'app, apparirà un messaggio che chiederà se si voglia scansionare un altro codice a barre oppure no.
7. Se si sceglie di scansionare un nuovo codice, tornare al punto **3**.
8. Se si sceglie di tornare alla schermata principale, si può decidere di disconnettersi.
9. _Tappare_ semplicemente sul tasto `DISCONNETTI` ed attendere il messaggio di avvenuta operazione.
10. Su jBiblio ora in alto a destra dovreste leggere `App non connessa` in grigio.

Qualsiasi schermata, su jBiblio, che abbia un campo ISBN compilabile, può essere popolato tramite applicazione.

## Autore

**Pier Riccardo Monzo** - *One man team* - [IslandOfCode.it](https://www.islandofcode.it/)

## Licenza

Questo programma è sviluppato sotto licenza **GPLv3**.

Fate riferimento al file [LICENSE.md](LICENSE.md), per avere sempre la versione più aggiornata della licensa.