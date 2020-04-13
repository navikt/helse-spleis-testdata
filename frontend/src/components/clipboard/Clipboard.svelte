<script>
    import { copyContentsToClipboard } from './copy';
    import CopyIcon from '../icons/CopyIcon.svelte';
    import CheckIcon from '../icons/CheckIcon.svelte';

    export let value = '';

    $: disabled = value.length === 0;

    let valueContainer;
    let copied = false;

    const copy = () => {
        copied = false;
        copied = copyContentsToClipboard(valueContainer);
    };
</script>

<div class="clipboard">
    <div class="value" bind:this="{valueContainer}">{value}</div>
    <button on:click="{copy}" {disabled}>
        {#if copied}
            <CheckIcon />
        {:else}
            <CopyIcon height="20" width="20" />
        {/if}
    </button>
</div>

<style>
    .clipboard {
        display: flex;
        align-items: center;
        background: #ededed;
        height: 2rem;
        width: 20rem;
        border-radius: 0.25rem;
    }
    .value {
        flex: 1;
        outline: none;
        padding: 0 0.5rem;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    button {
        display: flex;
        align-items: center;
        outline: none;
        background: var(--active-color);
        height: 2rem;
        width: 2rem;
        border-radius: 0 0.25rem 0.25rem 0;
        border: none;
        cursor: pointer;
    }
    button:hover,
    button:focus {
        background: var(--active-color-dark);
    }
    button:active {
        background: white;
        box-shadow: inset 0 0 0 2px var(--active-color-dark);
    }
    button[disabled] {
        background: #c2c2c2;
        box-shadow: none;
    }
    i {
        font-size: 1rem;
        color: #fff;
    }
    button:active :global(svg > path:last-of-type) {
        fill: var(--active-color-dark);
    }
    button[disabled] i {
        color: white;
    }
    label.månedsinntekt {
        color: #3e3832;
        font-weight: 600;
        margin: 0.5rem 2rem;
    }
    :global(.clipboard svg) {
        margin: 0;
    }
    :global(.clipboard svg > path:last-of-type) {
        fill: #fff;
    }
</style>
