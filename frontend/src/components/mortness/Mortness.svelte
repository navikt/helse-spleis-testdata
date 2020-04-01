<script>
    import Modal from '../Modal.svelte';
    import MortnessModal from './MortnessModal.svelte';

    const mortnessScale = ['Mort', 'Mortere', 'Enda mortere', 'Mortest'];

    let currentMortness = 0;

    let showModal = false;

    $: classForMortness = mortness =>
        currentMortness > mortness
            ? 'lessThan'
            : currentMortness < mortness
            ? 'greaterThan'
            : 'equal';
</script>

<div class="mortness">
    <hr />
    <button
        type="button"
        on:click="{() => (currentMortness = 0)}"
        class="mort {classForMortness(0)}"
    >
        {mortnessScale[0]}
    </button>
    <button
        type="button"
        on:click="{() => (currentMortness = 1)}"
        class="mortere {classForMortness(1)}"
    >
        {mortnessScale[1]}
    </button>
    <button
        type="button"
        on:click="{() => (currentMortness = 2)}"
        class="enda-mortere {classForMortness(2)}"
    >
        {mortnessScale[2]}
    </button>
    <button
        type="button"
        on:click="{() => showModal = true}"
        class="mortest {classForMortness(3)}"
    >
        {mortnessScale[3]}
    </button>
</div>

{#if showModal}
    <MortnessModal
        className="shake"
        onAccept="{() => {
            currentMortness = 3;
            showModal = false;
        }}"
        onClose="{() => showModal = false}"
    />
{/if}

<style>
    .mortness {
        position: relative;
        display: flex;
        justify-content: space-between;
        width: max-content;
        min-width: 32rem;
    }
    hr {
        position: absolute;
        border: none;
        border-top: 1px solid #78706a;
        width: calc(100% - 2.5rem);
        margin: 4px 1rem;
    }
    button {
        display: flex;
        flex-direction: column;
        align-items: center;
        border: none;
        background: none;
        padding: 0;
        color: #0067c5;
        text-decoration: underline;
        font-size: 1rem;
        cursor: pointer;
    }
    button.equal:before {
        background: #0067c5;
        box-shadow: inset 0 0 0 1px #0067c5;
    }
    button.lessThan:before {
        background: #e7e9e9;
    }
    button:hover,
    button:focus {
        text-decoration: none;
    }
    button:active,
    button:focus {
        outline: none;
        text-decoration: none;
    }
    button:before {
        content: '';
        position: relative;
        width: 9px;
        height: 9px;
        border-radius: 50%;
        background: #fff;
        box-shadow: inset 0 0 0 1px #78706a;
    }
    button:active:before,
    button:focus:before {
        background: #254b6d;
    }
    button:not(:last-child) {
        margin-right: 1rem;
    }
    button.equal {
        font-weight: 600;
        color: #3e3832;
        text-decoration: none;
    }

    @keyframes shake-max {
        25% {
            transform: translate3d(-2px, 2px, 0);
        }
        50% {
            transform: translate3d(4px, -3px, 0);
        }
        75% {
            transform: translate3d(-5px, -1px, 0);
        }
        100% {
            transform: translate3d(0px, -3px, 0);
        }
    }

    @keyframes shake-mid {
        50% {
            transform: translate3d(2px, 0, 0);
        }
    }

    :global(.shake) { animation: shake-max 0.1s linear both infinite; }

    button.equal.mortest {
        animation: shake-max 0.1s linear both infinite;
    }

    button.equal.enda-mortere {
        animation: shake-mid 0.1s linear both infinite;
    }
</style>
