import { useContext } from "solid-js";
import { ThemeContext } from "./ThemeContext";

export const useTheme = () => {
  const [state] = useContext(ThemeContext);
  return state;
};
