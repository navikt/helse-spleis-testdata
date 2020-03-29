<script>
    import Form from './form/Form.svelte';
    import Input from './form/Input.svelte';
    import { getInntekt } from '../io/http';
    import Clipboard from './clipboard/Clipboard.svelte';

    let inntektField;
    let fnr = '';
    let inntekt = {};

    const onSubmit = async () => {
        return getInntekt({ fnr }).then(async res => {
            inntekt = await res.json();
            return Promise.resolve(res);
        });
    };
</script>

<Form {onSubmit} submitText="Hent inntekt">
    <Input
        bind:value="{fnr}"
        label="Fødselsnummer"
        placeholder="Arbeidstakers fødselsnummer"
        required
    />
</Form>
<Clipboard value="{inntekt.beregnetMånedsinntekt}" />

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
