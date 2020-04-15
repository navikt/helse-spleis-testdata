import { writable } from 'svelte/store';

export const Routes = {
    SLETT_PERSON: 'Slett person',
    OPPRETT_VEDTAKSPERIODE: 'Opprett vedtaksperiode',
    HENT_INNTEKT: 'Hent inntekt',
    HENT_AKTØRID: 'Hent aktør-ID',
    OPPRETT_PERSON: 'Opprett person'
};

const initialRoute = process.env.NODE_ENV === 'development'
    ? Routes.OPPRETT_PERSON
    : Routes.OPPRETT_VEDTAKSPERIODE;

export const route = writable(initialRoute);

export const setCurrentRoute = newRoute => route.set(newRoute);
