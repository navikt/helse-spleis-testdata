<script>
    import { fly } from 'svelte/transition';
    import Card from '../../components/Card.svelte';
    import DateInput from '../../components/form/DateInput.svelte';
    import EditButton from '../../components/form/EditButton.svelte';
    import DeleteButton from '../../components/form/DeleteButton.svelte';
    import Toggle from '../../components/form/Toggle.svelte';
    import FormGroup from '../../components/form/FormGroup.svelte';
    import NumberInput from '../../components/form/NumberInput.svelte';

    export let senderSykmelding = true;
    export let senderSøknad = true;
    export let senderInntektsmelding = false;
    export let isOpen = false;

    export let førsteFraværsdag = '2020-01-01';
    export let sykdomFom = '2020-01-01';
    export let sykdomTom = '2020-01-31';

    export let sendtNav = '2020-02-01';
    export let sendtArbeidsgiver = null;
    export let sykmeldingsgrad = 100;
    export let faktiskgrad = null;

    export let onRemove = () => null;

    const toggleShowDetail = event => {
        event.stopPropagation();
        isOpen = !isOpen;
    };
</script>

<span class="vedtaksperiode-container" style="--sykmeldingsgrad: {sykmeldingsgrad}">
    <Card>
        <div class="flex">
            <p>{sykmeldingsgrad}%</p>
            <p>{sykdomFom} - {sykdomTom}</p>
            <div class="dokumenter">
                <div class="dokument sm" class:inactive="{!senderSykmelding}">sm</div>
                <div class="dokument sø" class:inactive="{!senderSøknad}">sø</div>
                <div class="dokument im" class:inactive="{!senderInntektsmelding}">im</div>
            </div>
        </div>
        {#if isOpen}
            <FormGroup>
                <Toggle label="Send sykmelding" bind:checked="{senderSykmelding}" />
                <Toggle label="Send søknad" bind:checked="{senderSøknad}" />
                <Toggle label="Send inntektsmelding" bind:checked="{senderInntektsmelding}" />
            </FormGroup>

            <hr />
            <NumberInput
                bind:value="{sykmeldingsgrad}"
                placeholder="Sykdomsgrad"
                required
                min="0"
                max="100"
            />
            <DateInput bind:value="{sykdomFom}" label="Sykdom f.o.m." required />
            <DateInput bind:value="{sykdomTom}" label="Sykdom t.o.m." required />
            <DateInput bind:value="{førsteFraværsdag}" label="Første fraværsdag" />

            {#if senderSøknad}
                <hr />
                <DateInput bind:value="{sendtNav}" label="Søknad sendt NAV" />
                <DateInput bind:value="{sendtArbeidsgiver}" label="Søknad sendt arbeidsgiver" />

                <NumberInput
                    bind:value="{faktiskgrad}"
                    placeholder="Faktisk arbeidsgrad i søknad"
                    min
                    max
                />
            {/if}
        {/if}
    </Card>
    <span class="buttons">
        <EditButton size="20" onClick="{toggleShowDetail}" />
        <DeleteButton size="20" onClick="{onRemove}" />
    </span>
</span>

<style>
    .buttons {
        display: flex;
        margin-top: 1.125rem;
        margin-left: 1rem;
    }
    :global(.buttons > button:not(:last-of-type)) {
        margin-right: 0.5rem;
    }
    :global(.vedtaksperiode-container > .card > .form-group) {
        margin-top: 1rem;
    }
    .vedtaksperiode-container {
        display: flex;
        position: relative;
    }
    :global(.vedtaksperiode-container .card) {
        margin: 0.5rem 0;
    }
    p {
        font-weight: 600;
        margin: 0;
    }
    .flex {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .flex > *:not(:last-child) {
        margin-right: 1rem;
    }
    .dokumenter {
        display: flex;
    }
    .dokument {
        height: 1.425rem;
        width: 2rem;
        display: flex;
        justify-content: center;
        font-weight: 600;
        border-radius: 1rem;
        margin-left: 0.5rem;
        transition: all 0.1s ease;
    }
    .sm {
        box-shadow: 0 0 0 2px #2f00ff;
        color: #2f00ff;
    }
    .sø {
        box-shadow: 0 0 0 2px #b400ff;
        color: #b400ff;
    }
    .im {
        box-shadow: 0 0 0 2px #ff008c;
        color: #ff008c;
    }
    .dokument.inactive {
        box-shadow: 0 0 0 2px var(--background-light);
        color: var(--background-light);
    }
    hr {
        margin: 2rem -1rem 1.5rem;
        border: none;
        height: 1px;
        background: var(--background-light);
    }
</style>
