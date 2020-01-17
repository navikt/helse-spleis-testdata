<script>
    import Form from "./Form.svelte";
    import TextInput from "./TextInput.svelte";

    let invalid = false;
    let inntektField;
    let aktørId = '';
    let inntekt = {};

    const onSubmit = async () => {
        if (aktørId) {
            invalid = false;
            const result = await fetch(`/person/inntekt/${aktørId}`, {
                method: 'get',
                headers: {"Content-Type": "application/json", "Accept": "application/json"}
            });
            inntekt = await result.json();
            return result
        } else {
            invalid = true;
            throw Error()
        }
    };

    const copy = () => {
        let clipboardTextarea = document.createElement("textarea");
        document.body.appendChild(clipboardTextarea);
        clipboardTextarea.value = inntekt.beregnetMånedsinntekt;
        clipboardTextarea.select();
        document.execCommand("copy");
        clipboardTextarea.remove();
    };
</script>
<Form onSubmit={onSubmit} submitText="Hent inntekt">
    <TextInput bind:value={aktørId} label="Aktør-id" placeholder="Arbeidstakers aktør-id" invalid={invalid} />
</Form>
<p class="response">
    {#if inntekt.beregnetMånedsinntekt}
        Beregnet månedsinntekt:
        <span class="response__result" on:click={copy}>{inntekt.beregnetMånedsinntekt} <i
            class="material-icons-outlined">file_copy</i></span>
    {/if}
</p>

<style>
    .response {
        padding-top: 1.5rem;
        padding-left: 1.5rem;
    }

    .response__result:hover {
        cursor: pointer;
    }

    .material-icons-outlined {
        font-size: 1rem;
    }
</style>
