<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let label;
    export let placeholder;
    export let value;
    export let onblur = () => {};
    export let invalid = false;
    export let required = false;
    export let disabled = false;
    export let max = 100;
    export let min = 0;
</script>

<span>
    <label for="{id}">{label}</label>
    <input
        {id}
        type="number"
        class:invalid
        class="{disabled ? 'hide' : ''}"
        bind:value
        on:blur="{onblur}"
        {required}
        {placeholder}
        {disabled}
        {max}
        {min}
    />
    {#if invalid}
        <p>Ugyldig verdi</p>
    {/if}
</span>

<style>
    span {
        width: max-content;
        display: flex;
        flex-direction: column;
    }
    p {
        color: red;
        margin-left: 1rem;
    }
    .invalid {
        border-color: red;
    }
    input {
        width: 10rem;
        margin-bottom: 0.75rem;
    }
</style>
