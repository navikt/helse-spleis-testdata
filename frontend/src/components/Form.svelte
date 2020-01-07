<script>
    import SubmitButton from './SubmitButton.svelte';
    export let onSubmit;
    export let submitText = 'Submit';

    const onSubmitWrapper = event => {
        event.preventDefault();
        onSubmit(event);
    }
</script>

<form on:submit={onSubmitWrapper}>
    <slot></slot>
    <SubmitButton value={submitText} />
</form>

<style>
    form {
        display: flex;
        flex-direction: column;
        padding: 2rem;
    }
</style>