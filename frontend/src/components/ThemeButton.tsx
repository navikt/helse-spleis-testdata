import React from "react";
import { Button } from "@navikt/ds-react";
import { MoonIcon, SunIcon } from "@navikt/aksel-icons";
import { useThemeState } from "../state/useTheme";
import styles from "./ThemeButton.module.css";

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
    <Button
      type="button"
      variant="tertiary"
      data-color="neutral"
      className={styles.ThemeButton}
      icon={
        theme === "light" ? <SunIcon aria-hidden /> : <MoonIcon aria-hidden />
      }
      aria-label={
        theme === "light" ? "Bytt til mørk modus" : "Bytt til lys modus"
      }
      onClick={toggleTheme}
      {...rest}
    />
  );
};
