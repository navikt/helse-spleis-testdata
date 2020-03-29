<script>
    import Form from './form/Form.svelte';
    import Input from './form/Input.svelte';
    import Clipboard from './clipboard/Clipboard.svelte';
    import { getAktørId } from '../io/http';

    let fnr = '';
    let aktørId = undefined;

    const onSubmit = async () => {
        return getAktørId({ fnr }).then(async res => {
            aktørId = await res.text();
            return Promise.resolve(res);
        });
    };
</script>

<Form {onSubmit} submitText="Hent aktørId">
    <Input
        bind:value="{fnr}"
        label="Fødselsnummer"
        placeholder="Arbeidstakers fødselsnummer"
        required
    />
</Form>
<Clipboard value="{aktørId}" />

<style>
    a {
        padding-top: 1.5rem;
        padding-left: 1.5rem;
    }
    p {
        margin-left: 2rem;
    }
    span:hover {
        cursor: pointer;
    }
    .material-icons-outlined {
        font-size: 1rem;
    }
</style>
