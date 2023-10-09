import { useEffect, useState } from "react";
import { useAddSystemMessage } from "../state/useSystemMessages";
import { nanoid } from "nanoid";

enum MessageType {
  Endring = "endring",
}

type UseSubscribeResult = [
  subscribeFunction: (fødselsnummer: string) => void,
  tilstand: string
];

type Message = {
  type: MessageType;
  tilstand: string;
}

export const useSubscribe = (): UseSubscribeResult => {
  const [data, setData] = useState<{ fødselsnummer: string; key: string }>();
  const [tilstand, setTilstand] = useState<string>();
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    if (data?.fødselsnummer) {
      const eventSource = new EventSource(`/sse/${data.fødselsnummer}`)

      eventSource.addEventListener('open', () => {
        addMessage({
          id: nanoid(),
          text: "Dokumenter er sendt. Venter på tilstandsendringer i spleis.",
          timeToLiveMs: 5000,
        });
      })

      eventSource.addEventListener('tilstandsendring', (event) => {
        const message: Message = JSON.parse(event.data);
        addMessage({
          id: nanoid(),
          text: `Vedtaksperioden har tilstand: ${message.tilstand}`,
          dismissable: true,
        });

        switch (message.type) {
          case MessageType.Endring: {
            setTilstand(message.tilstand);
            break;
          }
          default: {
            console.error("Received unknown message:", message);
          }
        }
      });

      return () => {
        eventSource.close();
      };
    }
  }, [data]);

  return [
    (fødselsnummer: string) => {
      setData({ fødselsnummer, key: nanoid() });
    },
    tilstand,
  ];
};
