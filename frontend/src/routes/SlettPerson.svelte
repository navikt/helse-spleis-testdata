<script>
    import Input from '../components/form/Input.svelte';
    import Form from '../components/form/Form.svelte';
    import { deletePerson, Status } from '../io/http';
    import Card from '../components/Card.svelte';
    import SubmitButton from '../components/form/SubmitButton.svelte';
    import ContentColumn from '../components/ContentColumn.svelte';

    let fnr = '';

    let status;

    const onSubmit = async () => {
        return await deletePerson({ fnr });
    };
</script>

<ContentColumn>
    <Card>
        <Form {onSubmit} bind:status>
            <Input bind:value="{fnr}" placeholder="Fødselsnummer" required />
            <SubmitButton value="Slett person" {status} />
        </Form>
    </Card>
</ContentColumn>
