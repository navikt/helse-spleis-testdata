<script>
    export let value = '';
    export let disabled = false;
</script>

<input type="submit" value={value} />

<style>
    input {
        margin-top: 1rem;
        background: var(--active-color);
        color: white;
        border: none;
        border-radius: 0.5rem;
        width: max-content;
        padding: 0.5rem 1rem;
        font-size: 1rem;
    }
    input:hover,
    input:focus {
        background: var(--active-color-dark);
        box-shadow:
            0 0 0 0.125rem white,
            0 0 0 0.25rem var(--active-color-dark);
    }
    input:active {
        background: white;
        color: var(--active-color-dark);
        box-shadow: 0 0 0 0.125rem var(--active-color-dark);
    }
</style>