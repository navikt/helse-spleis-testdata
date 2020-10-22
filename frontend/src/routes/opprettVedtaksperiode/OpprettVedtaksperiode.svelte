<script>
    import {
        addArbeidsgiverperiode,
        addFerie,
        addRefusjonsendring,
        removeArbeidsgiverperiode,
        removeFerie,
        removeRefusjonsendring,
        updateInntekt,
        vedtaksperiodeStore
    } from './vedtaksperiodeStore';
    import {deletePerson, getInntekt, postVedtaksperiode} from '../../io/http';
    import Card from '../../components/Card.svelte';
    import Form from '../../components/form/Form.svelte';
    import Input from '../../components/form/Input.svelte';
    import Toggle from '../../components/form/Toggle.svelte';
    import Mortness from '../../components/mortness/Mortness.svelte';
    import FormGroup from '../../components/form/FormGroup.svelte';
    import DateInput from '../../components/form/DateInput.svelte';
    import AddButton from '../../components/form/AddButton.svelte';
    import DateRange from '../../components/form/DateRange.svelte';
    import NumberInput from '../../components/form/NumberInput.svelte';
    import SubmitButton from '../../components/form/SubmitButton.svelte';
    import ContentColumn from '../../components/ContentColumn.svelte';
    import DeleteButton from '../../components/form/DeleteButton.svelte';

    let status;

    $: vedtaksperiode = $vedtaksperiodeStore;

    let invalid = false;
    let gjenopprett = false;
    let skalSendeSøknad = true;
    let skalSendeSykmelding = true;
    let skalSendeInntektsmelding = true;
    let harAndreInntektskilder = false;

    const deletePersonOnGjenopprett = async () =>
        gjenopprett ? deletePerson({fnr: vedtaksperiode.fnr}) : Promise.resolve()

    const onSubmit = () => deletePersonOnGjenopprett()
        .then(() => postVedtaksperiode({
            vedtaksperiode: {
                ...vedtaksperiode,
                skalSendeSøknad,
                skalSendeSykmelding,
                skalSendeInntektsmelding,
                harAndreInntektskilder
            }
        }));

    const hentInntekt = async event => {
        event.preventDefault();
        const respons = await getInntekt({fnr: vedtaksperiode.fnr});
        respons.json().then(data => updateInntekt(data.beregnetMånedsinntekt));
    };
</script>

<ContentColumn>
    <Form {onSubmit} bind:status>
        <Card title="Person">
            <FormGroup>
                <Input
                    bind:value="{vedtaksperiode.fnr}"
                    onBlur="{hentInntekt}"
                    placeholder="Fødselsnummer"
                    required
                />
                <Input bind:value="{vedtaksperiode.orgnummer}" placeholder="Organisasjonsnummer" required />
                <NumberInput
                    bind:value="{vedtaksperiode.inntekt}"
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
            <DateInput bind:value="{vedtaksperiode.førstefraværsdag}" label="Første fraværsdag" />
            <DateInput bind:value="{vedtaksperiode.sykdomFom}" label="Sykdom f.o.m." required />
            <DateInput bind:value="{vedtaksperiode.sykdomTom}" label="Sykdom t.o.m." required />
        </Card>

        <Card title="Søknad">
            <DateInput bind:value="{vedtaksperiode.sendtNav}" label="Søknad sendt NAV" />
            <DateInput bind:value="{vedtaksperiode.sendtArbeidsgiver}" label="Søknad sendt arbeidsgiver" />
            <DateInput bind:value="{vedtaksperiode.opphørRefusjon}" label="Opphør refusjon" />
            <NumberInput
                bind:value="{vedtaksperiode.sykmeldingsgrad}"
                placeholder="Sykdomsgrad i sykmeldingen"
                required
                min="0"
                max="100"
            />
            <NumberInput
                bind:value="{vedtaksperiode.faktiskgrad}"
                placeholder="Faktisk arbeidsgrad i søknad"
                min="0"
                max="100"
            />
        </Card>

        <AddButton label="Legg inn arbeidsgiverperioder" onClick="{addArbeidsgiverperiode}" />
        <span class="perioder">
            {#each vedtaksperiode.arbeidsgiverperiode as periode, i (periode.id)}
                <DateRange
                    bind:start="{periode.fom}"
                    startLabel="Arbeidsgiverperiode f.o.m."
                    bind:end="{periode.tom}"
                    endLabel="Arbeidsgiverperiode t.o.m."
                    onRemove="{() => removeArbeidsgiverperiode(i)}"
                />
            {/each}
        </span>
        <AddButton label="Legg inn ferie" onClick="{addFerie}" />
        <span class="perioder">
            {#each vedtaksperiode.ferieperioder as ferie, i (ferie.id)}
                <DateRange
                    bind:start="{ferie.fom}"
                    startLabel="Ferie f.o.m."
                    bind:end="{ferie.tom}"
                    endLabel="Ferie t.o.m."
                    onRemove="{() => removeFerie(i)}"
                />
            {/each}
        </span>
        <AddButton label="Legg inn endring i refusjon" onClick="{addRefusjonsendring}" />
        <span class="perioder">
            {#each vedtaksperiode.endringRefusjon as endring, i (endring.id)}
                <span class="refusjonsendring">
                    <Card>
                        <DateInput bind:value="{endring}" label="Dato for endring" />
                    </Card>
                    <DeleteButton onClick="{() => removeRefusjonsendring(i)}" />
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
