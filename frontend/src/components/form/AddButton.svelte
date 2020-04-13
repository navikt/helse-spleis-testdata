<script>
    import { fly, fade } from 'svelte/transition';

    export let onClick = () => null;
    export let label = '';
</script>

<button
    class="add-button"
    type="button"
    on:click|preventDefault="{onClick}"
    in:fly="{{ x: -20, duration: 200, delay: 50 }}"
    out:fly="{{ x: -20, duration: 200 }}"
>
    <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
        <path d="M0 0h24v24H0V0z" fill="none"></path>
        <path
            fill="var(--font-color)"
            d="M13 7h-2v4H7v2h4v4h2v-4h4v-2h-4V7zm-1-5C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48
            10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
        ></path>
    </svg>
    <span class="label">
        {label}
        <slot />
    </span>
</button>

<style>
    button {
        display: flex;
        align-items: center;
        background: var(--background-light);
        width: max-content;
        border: none;
        border-radius: 0.5rem;
        padding: 0.5rem 0.75rem;
        outline: none;
        cursor: pointer;
    }
    button:hover,
    button:focus {
        background: var(--background);
    }
    button:active {
        background: var(--background-dark);
        box-shadow: 0 0 0 3px var(--background-dark);
    }
    .label {
        font-size: 1rem;
    }
    :global(.add-button:not(:last-child)) {
        margin-bottom: 1.5rem;
    }
    svg {
        margin-right: 0.5rem;
    }
</style>
