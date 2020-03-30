<script>
    import DateInput from './DateInput.svelte';
    import DeleteButton from './DeleteButton.svelte';

    export let start;
    export let end;
    export let startLabel;
    export let endLabel;
    export let onRemove = () => null;
</script>

<div class="date-range">
    <span class="dates">
        <DateInput bind:value="{start}" label="{startLabel}" className="date" required />
        <DateInput bind:value="{end}" label="{endLabel}" className="date" required />
    </span>
    <DeleteButton onClick="{onRemove}" />
</div>

<style>
    .date-range {
        display: flex;
        align-items: center;
        margin: 1.5rem 4.5rem;
    }
    .dates {
        position: relative;
        display: flex;
        margin-right: 1.75rem;
    }
    .dates :global(.date) {
        min-width: 12rem;
        width: 12rem;
    }
    .dates :global(.date-input:not(:last-child)) {
        margin-right: 1rem;
    }
    button {
        border: none;
        background: none;
    }
    .date-range:not(:last-child) .dates:after {
        position: absolute;
        content: '';
        bottom: -0.5rem;
        border: none;
        border-top: 1px solid #c2c2c2;
        width: calc(100% - 4rem);
        margin-left: 2rem;
    }
</style>
