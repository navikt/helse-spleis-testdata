<script>
    import TextInput from './TextInput.svelte';
    import Form from './Form.svelte';

    let aktørId = '';
    let invalid = false;

    const onSubmit = async () => {
        if (aktørId) {
            invalid = false;
            return await fetch(`/person/${aktørId}`, { method: 'delete' });
        } else {
            invalid = true;
            throw Error()
        }
    };
</script>

<Form onSubmit={onSubmit} submitText="Slett person">
    <TextInput bind:value={aktørId} placeholder="Arbeidstakers aktør-id" label="Aktør-id" invalid={invalid} />
</Form>