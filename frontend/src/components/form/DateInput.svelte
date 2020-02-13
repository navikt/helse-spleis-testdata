<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let label = '';
    export let value;
    export let placeholder = '';
    export let invalid = false;
    export let required = false
</script>

<label for={id}>{label}</label>
<span>
    <input
        id={id}
        type="date"
        class:invalid
        bind:value={value}
        required={required}
    />
    {#if invalid}
        <p>Ugyldig verdi</p>
    {/if}
</span>

<style>
    span {
        width: max-content;
        display: flex;
    }
    p {
        color: red;
        margin-left: 1rem;
    }
    .invalid { border-color: red; }
</style>
