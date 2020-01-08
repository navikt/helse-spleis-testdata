<script>
    import SlettPerson from './SlettPerson.svelte';
    import OpprettVedtaksperiode from './OpprettVedtaksperiode.svelte';
    import SectionButton from './SectionButton.svelte';

    const Section = {
        SLETT_PERSON: 'slettperson',
        OPPRETT_VEDTAKSPERIODE: 'opprettvedtaksperiode'
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
</header>
<hr>
{#if activeSection === Section.SLETT_PERSON}
    <SlettPerson />
{:else if activeSection === Section.OPPRETT_VEDTAKSPERIODE}
    <OpprettVedtaksperiode />
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