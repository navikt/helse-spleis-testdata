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
</script>

<label for={id}>{label}</label>
<span>
    <input
        id={id}
        type="text"
        class:invalid
        class={disabled ? "hide" : ""}
        bind:value={value}
        on:blur={onblur}
        required={required}
        placeholder={placeholder}
        disabled={disabled}
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
