<script>
    export let className = '';
    export let onClose = () => null;
</script>

<div class="modal" on:click="{onClose}">
    <div class="{`content ${className}`}" on:click="{event => event.stopPropagation()}">
        <slot />
    </div>
</div>

<style>
    .modal {
        display: flex;
        align-items: center;
        justify-content: center;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 10000;
        background: rgba(0, 0, 0, 0.5);
        width: 100vw;
        height: 100vh;
    }
    .content {
        background: #fff;
        padding: 1.5rem;
        border-radius: 0.25rem;
    }
</style>
