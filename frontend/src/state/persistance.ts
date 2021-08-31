import { Dispatch, SetStateAction, useEffect, useState } from "react";

const usePersistedState = <T extends unknown>(
  storage: Storage,
  key: string
): [T, Dispatch<SetStateAction<T>>] => {
  const [value, setValue] = useState<T>(JSON.parse(storage.getItem(key)));

  useEffect(() => {
    storage.setItem(key, JSON.stringify(value));
  }, [value]);

  return [value, setValue];
};

export const useLocalStorageState = <T extends unknown>(
  key: string
): [T, Dispatch<SetStateAction<T>>] => usePersistedState(localStorage, key);
