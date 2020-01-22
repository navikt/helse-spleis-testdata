<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';

    let aktørId = '';

    const onSubmit = async () => {
        return await fetch(`/person/${aktørId}`, { method: 'delete' });
    };
</script>

<Form onSubmit={onSubmit} submitText="Slett person">
    <Input bind:value={aktørId} placeholder="Arbeidstakers aktør-id" label="Aktør-id" required />
</Form>