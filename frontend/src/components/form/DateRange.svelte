<script>
    import DateInput from './DateInput.svelte';
    import DeleteButton from './DeleteButton.svelte';
    import Card from '../Card.svelte';

    export let start;
    export let end;
    export let startLabel;
    export let endLabel;
    export let onRemove = () => null;
</script>

<div class="date-range">
    <Card>
        <span class="dates">
            <DateInput bind:value="{start}" label="{startLabel}" required />
            <DateInput bind:value="{end}" label="{endLabel}" required />
        </span>
    </Card>
    <DeleteButton onClick="{onRemove}" />
</div>

<style>
    .date-range {
        display: flex;
        align-items: center;
    }
    .date-range:not(:last-child) {
        margin-bottom: 1.5rem;
    }
    :global(.date-range > .card) {
        margin-bottom: 0;
        margin-right: 1rem;
    }
    .dates {
        position: relative;
        display: flex;
        margin-bottom: -0.75rem;
    }
    .dates :global(.input-container > input) {
        min-width: 12rem;
        width: 12rem;
    }
    .dates :global(.input-container:not(:last-child)) {
        margin-right: 1rem;
    }
    button {
        border: none;
        background: none;
    }
</style>
