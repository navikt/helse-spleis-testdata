<script>
    export let onClick = () => null;
    export let label = 'Fjern';
</script>

<button type="button" on:click|preventDefault="{onClick}">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="-1 -1 25 25">
        <g
            stroke="#0067c5"
            stroke-width="2px"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-miterlimit="10"
            fill="none"
        >
            <path
                d="M3.516 3.5h16v20h-16zM7.516.5h8v3h-8zM1.016 3.5h22M7.516 7v12M11.516 7v12M15.516
                7v12"
            ></path>
        </g>
    </svg>
    <span class="label">{label}</span>
</button>

<style>
    .label {
        color: #0067c5;
        font-size: 1rem;
        padding: 0 0.125rem;
        margin: 0 0 0 0.75rem;
        box-shadow: inset 0 -1px 0 0 #0067c5;
    }
    .content {
        margin: 1rem 2.325rem;
    }
    button {
        display: flex;
        align-items: center;
        background: none;
        padding: 0;
        border: none;
        cursor: pointer;
    }
    button:focus path,
    button:active path {
        stroke: #254b6d;
    }
    button:focus .label,
    button:active .label {
        color: #254b6d;
    }
    button:hover,
    button:focus {
        outline: none;
    }
    button:hover .label,
    button:focus .label {
        box-shadow: none;
    }
</style>
