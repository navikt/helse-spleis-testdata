import { Theme, useAppContext } from "./AppContext";

export type { Theme } from "./AppContext";

export const useTheme = (): Theme => {
  const { theme } = useAppContext();
  return theme;
};

export const useThemeState = (): [Theme, (theme: Theme) => void] => {
  const { theme, setTheme } = useAppContext();
  return [theme, setTheme];
};
