import type { Component } from "solid-js";
import { createContext } from "solid-js";
import { createStore } from "solid-js/store";

type SystemMessage = {
  id: string;
  text: string;
  dismissable?: boolean;
  timeToLiveMs?: number;
};

type SystemMessageContextValue = [
  { messages: SystemMessage[] },
  {
    add: (message: SystemMessage) => void;
    remove: (id: string) => void;
  }
];

export const SystemMessageContext = createContext<SystemMessageContextValue>([
  { messages: [] },
  {
    add: (_) => null,
    remove: (_) => null,
  },
]);

export const SystemMessageProvider: Component = (props) => {
  const [state, setState] = createStore({ messages: [] });

  const removeMessage = (id: string) => {
    setState("messages", (old) => old.filter((it) => it.id !== id));
  };

  const addMessage = (message: SystemMessage) => {
    console.log("adding message", message);
    setState("messages", (old) => [...old, message]);
    if (!isNaN(message.timeToLiveMs)) {
      setTimeout(() => removeMessage(message.id), message.timeToLiveMs);
    }
  };

  const store = [
    state,
    {
      add: addMessage,
      remove: removeMessage,
    },
  ] as SystemMessageContextValue;

  return (
    <SystemMessageContext.Provider value={store}>
      {props.children}
    </SystemMessageContext.Provider>
  );
};
