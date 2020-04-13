<script>
    import { fly } from 'svelte/transition';

    export let title = null;
</script>

<div
    class="card"
    in:fly="{{ x: -20, duration: 200, delay: 50 }}"
    out:fly="{{ x: -20, duration: 200 }}"
>
    {#if title}
        <h3>{title}</h3>
    {/if}
    <slot />
</div>

<style>
    .card {
        background: #fff;
        width: max-content;
        border-radius: 1rem;
        box-shadow: 0 3px 6px -3px rgba(0, 0, 0, 0.5);
        padding: 1rem;
    }
    :global(.card:not(:last-child)) {
        margin-bottom: 1.5rem;
    }
    h3 {
        margin-top: 0;
    }
</style>
