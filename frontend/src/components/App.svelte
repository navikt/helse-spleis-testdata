<script>
    import SlettPerson from './SlettPerson.svelte';
    import OpprettVedtaksperiode from './OpprettVedtaksperiode.svelte';
    import SectionButton from './SectionButton.svelte';
    import HentInntekt from "./HentInntekt.svelte";

    const Section = {
        SLETT_PERSON: 'slettperson',
        OPPRETT_VEDTAKSPERIODE: 'opprettvedtaksperiode',
        HENT_INNTEKT: "hentinntekt"
    };

    let activeSection = Section.SLETT_PERSON;
</script>

<header>
    <h1>spleis testdata</h1>
    <SectionButton
        label="slett person"
        isActive={activeSection === Section.SLETT_PERSON}
        onClick={() => activeSection = Section.SLETT_PERSON}
    />
    <SectionButton
        label="opprett vedtaksperiode"
        isActive={activeSection === Section.OPPRETT_VEDTAKSPERIODE}
        onClick={() => activeSection = Section.OPPRETT_VEDTAKSPERIODE}
    />
    <SectionButton
        label="Hent inntekt"
        isActive={activeSection === Section.HENT_INNTEKT}
        onClick={() => activeSection = Section.HENT_INNTEKT}
    />
</header>
<hr>
{#if activeSection === Section.SLETT_PERSON}
    <SlettPerson />
{:else if activeSection === Section.OPPRETT_VEDTAKSPERIODE}
    <OpprettVedtaksperiode />
{:else if activeSection === Section.HENT_INNTEKT}
    <HentInntekt />
{/if}

<style>
    header {
        padding: 0 1.5rem;
    }

    h1 {
        margin: 1.5rem 0;
        font-size: 2rem;
        font-weight: normal;
    }

    hr {
        border: none;
        border-top: 5px solid #dad6d2;
        margin: 0;
    }
</style>
