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

const removePeriode = (key, index) => vedtaksperiodeStore.update(periode => ({
    ...periode,
    [key]: [
        ...periode[key].slice(0, index),
        ...periode[key].slice(index + 1)
    ]
}))

export const updateInntekt = inntekt => vedtaksperiodeStore.update(periode => ({ ...periode, inntekt }));

export const addFerie = () => addPeriode('ferieInntektsmelding');
export const addRefusjonsendring = () => addPeriode('endringRefusjon');
export const addArbeidsgiverperiode = () => addPeriode('arbeidsgiverperiode');

export const removeFerie = i => removePeriode('ferieInntektsmelding', i);
export const removeRefusjonsendring = i => removePeriode('endringRefusjon', i);
export const removeArbeidsgiverperiode = i => removePeriode('arbeidsgiverperiode', i);

