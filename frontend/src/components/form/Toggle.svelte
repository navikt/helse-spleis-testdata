<script>
    import { uuid } from '../../scripts/uuid';

    export let label = '';
    export let checked = false;

    const id = uuid();

    const onClick = event => {
        checked = !checked;
    };
</script>

<div class="container">
    <button
        class="toggle"
        type="button"
        on:click="{onClick}"
        class:checked
        role="checkbox"
        aria-checked="{checked}"
        tabindex="0"
    ></button>
    <label on:click="{onClick}" for="{id}">{label}</label>
</div>

<style>
    .container {
        display: flex;
        align-items: center;
        height: 1.5rem;
    }
    label {
        margin: 0;
        font-weight: 400;
    }
    .toggle {
        position: relative;
        height: 1rem;
        width: 1.5rem;
        border: none;
        margin: 0 0.5rem 0 0;
        border-radius: 0.5rem;
        background-color: #c2c2c2;
        transition: all 0.1s ease-in-out;
        cursor: pointer;
    }
    .toggle:before {
        position: absolute;
        content: '';
        height: 0.75rem;
        width: 0.75rem;
        top: 50%;
        left: 0.125rem;
        transform: translateY(-50%);
        transition: all 0.1s ease-in-out;
        background-color: white;
        border-radius: 50%;
    }
    .toggle.checked {
        background-color: #00b58a;
    }
    .toggle:focus {
        outline: none;
        background: #78706a;
    }
    .toggle.checked:focus {
        background: var(--active-color-dark);
    }
    .toggle.checked:before {
        left: 0.625rem;
    }
</style>
