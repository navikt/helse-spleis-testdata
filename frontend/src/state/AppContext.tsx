import React, { createContext, ReactNode, useContext, useState } from "react";

export type Theme = "light" | "dark";

interface AppState {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  systemMessages: SystemMessageObject[];
  setSystemMessages: React.Dispatch<
    React.SetStateAction<SystemMessageObject[]>
  >;
}

const AppContext = createContext<AppState | undefined>(undefined);

interface AppProviderProps {
  children: ReactNode;
  initialTheme?: Theme;
  initialMessages?: SystemMessageObject[];
}

export const AppProvider: React.FC<AppProviderProps> = ({
  children,
  initialTheme,
  initialMessages = [],
}) => {
  const [theme, setThemeState] = useState<Theme>(() => {
    if (initialTheme) return initialTheme;
    return (localStorage.getItem("theme") as Theme) ?? "light";
  });

  const [systemMessages, setSystemMessages] =
    useState<SystemMessageObject[]>(initialMessages);

  const setTheme = (newTheme: Theme) => {
    localStorage.setItem("theme", newTheme);
    setThemeState(newTheme);
  };

  return (
    <AppContext.Provider
      value={{
        theme,
        setTheme,
        systemMessages,
        setSystemMessages,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useAppContext = (): AppState => {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error("useAppContext must be used within an AppProvider");
  }
  return context;
};
