<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let value;
    export let label = '';
    export let className = '';
    export let placeholder = label;
    export let required = false;

    let isActive = false;
</script>

<div class="input-container" class:isActive>
    <input
        {id}
        on:focus="{() => (isActive = true)}"
        on:blur="{() => (isActive = false)}"
        type="date"
        aria-label="{label}"
        class="{className}"
        bind:value
        {required}
    />
    <div class="placeholder" class:isActive>{placeholder}</div>
</div>

<style>
    .input-container {
        position: relative;
        width: max-content;
        display: flex;
        flex-direction: column;
    }
    .input-container:before {
        position: absolute;
        content: '';
        background: var(--active-color);
        width: 0;
        height: 2px;
        bottom: -2px;
        left: 0;
        z-index: 1000;
        transition: all 0.2s ease;
    }
    .input-container.isActive:before {
        left: 0;
        width: 100%;
        height: 2px;
    }
    .placeholder {
        user-select: none;
        pointer-events: none;
        position: absolute;
        color: #7f7f7f;
        transition: all 0.2s ease;
        font-size: 0.75rem;
        padding: 0.25rem 0.75rem;
    }
    .placeholder.isActive {
        color: var(--active-color);
    }
    input {
        padding: 1.25rem 0.75rem 0.25rem;
    }
</style>
