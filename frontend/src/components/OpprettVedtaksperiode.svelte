<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';
    import DateInput from './form/DateInput.svelte';
    import NumberInput from "./form/NumberInput.svelte";

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
    let sykmeldingsgrad = 100;
    let arbeidsgiverperiode = [];
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
            sykmeldingsgrad,
            førstefraværsdag,
            arbeidsgiverperiode
        };

        if (gjenopprett) {
            await fetch(`/person`, {
                method: 'delete',
                headers: {'ident': fnr}
            });
        }
        return await fetch(`/vedtaksperiode/`, {
            method: 'post',
            body: JSON.stringify(vedtak),
            headers: {"Content-Type": "application/json"}
        });
    };

    const hentInntekt = async (event) => {
        event.preventDefault();
        const result = await fetch(`/person/inntekt`, {
            method: 'get',
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "ident": fnr
            }
        });
        let respons = await result.json();
        inntekt = respons.beregnetMånedsinntekt;
    };

    const leggTilPeriode = () =>  {
        arbeidsgiverperiode = arbeidsgiverperiode.concat({})}
</script>

<Form onSubmit={onSubmit} submitText="Opprett vedtaksperiode">
    <Input bind:value={fnr} onblur={hentInntekt} label="Fødselsnummer" placeholder="Arbeidstakers fødselsnummer"
           required />
    <label class="switch" for=gjenopprett>Slett og gjenskap data for personen
        <input type=checkbox id=gjenopprett bind:checked={gjenopprett} />
        <span class="slider"></span>
    </label>
    <br />
    <Input
        bind:value={orgnummer}
        label="Organisasjonsnummer"
        placeholder="Arbeidsgivers organisasjonsnummer"
        required
    />
    <br />
    <Input
        bind:value={inntekt}
        label="Inntekt"
        placeholder="0"
        required={skalSendeInntektsmelding}
        disabled={!skalSendeInntektsmelding}
    />

    <label class="switch" for=sendSykmelding>Send sykmelding
        <input type="checkbox" id=sendSykmelding bind:checked={skalSendeSykmelding} />
        <span class="slider"></span>
    </label>
    <label class="switch" for=sendSøknad>Send søknad
        <input type="checkbox" id=sendSøknad bind:checked={skalSendeSøknad} />
        <span class="slider"></span>
    </label>
    <label class="switch" for=sendInntektsmelding>Send inntektsmelding
        <input type="checkbox" id=sendInntektsmelding bind:checked={skalSendeInntektsmelding} />
        <span class="slider"></span>
    </label>

    <label class="switch" for=inntekstkilder>Har andre inntektskilder
        <input type=checkbox id=inntekstkilder bind:checked={harAndreInntektskilder} />
        <span class="slider"></span>
    </label>

    <br />

    <div>
        <DateInput bind:value={førstefraværsdag} label="Første fraværsdag"/>
        <span>Legg inn arbeidsgiverperioder: <button on:click|preventDefault={leggTilPeriode}> + </button></span>
        {#each arbeidsgiverperiode as _, i}
            <DateInput bind:value={arbeidsgiverperiode[i].fom} label="Arbeidsgiverperiode FOM" required />
            <DateInput bind:value={arbeidsgiverperiode[i].tom} label="Arbeidsgiverperiode TOM" required />
        {/each}
    </div>

    <DateInput bind:value={sykdomFom} label="Sykdom f.o.m." required />
    <DateInput bind:value={sykdomTom} label="Sykdom t.o.m." required />
    <DateInput bind:value={sendtNav} label="Søknad sendt NAV" required />

    <NumberInput bind:value={sykmeldingsgrad} label="Sykmeldingsgrad" placeholder="Sykdomsgrad på sykmeldingen" required min=0 max=100 />

    <label>
        Dette er en slider!
        <br/>
        mort <input type="range"> mortest
    </label>

</Form>

