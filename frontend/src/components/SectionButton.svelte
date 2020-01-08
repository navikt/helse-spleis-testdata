<script>
    export let label;
    export let onClick;
    export let isActive = false;
</script>

<button class:active={isActive} on:click={onClick}>
    {label}
</button>

<style>
    button {
        background: white;
        color: black;
        position: relative;
        border: none;
        padding: 0.5rem;
        font-size: 1rem;
        cursor: pointer;
        margin: 0;
    }
    button.active:after,
    button:focus:after,
    button:hover:after {
        background: var(--active-color);
        position: absolute;
        content: '';
        height: 5px;
        width: 100%;
        left: 0;
        bottom: -5px;
    }
    button:hover,
    button:focus {
        outline: none;
        color: var(--active-color);
    }
    button.active {
        font-weight: bold;
    }
</style>