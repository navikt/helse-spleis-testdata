<script>
    import Modal from '../Modal.svelte';

    export let onClose = () => null;
    export let onAccept = () => null;
    export let className = '';

    let inputValue = '';

    const akseptstring = 'Jeg aksepterer risken';
</script>

<Modal className="shake" {onClose}>
    <div class="modalinnhold">
        <h1>🚨🚨🚨🚨🚨</h1>
        <p>
            Er du sikker på at du takler maksimal mortness? Skriv
            <code>{akseptstring}</code>
            i feltet under for å godta.
        </p>
        <input bind:value="{inputValue}" type="text" />
        <span class="knapper">
            <button
                disabled="{inputValue.toLowerCase() !== akseptstring.toLowerCase()}"
                on:click="{onAccept}"
            >
                Ja
            </button>
            <button on:click="{onClose}" class="flat">Nei</button>
        </span>
    </div>
</Modal>

<style>
    h1 {
        padding: 0;
        margin: 0 0 1rem 0;
    }
    code {
        background: #eee;
    }
    p {
        margin: 0 0 1rem 0;
    }
    .modalinnhold {
        display: flex;
        flex-direction: column;
        max-width: 20rem;
    }
    .knapper button:not(:last-child) {
        margin-right: 0.5rem;
    }
    input {
        margin-bottom: 1rem;
    }
    button {
        background: var(--active-color);
        color: white;
        border: none;
        border-radius: 0.5rem;
        width: max-content;
        padding: 0.5rem 2.5rem;
        font-size: 1rem;
        outline: none;
    }
    button:hover,
    button:focus {
        background: var(--active-color-dark);
        box-shadow: 0 0 0 0.125rem white, 0 0 0 0.25rem var(--active-color-dark);
    }
    button:active {
        background: white;
        color: var(--active-color-dark);
        box-shadow: 0 0 0 0.125rem var(--active-color-dark);
    }
    button:disabled {
        background: var(--disabled-color);
        box-shadow: none;
    }
    .wrapper {
        display: flex;
        align-items: center;
        width: max-content;
        height: max-content;
    }
    button.flat {
        background: #fff;
        color: var(--active-color);
    }
</style>
