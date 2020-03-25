<script>
  import { uuid } from '../../scripts/uuid';
  const id = uuid();

  export let label;
  export let placeholder;
  export let value;
  export let onblur = () => {};
  export let invalid = false;
  export let required = false;
  export let disabled = false;
  export let max = 0;
  export let min = 100;
</script>

<label for="{id}">{label}</label>
<span>
  <input
    {id}
    type="number"
    class:invalid
    class="{disabled ? 'hide' : ''}"
    bind:value
    on:blur="{onblur}"
    {required}
    {placeholder}
    {disabled}
    {max}
    {min}
  />
  {#if invalid}
    <p>Ugyldig verdi</p>
  {/if}
</span>

<style>
  span {
    width: max-content;
    display: flex;
  }
  p {
    color: red;
    margin-left: 1rem;
  }
  .invalid {
    border-color: red;
  }
  input {
    width: 10rem;
  }
</style>
