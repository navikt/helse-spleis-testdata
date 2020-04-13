<script>
    import Spinner from '../Spinner.svelte';
    import { Status } from '../../io/http';

    export let status;
    export let value = '';
    export let className = '';

    let disabled;

    $: disabled = status === Status.Sender;
</script>

<div class="{`flex ${className}`}">
    <div class="wrapper">
        <input type="submit" {value} {disabled} />
        {#if disabled}
            <Spinner />
        {/if}
    </div>
    {#if status === Status.Error}
        <p class="status error">Noe gikk galt 💔</p>
    {/if}
    {#if status === Status.Suksess}
        <p class="status suksess">Huge success! 🎉</p>
    {/if}
</div>

<style>
    input {
        background: var(--active-color);
        color: white;
        border: none;
        border-radius: 0.5rem;
        width: max-content;
        padding: 0.5rem 2.5rem;
        font-size: 1rem;
        transition: all 0.1s ease;
    }
    input:hover,
    input:focus {
        background: var(--active-color-dark);
        box-shadow: 0 0 0 0.125rem white, 0 0 0 0.25rem var(--active-color-dark);
    }
    input:active {
        background: white;
        color: var(--active-color-dark);
        box-shadow: 0 0 0 0.125rem var(--active-color-dark);
    }
    input:disabled {
        background: var(--disabled-color);
        box-shadow: none;
    }
    .wrapper {
        display: flex;
        align-items: center;
        width: max-content;
        height: max-content;
    }
    p.status {
        margin: 0 0 0 1rem;
    }
    p.error {
        color: red;
    }
    p.suksess {
        color: var(--active-color);
    }
    .flex {
        display: flex;
        align-items: center;
        margin-top: 1rem;
    }
</style>
