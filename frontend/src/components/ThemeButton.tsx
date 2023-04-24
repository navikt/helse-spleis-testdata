import styles from "./ThemeButton.module.css";
import classNames from "classnames";
import { useThemeState } from "../state/useTheme";
import React from "react";

interface ThemeButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const ThemeButton: React.FC<ThemeButtonProps> = ({
  className,
  ...rest
}) => {
  const [theme, setTheme] = useThemeState();

  const toggleTheme = () => {
    setTheme(theme === "light" ? "dark" : "light");
  };

  return (
    <button
      className={classNames(styles.ThemeButton, styles[theme], className)}
      onClick={toggleTheme}
      {...rest}
    >
      {theme === "light" && <i className="material-icons wb_sunny" />}
      {theme === "dark" && <i className="material-icons brightness_2" />}
    </button>
  );
};
