<script>
    import DateInput from './DateInput.svelte';
    import DeleteButton from './DeleteButton.svelte';

    export let start;
    export let end;
    export let startLabel;
    export let endLabel;
    export let onRemove = () => null;
</script>

<div class="container">
    <span class="dates">
        <DateInput bind:value="{start}" label="{startLabel}" required />
        <DateInput bind:value="{end}" label="{endLabel}" required />
    </span>
    <DeleteButton onClick="{onRemove}" />
</div>

<style>
    .container {
        display: flex;
        align-items: center;
        margin: 1.5rem 4.5rem;
    }
    .dates {
        margin-right: 1.75rem;
    }
    button {
        border: none;
        background: none;
    }
</style>
