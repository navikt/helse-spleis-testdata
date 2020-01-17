<script>
    import TextInput from './TextInput.svelte';
    import Form from './Form.svelte';

    let invalid = false;
    let aktørId = '';
    let fnr = '';
    let orgnummer = '';
    let sykdomFom = '';
    let sykdomTom = '';

    const onSubmit = async () => {
        const vedtak = { aktørId, fnr, orgnummer, sykdomFom, sykdomTom };

        if (aktørId) {
            invalid = false;
            const result = await fetch(`/vedtaksperiode/`, {
                method: 'post',
                body: JSON.stringify(vedtak),
                headers: { "Content-Type": "application/json" }
            });
            return result;
        } else {
            invalid = true;
            throw Error()
        }
    };
</script>

<Form onSubmit={onSubmit} submitText="Opprett vedtaksperiode">
    <TextInput bind:value={aktørId} label="Aktør-id" placeholder="Arbeidstakers aktør-id" invalid={invalid} />
    <TextInput bind:value={fnr} label="Fødselsnummer" placeholder="Arbeidstakers fødselsnummer" />
    <TextInput bind:value={orgnummer} label="Organisasjonsnummer" placeholder="Arbeidsgivers organisasjonsnummer" />
    <TextInput bind:value={sykdomFom} label="Sykdom f.o.m." placeholder="F.eks. 2020-01-01" />
    <TextInput bind:value={sykdomTom} label="Sykdom t.o.m." placeholder="F.eks. 2020-01-30" />
</Form>