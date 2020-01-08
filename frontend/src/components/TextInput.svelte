<script>
    import { uuid } from '../scripts/uuid';
    const id = uuid();

    export let label = '';
    export let placeholder = '';
    export let value = '';
</script>

<label for={id}>{label}</label>
<input bind:value={value} placeholder={placeholder} id={id}>

<style>
    label {
        display: block;
        line-height: 1.4;
        margin-bottom: 0.5rem;
        font-size: 1rem;
    }
    input {
        margin: 0.25rem 0 1rem 0;
        max-width: 20rem;
        appearance: none;
        padding: 0.5rem;
        background-color: #FFF;
        border-radius: 4px;
        border: 1px solid rgb(120, 112, 106);
        box-sizing: border-box;
        line-height: 1.375rem;
    }
    input:hover {
        border-color: #0067C5;
        transition: border-color 200ms cubic-bezier(0.465, 0.183, 0.153, 0.946);
    }
    input:focus {
        outline: 0;
        box-shadow: 0 0 0 3px #254b6d;
    }
</style>