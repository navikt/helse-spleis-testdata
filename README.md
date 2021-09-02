# <div style="display:flex;align-items:center"><img style="margin-right:16px;" src="https://raw.githubusercontent.com/navikt/helse-spleis-testdata/master/frontend/src/assets/logo.png" width="35" height="35">Spleis-testdata</div>


## Beskrivelse
Oppretter og manipulerer testdata for [spleis](https://www.github.com/navikt/helse-spleis).

## Kjøretidsavhengigheter

* JDK 15
* Kafka

## Kjøre lokalt

### Frontend

Frontenden er skrevet i [React](https://reactjs.org) og bygges med [Vite](https://vitejs.dev), og startes fra `frontend`-mappen slik:

````shell script
# Installér avhengigheter i package.json:
npm i

# Bygg frontenden og start dev-server:
npm run start
````

Skal du teste frontenden sammen med backenden må du først bygge appen slik at de bundlede filene kan serves av backenden fra `public`-mappen:

```shell script
# Kjøres fra frontend/:
npm run build

# Evt watch mode dersom du ønsker å bygge mens du skriver kode:
npm run watch
```

### Backend

Lokal app med mocket rapids-and-rivers og embedded postgres-db kan kjøres ved å starte main-funksjonen i `LocalApp.kt`. Serveren svarer på `0.0.0.0:8080` og server filer fra `public`-mappen hvis du har bygd frontend-appen på forhånd.
