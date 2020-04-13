<script>
    import Form from '../components/form/Form.svelte';
    import Input from '../components/form/Input.svelte';
    import { getInntekt } from '../io/http';
    import Clipboard from '../components/clipboard/Clipboard.svelte';
    import Card from '../components/Card.svelte';
    import ContentColumn from '../components/ContentColumn.svelte';
    import SubmitButton from '../components/form/SubmitButton.svelte';

    let inntektField;
    let fnr = '';
    let inntekt = {};

    let status;

    const onSubmit = async () => {
        return getInntekt({ fnr }).then(async res => {
            inntekt = await res.json();
            return Promise.resolve(res);
        });
    };
</script>

<ContentColumn>
    <Card>
        <Form {onSubmit} bind:status>
            <Input
                bind:value="{fnr}"
                placeholder="Arbeidstakers fødselsnummer"
                required
            />
            <SubmitButton value="Hent inntekt" {status} />
        </Form>
    </Card>
    <Card>
        <label>Inntekt</label>
        <Clipboard value="{inntekt.beregnetMånedsinntekt}" />
    </Card>
</ContentColumn>

<style>
    button {
        outline: none;
        background: none;
        border: none;
    }
    button:hover {
        cursor: pointer;
    }
    .material-icons-outlined {
        font-size: 1rem;
    }
    label.månedsinntekt {
        color: #3e3832;
        font-weight: 600;
        margin: 0.5rem 2rem;
    }
    div.månedsinntekt {
        display: flex;
        align-items: center;
        background: #ededed;
        height: 2rem;
        width: 20rem;
        margin: 0 2rem;
        border-radius: 0.25rem;
        border: 1px #c2c2c2 dashed;
    }
</style>
