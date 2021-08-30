import type { Component } from "solid-js";
import { createContext } from "solid-js";
import { createStore } from "solid-js/store";

type Theme = "light" | "dark";

type ThemeContextValue = [
  { theme: Theme },
  { setTheme: (theme: Theme) => void }
];

const getThemeFromStorage = (): Theme =>
  (localStorage.getItem("theme") as Theme) ?? "light";

export const ThemeContext = createContext<ThemeContextValue>([
  { theme: getThemeFromStorage() },
  { setTheme: (_) => null },
]);

export const ThemeProvider: Component = (props) => {
  const [theme, setTheme] = createStore({ theme: getThemeFromStorage() });

  const store = [
    theme,
    {
      setTheme: (theme: Theme) => {
        localStorage.setItem("theme", theme);
        setTheme("theme", () => theme);
      },
    },
  ] as ThemeContextValue;

  return (
    <ThemeContext.Provider value={store}>
      {props.children}
    </ThemeContext.Provider>
  );
};
