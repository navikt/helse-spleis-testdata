<script>
    import Form from "./form/Form.svelte";
    import Input from "./form/Input.svelte";

    let inntektField;
    let aktørId = '';
    let inntekt = {};

    const onSubmit = async () => {
        const result = await fetch(`/person/inntekt/${aktørId}`, {
            method: 'get',
            headers: {"Content-Type": "application/json", "Accept": "application/json"}
        });
        inntekt = await result.json();
        return result
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
    <Input bind:value={aktørId} label="Aktør-id" placeholder="Arbeidstakers aktør-id" required />
</Form>
<p>
    {#if inntekt.beregnetMånedsinntekt}
        Beregnet månedsinntekt:
        <span on:click={copy}>{inntekt.beregnetMånedsinntekt}
            <i class="material-icons-outlined">file_copy</i>
        </span>
    {/if}
</p>

<style>
    a {
        padding-top: 1.5rem;
        padding-left: 1.5rem;
    }

    span:hover {
        cursor: pointer;
    }

    .material-icons-outlined {
        font-size: 1rem;
    }
</style>
