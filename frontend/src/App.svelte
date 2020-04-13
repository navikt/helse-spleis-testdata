<script>
    import SlettPerson from './routes/SlettPerson.svelte';
    import OpprettVedtaksperiode from './routes/OpprettVedtaksperiode.svelte';
    import SectionButton from './components/SectionButton.svelte';
    import HentInntekt from './routes/HentInntekt.svelte';
    import HentAktørId from './routes/HentAktørid.svelte';
    import OpprettPerson from './routes/OpprettPerson.svelte';
    import DeleteIcon from './components/icons/DeleteIcon.svelte';
    import MoneyIcon from './components/icons/MoneyIcon.svelte';
    import IdIcon from './components/icons/IdIcon.svelte';
    import CalendarIcon from './components/icons/CalendarIcon.svelte';
    import AddPersonIcon from './components/icons/AddPersonIcon.svelte';

    const Section = {
        SLETT_PERSON: 'Slett person',
        OPPRETT_VEDTAKSPERIODE: 'Opprett vedtaksperiode',
        HENT_INNTEKT: 'Hent inntekt',
        HENT_AKTØRID: 'Hent aktør-ID',
        OPPRETT_PERSON: 'Opprett person'
    };

    let activeSection = Section.OPPRETT_VEDTAKSPERIODE;
</script>

<div class="app">
    <div class="main-menu">
        <h1>Spleis testdata</h1>
        <nav>
            {#if process.env.NODE_ENV === 'development'}
                <SectionButton
                    isActive="{activeSection === Section.OPPRETT_PERSON}"
                    onClick="{() => (activeSection = Section.OPPRETT_PERSON)}"
                >
                    <AddPersonIcon />
                    Opprett person
                </SectionButton>
            {/if}
            <SectionButton
                isActive="{activeSection === Section.OPPRETT_VEDTAKSPERIODE}"
                onClick="{() => (activeSection = Section.OPPRETT_VEDTAKSPERIODE)}"
            >
                <CalendarIcon />
                Opprett vedtaksperiode
            </SectionButton>
            <SectionButton
                isActive="{activeSection === Section.HENT_INNTEKT}"
                onClick="{() => (activeSection = Section.HENT_INNTEKT)}"
            >
                <MoneyIcon />
                Hent inntekt
            </SectionButton>
            <SectionButton
                isActive="{activeSection === Section.HENT_AKTØRID}"
                onClick="{() => (activeSection = Section.HENT_AKTØRID)}"
            >
                <IdIcon />
                Hent aktør-ID
            </SectionButton>
            <SectionButton
                isActive="{activeSection === Section.SLETT_PERSON}"
                onClick="{() => (activeSection = Section.SLETT_PERSON)}"
            >
                <DeleteIcon />
                Slett person
            </SectionButton>
        </nav>
    </div>
    <div class="main-content">
        <header>
            <h2>{activeSection}</h2>
        </header>
        <div class="content">
            {#if activeSection === Section.SLETT_PERSON}
                <SlettPerson />
            {:else if activeSection === Section.OPPRETT_VEDTAKSPERIODE}
                <OpprettVedtaksperiode />
            {:else if activeSection === Section.HENT_INNTEKT}
                <HentInntekt />
            {:else if activeSection === Section.HENT_AKTØRID}
                <HentAktørId />
            {:else if activeSection === Section.OPPRETT_PERSON}
                <OpprettPerson />
            {/if}
        </div>
    </div>
</div>

<style>
    .app {
        display: flex;
        min-height: 100vh;
        width: 100vw;
        position: relative;
        height: max-content;
        flex: 1;
    }
    .main-menu {
        display: flex;
        flex-direction: column;
        background: #fff;
        padding: 1.5rem 2.5rem;
    }
    .main-content {
        position: relative;
        flex: 1;
        padding: 1rem 2.5rem;
        background: var(--background-light);
        overflow-y: scroll;
    }
    .content {
        position: relative;
        display: flex;
    }
    header {
        display: flex;
        align-items: center;
        height: 6.5rem;
        margin-bottom: 1rem;
    }
    h1 {
        font-size: 2rem;
        font-weight: 600;
        margin-bottom: 2rem;
    }
    nav {
        display: flex;
        flex-direction: column;
    }
</style>
