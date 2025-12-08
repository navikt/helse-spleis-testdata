import React, {act, ReactNode} from "react";
import { useSystemMessages } from "./useSystemMessages";
import { AppProvider } from "./AppContext";
import { describe, it, expect } from "vitest";
import {renderHook} from "@testing-library/react";

const wrapper = ({ children }: { children: ReactNode }) => <AppProvider>{children}</AppProvider>;

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
