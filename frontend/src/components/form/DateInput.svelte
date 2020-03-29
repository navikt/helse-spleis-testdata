<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let label = '';
    export let value;
    export let placeholder = '';
    export let invalid = false;
    export let required = false;
</script>

<label for="{id}">{label}</label>
<input {id} type="date" class:invalid bind:value {required} />

<style>
    input {
        margin-bottom: 0.75rem;
    }
    .invalid {
        border-color: red;
        color: red;
        background: pink;
    }
</style>
