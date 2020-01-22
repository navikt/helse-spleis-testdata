<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';
    import DateInput from './form/DateInput.svelte';

    let invalid = false;
    let aktørId = '';
    let fnr = '';
    let orgnummer = '';
    let sykdomFom = '';
    let sykdomTom = '';

    const onSubmit = async () => {
        const vedtak = { aktørId, fnr, orgnummer, sykdomFom, sykdomTom };

        return await fetch(`/vedtaksperiode/`, {
            method: 'post',
            body: JSON.stringify(vedtak),
            headers: { "Content-Type": "application/json" }
        });
    };
</script>

<Form onSubmit={onSubmit} submitText="Opprett vedtaksperiode">
    <Input bind:value={aktørId} label="Aktør-id" placeholder="Arbeidstakers aktør-id" invalid={invalid} required />
    <Input bind:value={fnr} label="Fødselsnummer" placeholder="Arbeidstakers fødselsnummer" required />
    <Input bind:value={orgnummer} label="Organisasjonsnummer" placeholder="Arbeidsgivers organisasjonsnummer" required />
    <DateInput bind:value={sykdomFom} label="Sykdom f.o.m." placeholder="F.eks. 2020-01-01" required />
    <DateInput bind:value={sykdomTom} label="Sykdom t.o.m." placeholder="F.eks. 2020-01-30" required />
</Form>