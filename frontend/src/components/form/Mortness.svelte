<script>
    const mortnessScale = ['Mort', 'Mortere', 'Enda mortere', 'Mortest'];

    let currentMortness = 0;

    $: classForMortness = mortness =>
        currentMortness > mortness
            ? 'lessThan'
            : currentMortness < mortness
            ? 'greaterThan'
            : 'equal';
</script>

<div class="mortness">
    <hr />
    <button type="button" on:click="{() => (currentMortness = 0)}" class="{classForMortness(0)}">
        {mortnessScale[0]}
    </button>
    <button type="button" on:click="{() => (currentMortness = 1)}" class="{classForMortness(1)}">
        {mortnessScale[1]}
    </button>
    <button type="button" on:click="{() => (currentMortness = 2)}" class="{classForMortness(2)}">
        {mortnessScale[2]}
    </button>
    <button type="button" on:click="{() => (currentMortness = 3)}" class="{classForMortness(3)}">
        {mortnessScale[3]}
    </button>
</div>

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
</style>
