<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let className = '';
    export let label;
    export let placeholder;
    export let value;
    export let onblur = () => {};
    export let invalid = false;
    export let required = false;
    export let disabled = false;
</script>

<div class="container">
    <label for="{id}">{label}</label>
    <input
        {id}
        class:invalid
        class="{className}"
        type="text"
        bind:value
        on:blur="{onblur}"
        {required}
        {placeholder}
        {disabled}
    />
</div>

<style>
    .container {
        width: max-content;
        display: flex;
        flex-direction: column;
        margin-bottom: 0.75rem;
    }
    .invalid {
        border-color: red;
        color: red;
        background: pink;
    }
</style>
