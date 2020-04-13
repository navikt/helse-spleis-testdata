<script>
    import { uuid } from '../../scripts/uuid';
    const id = uuid();

    export let className = '';
    export let placeholder;
    export let value = null;
    export let onBlur = () => {};
    export let invalid = false;
    export let required = false;
    export let disabled = false;

    let isActive = false;
</script>

<div class="input-container" class:isActive class:hasValue="{value}">
    <input
        type="text"
        {id}
        {required}
        {disabled}
        class:invalid
        class="{className}"
        bind:value
        on:blur="{event => {
            onBlur(event);
            isActive = false;
        }}"
        on:focus="{() => (isActive = true)}"
    />
    <div class="placeholder" class:isActive class:hasValue="{value}">{placeholder}</div>
</div>

<style>
    .input-container {
        position: relative;
        width: max-content;
        display: flex;
        flex-direction: column;
    }
    :global(.input-container):not(:last-child) {
        margin-bottom: 0.75rem;
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
        padding: 0.85rem 0.75rem 0.65rem;
        color: #7f7f7f;
        transition: all 0.2s ease;
    }
    .placeholder.isActive {
        color: var(--active-color);
    }
    .placeholder.hasValue,
    .placeholder.isActive {
        font-size: 0.75rem;
        padding: 0.25rem 0.75rem;
    }
    input {
        padding: 1.25rem 0.75rem 0.25rem;
    }
</style>
