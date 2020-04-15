<script>
    import { Routes, route } from './store/routing';
    import Menu from './components/Menu.svelte';
    import SlettPerson from './routes/SlettPerson.svelte';
    import HentInntekt from './routes/HentInntekt.svelte';
    import HentAktørId from './routes/HentAktørid.svelte';
    import OpprettPerson from './routes/opprettPerson/OpprettPerson.svelte';
    import OpprettVedtaksperiode from './routes/opprettVedtaksperiode/OpprettVedtaksperiode.svelte';

    $: activeRoute = $route;
</script>

<div class="app">
    <Menu />
    <div class="main-content">
        <header>
            <h2>{activeRoute}</h2>
        </header>
        <div class="content">
            {#if activeRoute === Routes.SLETT_PERSON}
                <SlettPerson />
            {:else if activeRoute === Routes.OPPRETT_VEDTAKSPERIODE}
                <OpprettVedtaksperiode />
            {:else if activeRoute === Routes.HENT_INNTEKT}
                <HentInntekt />
            {:else if activeRoute === Routes.HENT_AKTØRID}
                <HentAktørId />
            {:else if activeRoute === Routes.OPPRETT_PERSON}
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
    }
</style>
