<script>
    import { fly } from 'svelte/transition';

    export let onClick = () => null;
    export let size = 24;
</script>

<button
    type="button"
    on:click|preventDefault="{onClick}"
    in:fly="{{ x: 20, duration: 200 }}"
    out:fly="{{ x: 20, duration: 200 }}"
    style="--size: {size}px"
>
    <svg xmlns="http://www.w3.org/2000/svg" height={size} viewBox="0 0 24 24" width={size}>
        <path d="M0 0h24v24H0V0z" fill="none"></path>
        <path
            class="icon"
            d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zm2.46-7.12l1.41-1.41L12 12.59l2.12-2.12
            1.41 1.41L13.41 14l2.12 2.12-1.41 1.41L12 15.41l-2.12 2.12-1.41-1.41L10.59
            14l-2.13-2.12zM15.5 4l-1-1h-5l-1 1H5v2h14V4z"
        ></path>
        <path d="M0 0h24v24H0z" fill="none"></path>
    </svg>
</button>

<style>
    button {
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--background);
        width: calc(var(--size) + 1rem);
        height: calc(var(--size) + 1rem);
        padding: 0;
        border-radius: 50%;
        border: none;
        cursor: pointer;
        outline: none;
        transition: all 0.1s ease;
    }
    button:hover,
    button:focus {
        background: #c73535;
    }
    button:hover .icon,
    button:focus .icon,
    button:active .icon {
        fill: #fff;
    }
    button:active {
        box-shadow: 0 0 0 3px #c73535;
    }
</style>
