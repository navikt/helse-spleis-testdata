import {atom, useRecoilValue, useResetRecoilState, useSetRecoilState} from "recoil";

export const systemMessagesState = atom<SystemMessageObject[]>({
  key: "systemMessagesState",
  default: [],
});

type UseAddSystemMessageResult = (message: SystemMessageObject) => void;

export const useAddSystemMessage = (): UseAddSystemMessageResult => {
  const setSystemMessages = useSetRecoilState(systemMessagesState);

  return (message: SystemMessageObject): void => {
    setSystemMessages((old) => [...old, message]);
  };
};

type UseRemoveSystemMessageResult = (id: string) => void;

export const useRemoveSystemMessage = (): UseRemoveSystemMessageResult => {
  const setSystemMessages = useSetRecoilState(systemMessagesState);

  return (id: string): void => {
    setSystemMessages((old) => old.filter((it) => it.id !== id));
  };
};

type UseClearSystemMessagesResult = () => void;

export const useClearSystemMessages = (): UseClearSystemMessagesResult => {
  const resetSystemMessages = useResetRecoilState(systemMessagesState);
  return () => resetSystemMessages();
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
  const messages = useRecoilValue(systemMessagesState);
  const addMessage = useAddSystemMessage();
  const removeMessage = useRemoveSystemMessage();
  const clearMessages = useClearSystemMessages();
  return [messages, { addMessage, removeMessage, clearMessages }];
};
