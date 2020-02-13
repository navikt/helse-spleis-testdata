<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';
    import DateInput from './form/DateInput.svelte';

    let invalid = false;
    let fnr = '';
    let orgnummer = '';
    let sykdomFom = '2020-01-01';
    let sykdomTom = '2020-01-31';
    let inntekt = '';

    const onSubmit = async () => {
        const vedtak = { fnr, inntekt, orgnummer, sykdomFom, sykdomTom };

        return await fetch(`/vedtaksperiode/`, {
            method: 'post',
            body: JSON.stringify(vedtak),
            headers: { "Content-Type": "application/json" }
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
    }
</script>

<Form onSubmit={onSubmit} submitText="Opprett vedtaksperiode">
    <Input bind:value={fnr} onblur={hentInntekt} label="Fødselsnummer" placeholder="Arbeidstakers fødselsnummer" required />
    <Input bind:value={orgnummer} label="Organisasjonsnummer" placeholder="Arbeidsgivers organisasjonsnummer" required />
    <Input class="input" bind:value={inntekt} label="Inntekt" placeholder="0" required />

    <DateInput bind:value={sykdomFom} label="Sykdom f.o.m." required />
    <DateInput bind:value={sykdomTom} label="Sykdom t.o.m." required />
</Form>
