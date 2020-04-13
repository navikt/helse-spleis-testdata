<script>
    import { Status } from '../../io/http';

    export let status = null;
    export let onSubmit;
    export let submitText = 'Submit';

    const onSubmitWrapper = event => {
        event.preventDefault();
        status = Status.Sender;
        onSubmit(event)
            .then(res => {
                console.debug(res);
                status = res.status < 300 && res.status >= 200 ? Status.Suksess : Status.Error;
            })
            .catch(_ => (status = Status.Error))
            .finally(() => {
                if (status === Status.Sender) status = null;
            });
    };
</script>

<form on:submit="{onSubmitWrapper}">
    <slot />
</form>

<style>
    form {
        position: relative;
        display: flex;
        flex-direction: column;
        flex: 1;
    }
    form:not(:last-child) {
        margin-bottom: 1.5rem;
    }
</style>
