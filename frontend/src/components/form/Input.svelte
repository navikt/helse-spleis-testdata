<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let label;
    export let placeholder;
    export let value;
    export let invalid = false;
    export let required = false;
</script>

<label for={id}>{label}</label>
<span>
    <input
        id={id}
        type="text"
        class:invalid
        bind:value={value}
        required={required}
        placeholder={placeholder}
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