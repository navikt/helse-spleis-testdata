import React from "react";
import { InternalHeader } from "@navikt/ds-react";
import { MoonIcon, SunIcon } from "@navikt/aksel-icons";
import { useThemeState } from "../state/useTheme";

interface ThemeButtonProps extends Omit<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  "color"
> {}

export const ThemeButton: React.FC<ThemeButtonProps> = ({ ...rest }) => {
  const [theme, setTheme] = useThemeState();

  const toggleTheme = () => {
    setTheme(theme === "light" ? "dark" : "light");
  };

  return (
    <InternalHeader.Button
      type="button"
      aria-label={
        theme === "light" ? "Bytt til mørk modus" : "Bytt til lys modus"
      }
      onClick={toggleTheme}
      {...rest}
    >
      {theme === "light" ? (
        <SunIcon aria-hidden fontSize="1.5rem" />
      ) : (
        <MoonIcon aria-hidden fontSize="1.5rem" />
      )}
    </InternalHeader.Button>
  );
};
