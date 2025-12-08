import { useAppContext } from "./AppContext";

type UseAddSystemMessageResult = (message: SystemMessageObject) => void;

export const useAddSystemMessage = (): UseAddSystemMessageResult => {
  const { setSystemMessages } = useAppContext();

  return (message: SystemMessageObject): void => {
    setSystemMessages((old) => [...old, message]);
  };
};

type UseRemoveSystemMessageResult = (id: string) => void;

export const useRemoveSystemMessage = (): UseRemoveSystemMessageResult => {
  const { setSystemMessages } = useAppContext();

  return (id: string): void => {
    setSystemMessages((old) => old.filter((it) => it.id !== id));
  };
};

type UseClearSystemMessagesResult = () => void;

export const useClearSystemMessages = (): UseClearSystemMessagesResult => {
  const { systemMessages, setSystemMessages } = useAppContext();

  const removeLastMessage = () => {
    setSystemMessages((prev) => prev.slice(0, prev.length - 1));
  };

  const removeMessages = () => {
    removeLastMessage();
    let i = 0;
    const interval = setInterval(() => {
      if (i < systemMessages.length - 1) {
        removeLastMessage();
        i++;
      } else clearInterval(interval);
    }, 50);
  };

  return () => removeMessages();
};

type UseSystemMessagesResult = [
  messages: SystemMessageObject[],
  methods: {
    addMessage: (message: SystemMessageObject) => void;
    removeMessage: (id: string) => void;
    clearMessages: UseClearSystemMessagesResult;
  }
];

export const useSystemMessages = (): UseSystemMessagesResult => {
  const { systemMessages } = useAppContext();
  const addMessage = useAddSystemMessage();
  const removeMessage = useRemoveSystemMessage();
  const clearMessages = useClearSystemMessages();
  return [systemMessages, { addMessage, removeMessage, clearMessages }];
};
