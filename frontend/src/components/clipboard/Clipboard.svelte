<script>
    import { copyContentsToClipboard } from './copy';

    export let value = '';

    $: disabled = value.length === 0;

    let valueContainer;
    let copied = false;

    const copy = () => {
        copied = false;
        copied = copyContentsToClipboard(valueContainer);
    };
</script>

<div class="container">
    <div class="value" bind:this="{valueContainer}">
        {value}
        {#if copied}
            <i class="material-icons done">done</i>
        {/if}
    </div>
    <button on:click="{copy}" {disabled}>
        <i class="material-icons-outlined">file_copy</i>
    </button>
</div>

<style>
    .container {
        display: flex;
        align-items: center;
        background: #ededed;
        height: 2rem;
        width: 20rem;
        margin: 0 2rem;
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
        color: white;
    }
    button:active i {
        color: var(--active-color-dark);
    }
    button[disabled] i {
        color: white;
    }
    label.månedsinntekt {
        color: #3e3832;
        font-weight: 600;
        margin: 0.5rem 2rem;
    }
    i.done {
        color: var(--active-color-dark);
    }
</style>
