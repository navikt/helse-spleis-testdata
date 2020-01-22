<script>
    import SubmitButton from './SubmitButton.svelte';

    export let onSubmit;
    export let submitText = 'Submit';

    const INITIAL = "INITIAL";
    const SENDING = "SENDING";
    const OK = "OK";
    const ERROR = "ERROR";

    let status = INITIAL;

    const onSubmitWrapper = event => {
        event.preventDefault();
        status = SENDING;
        onSubmit(event)
                .then(res => {
                    console.log(res);
                    if (res.status < 300 && res.status >= 200) {
                        status = OK;
                        return res;
                    } else throw Error("Invalid status code")
                })
                .catch(_ => status = ERROR)

    }
</script>

<form on:submit={onSubmitWrapper}>
    <slot></slot>
    <SubmitButton value={submitText} disabled={status === SENDING}/>
    <p>Status: {status}</p>
</form>

<style>
    form {
        display: flex;
        flex-direction: column;
        padding: 2rem;
        flex: 1;
        max-width: 50rem;
    }
</style>