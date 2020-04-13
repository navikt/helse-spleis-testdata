<script>
    import { fly } from 'svelte/transition';
    import EditIcon from '../icons/EditIcon.svelte';

    export let onClick = () => null;
    export let size = 24;
</script>

<button
    class="edit-button"
    type="button"
    on:click|preventDefault="{onClick}"
    in:fly="{{ x: 20, duration: 200 }}"
    out:fly="{{ x: 20, duration: 200 }}"
    style="--size: {size}px"
>
    <EditIcon {size} />
</button>

<style>
    .edit-button {
        background: none;
        border: none;
        outline: none;
        font-size: unset;
        background: var(--background);
        border-radius: 50%;
        width: calc(var(--size) + 1rem);
        height: calc(var(--size) + 1rem);
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.1s ease;
    }
    .edit-button:hover,
    .edit-button:focus {
        background: var(--font-color);
    }
    .edit-button:active {
        box-shadow: 0 0 0 3px var(--font-color);
    }
    :global(.edit-button:hover path:first-child, .edit-button:focus path:first-child) {
        fill: #fff;
    }
</style>
