<script>
  import Spinner from "../Spinner.svelte";

  export let value = "";
  export let disabled = false;
</script>

<style>
  input {
    background: var(--active-color);
    color: white;
    border: none;
    border-radius: 0.5rem;
    width: max-content;
    padding: 0.5rem 2.5rem;
    font-size: 1rem;
  }
  input:hover,
  input:focus {
    background: var(--active-color-dark);
    box-shadow: 0 0 0 0.125rem white, 0 0 0 0.25rem var(--active-color-dark);
  }
  input:active {
    background: white;
    color: var(--active-color-dark);
    box-shadow: 0 0 0 0.125rem var(--active-color-dark);
  }
  input:disabled {
    background: var(--disabled-color);
    box-shadow: none;
  }
  .wrapper {
    display: flex;
    align-items: center;
    width: max-content;
    height: max-content;
    margin-top: 1rem;
  }
</style>

<div class="wrapper">
  <input type="submit" {value} {disabled} />
  {#if disabled}
    <Spinner />
  {/if}
</div>
