<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';

    let fnr = '';

    const onSubmit = async () => {
        return await fetch(`/person`, { method: 'delete' });
    };
</script>

<Form onSubmit={onSubmit} submitText="Slett person">
    <Input bind:value={fnr} placeholder="Arbeidstakers fnr" label="fødselsnummer" required />
</Form>