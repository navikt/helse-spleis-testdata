<script>
    import { Routes } from '../store/routing';
    import IdIcon from './icons/IdIcon.svelte';
    import MoneyIcon from './icons/MoneyIcon.svelte';
    import DeleteIcon from './icons/DeleteIcon.svelte';
    import CalendarIcon from './icons/CalendarIcon.svelte';
    import SectionButton from './SectionButton.svelte';
    import AddPersonIcon from './icons/AddPersonIcon.svelte';
</script>

<div class="menu">
    <h1>🧬 Spleis testdata</h1>
    <nav>
        {#if process.env.NODE_ENV === 'development'}
            <SectionButton section="{Routes.OPPRETT_PERSON}">
                <AddPersonIcon />
                Opprett person
            </SectionButton>
        {/if}
        <SectionButton section="{Routes.OPPRETT_VEDTAKSPERIODE}">
            <CalendarIcon />
            Opprett vedtaksperiode
        </SectionButton>
        <SectionButton section="{Routes.HENT_INNTEKT}">
            <MoneyIcon />
            Hent inntekt
        </SectionButton>
        <SectionButton section="{Routes.HENT_AKTØRID}">
            <IdIcon />
            Hent aktør-ID
        </SectionButton>
        <SectionButton section="{Routes.SLETT_PERSON}">
            <DeleteIcon />
            Slett person
        </SectionButton>
    </nav>
</div>

<style>
    h1 {
        font-size: 2rem;
        font-weight: 600;
        margin-bottom: 2rem;
    }
    nav {
        display: flex;
        flex-direction: column;
    }
    .menu {
        display: flex;
        flex-direction: column;
        background: #fff;
        padding: 1.5rem 2.5rem;
    }
</style>
