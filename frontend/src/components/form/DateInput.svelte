<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let value;
    export let label = '';
    export let className = '';
    export let placeholder = '';
    export let invalid = false;
    export let required = false;
</script>

<span class="date-input">
    <label for="{id}">{label}</label>
    <input {id} type="date" class:invalid class="{className}" bind:value {required} />
</span>

<style>
    .date-input {
        display: flex;
        flex-direction: column;
    }
    input {
        margin-bottom: 0.75rem;
    }
    .invalid {
        border-color: red;
        color: red;
        background: pink;
    }
</style>
