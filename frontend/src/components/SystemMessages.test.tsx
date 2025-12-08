import React from "react";
import { render, screen } from "@testing-library/react";
import { AppProvider } from "../state/AppContext";
import { SystemMessages } from "./SystemMessages";
import userEvent from "@testing-library/user-event";
import { vi, describe, it, expect } from "vitest";

vi.mock("react-spring", () => ({
  animated: {
    div: ({ children }) => <div>{children}</div>,
  },
  useTransition:
    (messages: SystemMessageObject[]) =>
    (func: (style: {}, item: SystemMessageObject) => React.ReactNode) =>
      messages.map((it) => func({}, it)),
}));

const wrapper = ({ children }) => (
  <AppProvider
    initialMessages={[
      { id: "en-melding", text: "En melding", dismissable: true },
    ]}
  >
    {children}
  </AppProvider>
);

describe("SystemMessages", () => {
  it("kan lukkes", async () => {
    render(<SystemMessages />, { wrapper });
    expect(screen.queryByText("En melding")).toBeVisible();

    await userEvent.click(screen.getByRole("button", { name: /Fjern alle meldinger/ }));
    expect(screen.queryByText("En melding")).toBeNull();
  });
});
