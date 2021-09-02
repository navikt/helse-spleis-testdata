import React from "react";
import { act, renderHook } from "@testing-library/react-hooks";
import { useSystemMessages } from "./useSystemMessages";
import { RecoilRoot } from "recoil";

const wrapper = ({ children }) => <RecoilRoot>{children}</RecoilRoot>;

describe("useSystemMessages", () => {
  it("legger til melding", () => {
    const { result } = renderHook(() => useSystemMessages(), { wrapper });
    const [messages, { addMessage, removeMessage }] = result.current;

    expect(messages).toHaveLength(0);

    act(() => {
      addMessage({ id: "en-melding", text: "En melding", dismissable: true });
    });

    expect(result.current[0]).toHaveLength(1);
  });

  it("fjerner melding", () => {
    const { result } = renderHook(() => useSystemMessages(), { wrapper });
    const [_, { addMessage, removeMessage }] = result.current;

    act(() => {
      addMessage({ id: "en-melding", text: "En melding", dismissable: true });
    });

    expect(result.current[0]).toHaveLength(1);

    act(() => {
      removeMessage("en-melding");
    });

    expect(result.current[0]).toHaveLength(0);
  });
});
