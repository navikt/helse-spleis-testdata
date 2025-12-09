import { Dispatch, SetStateAction, useEffect, useState } from "react";

type UsePersistedStateResult<T> = [
  value: T | null,
  set: Dispatch<SetStateAction<T | null>>,
  clear: () => void,
];

const usePersistedState = <T extends unknown>(
  storage: Storage,
  key: string,
): UsePersistedStateResult<T> => {
  const [value, setValue] = useState<T | null>(() => {
    const item = storage.getItem(key);
    return item ? JSON.parse(item) : null;
  });

  const clearValue = () => {
    storage.removeItem(key);
    setValue(null);
  };

  useEffect(() => {
    storage.setItem(key, JSON.stringify(value));
  }, [value]);

  return [value, setValue, clearValue];
};

export const useLocalStorageState = <T extends unknown>(
  key: string,
): UsePersistedStateResult<T> => usePersistedState(localStorage, key);
