<script>
    import Input from './form/Input.svelte';
    import Form from './form/Form.svelte';
    import { deletePerson } from '../io/http';

    let fnr = '';

    const onSubmit = async () => {
        return await deletePerson({ fnr });
    };
</script>

<Form {onSubmit} submitText="Slett person">
    <Input
        bind:value="{fnr}"
        placeholder="Arbeidstakers fødselsnummer"
        label="Fødselsnummer"
        required
    />
</Form>
