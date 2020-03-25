<script>
  import Form from './form/Form.svelte';
  import Input from './form/Input.svelte';

  let fnr = '';
  let aktørId = undefined;

  const onSubmit = async () => {
    const result = await fetch(`/person/aktorid`, {
      method: 'get',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        ident: fnr
      }
    });
    aktørId = await result.text();
    return result;
  };
</script>

<Form {onSubmit} submitText="Hent aktørId">
  <Input
    bind:value="{fnr}"
    label="Fnr"
    placeholder="Arbeidstakers fnr"
    required
  />
</Form>
<p>
  {#if aktørId}AktørId: {aktørId}{/if}
</p>

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
