import { atom, selector, useRecoilState, useRecoilValue } from "recoil";

type Theme = "light" | "dark";

const themeState = atom<Theme>({
  key: "themeState",
  default: (localStorage.getItem("theme") as Theme) ?? "light",
});

const derivedTheme = selector<Theme>({
  key: "derivedTheme",
  get: ({ get }) => get(themeState),
  set: ({ set }, newValue) => {
    localStorage.setItem("theme", newValue as string);
    set(themeState, newValue);
  },
});

export const useTheme = () => useRecoilValue(derivedTheme);

export const useThemeState = () => useRecoilState(derivedTheme);
