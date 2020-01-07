<script>
    export let value = '';
</script>

<input type="submit" value={value} />

<style>
    input {
        width: max-content;
        --webkit-appearance: button;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 1em;
        white-space: nowrap;
        border-radius: 0.125em;
        cursor: pointer;
        user-select: none;
        letter-spacing: 0.0625em;
        line-height: 1.375rem;
        text-transform: uppercase;
        text-decoration: none;
        padding: calc(.475rem - 1px) 2.5rem calc(.525rem - 1px) 2.5rem;
        min-height: 2.5rem;
        vertical-align: middle;
        transform: translateY(-2px);
        box-shadow: 0 2px 0 #C6C2BF;
        color: #FFF;
        border-color: #0067C5;
        background-color: #0067C5;
        position: relative;
    }
    input:hover {
        color: #FFF;
        border-color: #0074df;
        background-color: #0074df;
        transform: translateY(-3px);
        box-shadow: 0 3px 0 #d2cfcc;
    }
    input:active {
        box-shadow: none;
        border-color: #005B82;
        background-color: #005B82;
        transform: translateY(0);
    }
    input:focus {
        background: white;
        color: #0067C5;
        outline: none;
        box-shadow: 0 0 0 3px #254b6d;
    }
</style>