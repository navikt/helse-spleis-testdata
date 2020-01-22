<script>
    import Input from './Input.svelte';

    export let label = '';
    export let placeholder = '';
    export let value = '';
    export let invalid = false;
    export let required = false;
</script>

<Input
    value={value}
    label={label}
    invalid={invalid}
    required={required}
    placeholder={placeholder}
/>