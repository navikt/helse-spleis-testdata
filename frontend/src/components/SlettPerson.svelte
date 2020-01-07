<script>
    import TextInput from './TextInput.svelte';
    import Form from './Form.svelte';

    let aktørId = '';

    const onSubmit = async () => {
        if (aktørId) {
            const result = await fetch(`/person/${aktørId}`, { method: 'delete' });
            console.log(result.status);
        }
    };
</script>

<Form onSubmit={onSubmit} submitText="Slett person">
    <TextInput bind:value={aktørId} placeholder="aktørid" label="Slett person"/>
</Form>