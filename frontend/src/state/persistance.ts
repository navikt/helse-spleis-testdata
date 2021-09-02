import { Dispatch, SetStateAction, useEffect, useState } from "react";

type UsePersistedStateResult<T> = [
  value: T,
  set: Dispatch<SetStateAction<T>>,
  clear: () => void
];

const usePersistedState = <T extends unknown>(
  storage: Storage,
  key: string
): UsePersistedStateResult<T> => {
  const [value, setValue] = useState<T>(JSON.parse(storage.getItem(key)));

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
  key: string
): UsePersistedStateResult<T> => usePersistedState(localStorage, key);
