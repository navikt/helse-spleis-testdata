import { Accessor, createEffect, createSignal, Setter } from "solid-js";

const createPersistedSignal = <T extends unknown>(
  storage: Storage,
  key: string
): [get: Accessor<T>, set: Setter<T>] => {
  const [value, setValue] = createSignal<T>(JSON.parse(storage.getItem(key)));

  createEffect(() => {
    storage.setItem(key, JSON.stringify(value()));
  });

  return [value, setValue];
};

export const createLocalStorageSignal = <T extends unknown>(
  key: string
): [get: Accessor<T>, set: Setter<T>] =>
  createPersistedSignal(localStorage, key);
