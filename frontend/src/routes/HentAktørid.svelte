<script>
    import Form from '../components/form/Form.svelte';
    import Input from '../components/form/Input.svelte';
    import Clipboard from '../components/clipboard/Clipboard.svelte';
    import { getAktørId } from '../io/http';
    import Card from '../components/Card.svelte';
    import SubmitButton from '../components/form/SubmitButton.svelte';
    import ContentColumn from '../components/ContentColumn.svelte';

    let fnr = '';
    let aktørId = undefined;
    let status;

    const onSubmit = async () => {
        return getAktørId({ fnr }).then(async res => {
            aktørId = await res.text();
            return Promise.resolve(res);
        });
    };
</script>

<ContentColumn>
    <Card>
        <Form {onSubmit} bind:status>
            <Input bind:value="{fnr}" placeholder="Arbeidstakers fødselsnummer" required />
            <SubmitButton value="Hent aktør-ID" {status} />
        </Form>
    </Card>
    <Card>
        <label>Aktør-ID</label>
        <Clipboard value="{aktørId}" />
    </Card>
</ContentColumn>
