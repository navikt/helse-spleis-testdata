<script>
    import { deletePerson, getInntekt, postVedtaksperiode } from '../io/http';
    import { uuid } from '../scripts/uuid';
    import Card from '../components/Card.svelte';
    import Form from '../components/form/Form.svelte';
    import Input from '../components/form/Input.svelte';
    import Toggle from '../components/form/Toggle.svelte';
    import Mortness from '../components/mortness/Mortness.svelte';
    import FormGroup from '../components/form/FormGroup.svelte';
    import DateInput from '../components/form/DateInput.svelte';
    import AddButton from '../components/form/AddButton.svelte';
    import DateRange from '../components/form/DateRange.svelte';
    import NumberInput from '../components/form/NumberInput.svelte';
    import SubmitButton from '../components/form/SubmitButton.svelte';
    import ContentColumn from '../components/ContentColumn.svelte';
    import DeleteButton from '../components/form/DeleteButton.svelte';

    let status;

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
    let opphørRefusjon = null;
    let endringRefusjon = [];

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
            ferieInntektsmelding,
            opphørRefusjon,
            endringRefusjon
        };

        if (gjenopprett) await deletePerson({ fnr });
        return await postVedtaksperiode({ vedtak });
    };

    const hentInntekt = async event => {
        event.preventDefault();
        const respons = await getInntekt({ fnr });
        respons.json().then(data => (inntekt = data.beregnetMånedsinntekt));
        console.log(respons);
    };

    const leggTilPeriode = () => {
        arbeidsgiverperiode = arbeidsgiverperiode.concat({ id: uuid() });
    };

    const fjernPeriode = i => {
        arbeidsgiverperiode = [
            ...arbeidsgiverperiode.slice(0, i),
            ...arbeidsgiverperiode.slice(i + 1)
        ];
    };

    const leggTilFerie = () => {
        ferieInntektsmelding = ferieInntektsmelding.concat({ id: uuid() });
    };

    const fjernFerie = i => {
        ferieInntektsmelding = [
            ...ferieInntektsmelding.slice(0, i),
            ...ferieInntektsmelding.slice(i + 1)
        ];
    };

    const leggTilEndringRefusjon = () => {
        endringRefusjon = endringRefusjon.concat({ id: uuid() });
    };

    const fjernEndring = i => {
        endringRefusjon = [
            ...endringRefusjon.slice(0, i),
            ...endringRefusjon.slice(i + 1)
        ];
    };
</script>

<ContentColumn>
    <Form {onSubmit} bind:status>
        <Card title="Person">
            <FormGroup>
                <Input
                    bind:value="{fnr}"
                    onBlur="{hentInntekt}"
                    placeholder="Fødselsnummer"
                    required
                />
                <Input bind:value="{orgnummer}" placeholder="Organisasjonsnummer" required />
                <NumberInput
                    bind:value="{inntekt}"
                    placeholder="Inntekt"
                    required="{skalSendeInntektsmelding}"
                    disabled="{!skalSendeInntektsmelding}"
                />
            </FormGroup>
            <Toggle label="Slett og gjenskap data for personen" bind:checked="{gjenopprett}" />
            <Toggle label="Send sykmelding" bind:checked="{skalSendeSykmelding}" />
            <Toggle label="Send søknad" bind:checked="{skalSendeSøknad}" />
            <Toggle label="Send inntektsmelding" bind:checked="{skalSendeInntektsmelding}" />
            <Toggle label="Har andre inntektskilder" bind:checked="{harAndreInntektskilder}" />
        </Card>
        <Card title="Sykdom">
            <DateInput bind:value="{førstefraværsdag}" label="Første fraværsdag" />
            <DateInput bind:value="{sykdomFom}" label="Sykdom f.o.m." required />
            <DateInput bind:value="{sykdomTom}" label="Sykdom t.o.m." required />
        </Card>

        <Card title="Søknad">
            <DateInput bind:value="{sendtNav}" label="Søknad sendt NAV" />
            <DateInput bind:value="{sendtArbeidsgiver}" label="Søknad sendt arbeidsgiver" />
            <DateInput bind:value="{opphørRefusjon}" label="Opphør refusjon" />
            <NumberInput
                bind:value="{sykmeldingsgrad}"
                placeholder="Sykdomsgrad i sykmeldingen"
                required
                min="0"
                max="100"
            />
            <NumberInput
                bind:value="{faktiskgrad}"
                placeholder="Faktisk arbeidsgrad i søknad"
                min="0"
                max="100"
            />
        </Card>

        <AddButton label="Legg inn arbeidsgiverperioder" onClick="{leggTilPeriode}" />
        <span class="perioder">
            {#each arbeidsgiverperiode as periode, i (periode.id)}
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
        <span class="perioder">
            {#each ferieInntektsmelding as ferie, i (ferie.id)}
                <DateRange
                    bind:start="{ferie.fom}"
                    startLabel="Ferie f.o.m."
                    bind:end="{ferie.tom}"
                    endLabel="Ferie t.o.m."
                    onRemove="{() => fjernFerie(i)}"
                />
            {/each}
        </span>
        <AddButton label="Legg inn endring i refusjon" onClick="{leggTilEndringRefusjon}" />
        <span class="perioder">
            {#each endringRefusjon as endring, i (endring.id)}
                <span class="refusjonsendring">
                    <Card>
                        <DateInput
                            bind:value="{endring}"
                            label="Dato for endring"
                        />
                    </Card>
                    <DeleteButton onClick="{() => fjernEndring(i)}" />
                </span>
            {/each}
        </span>
        <Mortness />
        <SubmitButton className="block-l" value="Opprett vedtaksperiode" {status} />
    </Form>
</ContentColumn>

<style>
    :global(.perioder > *:last-child) {
        margin-bottom: 1.5rem;
    }
    .refusjonsendring {
        display: flex;
        align-items: center;
    }
    .refusjonsendring:not(:last-child) {
        margin-bottom: 1.5rem;
    }
    :global(.refusjonsendring > .card) {
        margin-bottom: 0;
        margin-right: 1rem;
    }
</style>
