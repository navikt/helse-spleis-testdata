import styles from "./ThemeButton.module.css";
import lightIcon from "material-design-icons/image/svg/production/ic_wb_sunny_24px.svg";
import darkIcon from "material-design-icons/image/svg/production/ic_brightness_2_24px.svg";
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
      {theme === "light" && <img src={lightIcon} alt="" />}
      {theme === "dark" && <img src={darkIcon} alt="" />}
    </button>
  );
};
