import { atom, selector, useRecoilState, useRecoilValue } from "recoil";
import { useEffect } from "react";

export type Theme = "light" | "dark";

const storage: Storage = localStorage;

const themeState = atom<Theme>({
  key: "themeState",
  default: (storage.getItem("theme") as Theme) ?? "light",
});

const derivedTheme = selector<Theme>({
  key: "derivedTheme",
  get: ({ get }) => get(themeState),
  set: ({ set }, newValue) => {
    storage.setItem("theme", newValue as string);
    set(themeState, newValue);
  },
});

export const useTheme = (): Theme => useRecoilValue(derivedTheme);

export const useThemeState = () => useRecoilState<Theme>(derivedTheme);

export const useUpdateBodyBackgroundColor = (theme) => {
  useEffect(() => {
    document.body.style.setProperty(
      "--body-background-color",
      theme === "light" ? "white" : "black"
    );
  }, [theme]);
};
