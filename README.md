# helse-spleis-testdata

[helse-spleis](https://www.github.com/navikt/helse-spleis)

## Beskrivelse
Oppretter og manipulerer testdata for spleis.

## Kjøretidsavhengigheter

* JDK 12
* Kafka

## Kjøre lokalt

### Frontend

Frontenden er skrevet i [Svelte](https://svelte.dev/) og bygges med [Parcel](https://parceljs.org/), og startes fra `frontend`-mappen slik:

````shell script
# Installér avhengigheter i package.json:
npm i

# Bygg frontenden og start dev-server:
npm start
````

Skal du teste frontenden sammen med backenden må du først bygge appen slik at de bundlede filene kan serves av backenden fra `public`-mappen:

```shell script
# Kjøres fra frontend-mappen:
npm run build
```

### Backend

Lokal app med mocket kafka-producer og embedded postgres-db kan kjøres ved å starte main-funksjonen i `LocalApp.kt`. Serveren svarer på `0.0.0.0:8080` og server filer fra `public`-mappen hvis du har bygd frontend-appen på forhånd.
