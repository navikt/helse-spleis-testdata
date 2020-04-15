import { writable } from 'svelte/store';
import { uuid } from '../../scripts/uuid';

export const vedtaksperiodeStore = writable({
    fnr: '',
    orgnummer: '',
    sykdomFom: '2020-01-01',
    sykdomTom: '2020-01-31',
    inntekt: '',
    harAndreInntektskilder: false,
    sendtNav: '2020-02-01',
    sendtArbeidsgiver: null,
    sykmeldingsgrad: 100,
    faktiskgrad: null,
    arbeidsgiverperiode: [],
    ferieInntektsmelding: [],
    førstefraværsdag: '2020-01-01',
    opphørRefusjon: null,
    endringRefusjon: []
});

const addPeriode = key => vedtaksperiodeStore.update(periode => ({
    ...periode,
    [key]: [...periode[key], { id: uuid() }]
}));

export const updateInntekt = inntekt => vedtaksperiodeStore.update(periode => ({ ...periode, inntekt }));

export const addArbeidsgiverperiode = () => addPeriode('arbeidsgiverperiode');

export const addFerie = () => addPeriode('ferieInntektsmelding');

export const addRefusjonsendring = () => addPeriode('endringRefusjon');

export const removeArbeidsgiverperiode = i => vedtaksperiodeStore.update(periode => ({
    ...periode,
    arbeidsgiverperiode: [
        ...periode.arbeidsgiverperiode.slice(0, i),
        ...periode.arbeidsgiverperiode.slice(i + 1)
    ]
}));

export const removeFerie = i => vedtaksperiodeStore.update(periode => ({
    ...periode,
    ferieInntektsmelding: [
        ...periode.ferieInntektsmelding.slice(0, i),
        ...periode.ferieInntektsmelding.slice(i + 1)
    ]
}));

export const removeRefusjonsendring = i => vedtaksperiodeStore.update(periode => ({
    ...periode,
    endringRefusjon: [
        ...periode.endringRefusjon.slice(0, i),
        ...periode.endringRefusjon.slice(i + 1)
    ]
}));
