<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';
    import DateInput from './form/DateInput.svelte';
    import NumberInput from './form/NumberInput.svelte';
    import { deletePerson, getInntekt, postVedtaksperiode } from '../io/http';
    import Switch from './form/Toggle.svelte';
    import AddButton from './form/AddButton.svelte';
    import DateRange from './form/DateRange.svelte';
    import Mortness from './form/Mortness.svelte';

    let invalid = false;
    let fnr = '';
    let orgnummer = '';
    let sykdomFom = '2020-01-01';
    let sykdomTom = '2020-01-31';
    let inntekt = '';
    let harAndreInntektskilder = false;
    let gjenopprett = false;
    let skalSendeInntektsmelding = true;
    let skalSendeSykmelding = true;
    let skalSendeSøknad = true;
    let sendtNav = '2020-02-01';
    let sendtArbeidsgiver = null;
    let sykmeldingsgrad = 100;
    let faktiskgrad = null;
    let arbeidsgiverperiode = [];
    let ferieInntektsmelding = [];
    let førstefraværsdag = '2020-01-01';

    const onSubmit = async () => {
        const vedtak = {
            fnr,
            inntekt,
            orgnummer,
            sykdomFom,
            sykdomTom,
            harAndreInntektskilder,
            skalSendeInntektsmelding,
            skalSendeSykmelding,
            skalSendeSøknad,
            sendtNav,
            sendtArbeidsgiver,
            sykmeldingsgrad,
            faktiskgrad,
            førstefraværsdag,
            arbeidsgiverperiode,
            ferieInntektsmelding
        };

        if (gjenopprett) await deletePerson({ fnr });
        return await postVedtaksperiode({ vedtak });
    };

    const hentInntekt = async event => {
        event.preventDefault();
        const respons = await getInntekt({ fnr });
        respons.json().then(data => (inntekt = data.beregnetMånedsinntekt));
    };

    const leggTilPeriode = () => {
        arbeidsgiverperiode = arbeidsgiverperiode.concat({});
    };

    const fjernPeriode = i => {
        arbeidsgiverperiode = [
            ...arbeidsgiverperiode.slice(0, i),
            ...arbeidsgiverperiode.slice(i + 1)
        ];
    };

    const leggTilFerie = () => {
        ferieInntektsmelding = ferieInntektsmelding.concat({});
    };

    const fjernFerie = i => {
        ferieInntektsmelding = [
            ...ferieInntektsmelding.slice(0, i),
            ...ferieInntektsmelding.slice(i + 1)
        ];
    };
</script>

<Form {onSubmit} submitText="Opprett vedtaksperiode">
    <span class="form-group">
        <Input
            bind:value="{fnr}"
            onblur="{hentInntekt}"
            label="Fødselsnummer"
            placeholder="Arbeidstakers fødselsnummer"
            required
        />
        <Switch label="Slett og gjenskap data for personen" bind:checked="{gjenopprett}" />
    </span>
    <span class="form-group">
        <Input
            bind:value="{orgnummer}"
            label="Organisasjonsnummer"
            placeholder="Arbeidsgivers organisasjonsnummer"
            required
        />
    </span>
    <span class="form-group">
        <Switch label="Send sykmelding" bind:checked="{skalSendeSykmelding}" />
        <Switch label="Send søknad" bind:checked="{skalSendeSøknad}" />
        <Switch label="Send inntektsmelding" bind:checked="{skalSendeInntektsmelding}" />
        <Switch label="Har andre inntektskilder" bind:checked="{harAndreInntektskilder}" />
    </span>
    <span class="form-group">
        <DateInput bind:value="{førstefraværsdag}" label="Første fraværsdag" />
        <DateInput bind:value="{sykdomFom}" label="Sykdom f.o.m." required />
        <DateInput bind:value="{sykdomTom}" label="Sykdom t.o.m." required />
    </span>

    <span class="form-group">
        <AddButton label="Legg inn arbeidsgiverperioder" onClick="{leggTilPeriode}" />
        <span>
            {#each arbeidsgiverperiode as periode, i}
                <DateRange
                    bind:start="{periode.fom}"
                    startLabel="Arbeidsgiverperiode f.o.m."
                    bind:end="{periode.tom}"
                    endLabel="Arbeidsgiverperiode t.o.m."
                    onRemove="{() => fjernPeriode(i)}"
                />
            {/each}
        </span>
        <AddButton label="Legg inn ferie" onClick="{leggTilFerie}" />
        <span>
            {#each ferieInntektsmelding as ferie, i}
                <DateRange
                    bind:start="{ferie.fom}"
                    startLabel="Ferie f.o.m."
                    bind:end="{ferie.tom}"
                    endLabel="Ferie t.o.m."
                    onRemove="{() => fjernFerie(i)}"
                />
            {/each}
        </span>
    </span>

    <span class="form-group">
        <DateInput bind:value="{sendtNav}" label="Søknad sendt NAV" />
        <DateInput bind:value="{sendtArbeidsgiver}" label="Søknad sendt arbeidsgiver" />
        <NumberInput
            bind:value="{sykmeldingsgrad}"
            label="Sykmeldingsgrad"
            placeholder="Sykdomsgrad på sykmeldingen"
            required
            min="0"
            max="100"
        />
        <NumberInput
                bind:value="{faktiskgrad}"
                label="Faktisk arbeidsgrad i søknad"
                placeholder="Faktisk arbeidsgrad"
                min
                max
        />
    </span>
    <span class="form-group">
        <Input
            bind:value="{inntekt}"
            label="Inntekt"
            placeholder="0"
            required="{skalSendeInntektsmelding}"
            disabled="{!skalSendeInntektsmelding}"
        />
    </span>
    <span class="form-group">
        <Mortness />
    </span>
</Form>

<style>
    .form-group {
        margin-bottom: 1.5rem;
        width: max-content;
    }
</style>
