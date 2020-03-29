<script>
    import SubmitButton from './SubmitButton.svelte';

    const Status = {
        Suksess: 'suksess',
        Sender: 'sender',
        Error: 'error'
    };

    export let onSubmit;
    export let submitText = 'Submit';

    let status = null;

    const onSubmitWrapper = event => {
        event.preventDefault();
        status = Status.Sender;
        onSubmit(event)
            .then(res => {
                console.debug(res);
                status = res.status < 300 && res.status >= 200 ? Status.Suksess : Status.Error;
            })
            .finally(() => {
                if (status === Status.Sender) status = null;
            });
    };
</script>

<form on:submit="{onSubmitWrapper}">
    <slot />
    <div class="flex">
        <SubmitButton value="{submitText}" disabled="{status === Status.Sender}" />
        {#if status === Status.Error}
            <p class="status error">Noe gikk galt 😢💔</p>
        {/if}
        {#if status === Status.Suksess}
            <p class="status suksess">Great success! 🥳🎉</p>
        {/if}
    </div>
</form>

<style>
    form {
        display: flex;
        flex-direction: column;
        padding: 1.5rem 2rem;
        flex: 1;
        max-width: 50rem;
    }
    p.status {
        margin: 0 0 0 1rem;
    }
    p.error {
        color: red;
    }
    p.suksess {
        color: var(--active-color);
    }
    .flex {
        display: flex;
        align-items: center;
        margin-top: 1rem;
    }
</style>
