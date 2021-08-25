import { useContext } from "solid-js";
import { ThemeContext } from "./ThemeProvider";

export const useTheme = () => {
  const [state] = useContext(ThemeContext);
  return state;
};
