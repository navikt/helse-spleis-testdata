# Generere lønnsinformasjon fra kommandolinja

Dette verktøyet kan brukes i utviklerimage, grunnet tilgang til systemer (man kan sikkert port-forwarde.)

I source-me ligger kode for å kunne opprette lønninger i inntektstubben.

Hvordan bruke:

- Åpne en terminal
- source filen: `. helse-test-data/cli/source-me`
- Kjør `auth` for å autentisere
- Kjør ønsket kommando

Eksempler:

- `opprett_inntekter <fnr> <månedslønn>`
- `slett_alle_inntekter <fnr>`
- `hent_inntekter <fnr>`
