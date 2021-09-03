import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { RecoilRoot } from "recoil";
import { SystemMessages } from "./SystemMessages";
import { systemMessagesState } from "../state/useSystemMessages";
import userEvent from "@testing-library/user-event";

jest.mock("react-spring", () => ({
  animated: {
    div: ({ children }) => <div>{children}</div>,
  },
  useTransition:
    (messages: SystemMessageObject[]) =>
    (func: (style: {}, item: SystemMessageObject) => React.ReactNode) =>
      messages.map((it) => func({}, it)),
}));

const wrapper = ({ children }) => (
  <RecoilRoot
    initializeState={({ set }) => {
      set(systemMessagesState, [
        { id: "en-melding", text: "En melding", dismissable: true },
      ]);
    }}
  >
    {children}
  </RecoilRoot>
);

describe("SystemMessages", () => {
  it("kan lukkes", async () => {
    render(<SystemMessages />, { wrapper });
    expect(screen.queryByText("En melding")).toBeVisible();

    userEvent.click(screen.getByRole("button"));
    expect(screen.queryByText("En melding")).toBeNull();
  });
});
