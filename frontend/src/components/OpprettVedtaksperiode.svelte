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
    let harAndreInntektskilder = false;
    let gjenopprett = false;
    let skalSendeInntektsmelding = true;

    const onSubmit = async () => {
        const vedtak = {
            fnr,
            inntekt,
            orgnummer,
            sykdomFom,
            sykdomTom,
            harAndreInntektskilder,
            skalSendeInntektsmelding
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
    }
</script>

<Form onSubmit={onSubmit} submitText="Opprett vedtaksperiode">
    <Input bind:value={fnr} onblur={hentInntekt} label="Fødselsnummer" placeholder="Arbeidstakers fødselsnummer"
           required/>
        <label class="switch" for=gjenopprett>Slett og gjenskap data for personen
            <input type=checkbox id=gjenopprett bind:checked={gjenopprett}/>
            <span class="slider"></span>
        </label>
    <br/>
    <Input bind:value={orgnummer} label="Organisasjonsnummer" placeholder="Arbeidsgivers organisasjonsnummer" required/>
    <br/>
    <Input bind:value={inntekt} label="Inntekt" placeholder="0" required={skalSendeInntektsmelding}
           disabled={!skalSendeInntektsmelding}/>

    <label class="switch" for=sendInntektsmelding>Send inntektsmelding
        <input type="checkbox" id=sendInntektsmelding bind:checked={skalSendeInntektsmelding}/>
    <span class="slider"></span></label>

        <label class="switch" for=inntekstkilder>Har andre inntektskilder
        <input type=checkbox id=inntekstkilder bind:checked={harAndreInntektskilder}/>
        <span class="slider"></span> </label>

    <br/>

    <DateInput bind:value={sykdomFom} label="Sykdom f.o.m." required/>
    <DateInput bind:value={sykdomTom} label="Sykdom t.o.m." required/>
</Form>
