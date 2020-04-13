<script>
    export let onClick;
    export let isActive = false;
</script>

<button class="section-button" class:active="{isActive}" on:click="{onClick}">
    <slot />
</button>

<style>
    button {
        display: flex;
        align-items: center;
        color: #3e3832;
        background: none;
        padding: 0.75rem 1rem;
        text-align: left;
        font-size: 1rem;
        font-weight: 600;
        border: none;
        border-radius: 0.5rem;
        margin: 0.25rem 0;
        cursor: pointer;
        outline: none;
        transition: background 0.1s ease;
    }
    :global(.section-button > *) {
        margin-right: 0.5rem;
    }
    button:hover,
    button:focus {
        background: var(--background-light);
    }
    button.active,
    button:active {
        background: #bce8de;
    }
</style>
