import { useEffect, useState } from "react";
import { useAddSystemMessage } from "../state/useSystemMessages";
import { nanoid } from "nanoid";

enum MessageType {
  Endring = "endring",
  Sletting = "sletting",
}

type UseSubscribeResult = [
  subscribeFunction: (fødselsnummer: string) => void,
  tilstand: string
];

type Message = {
  type: MessageType;
  verdi: string;
}

export const useSubscribe = (): UseSubscribeResult => {
  const [data, setData] = useState<{ fødselsnummer: string; key: string }>();
  const [tilstand, setTilstand] = useState<string>();
  const [eventSource, setEventSource] = useState<EventSource>();
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    if (!data?.fødselsnummer) return
    const eventSource = new EventSource(`/sse/${data.fødselsnummer}`)
    console.log(`eventSource opprettet: ${eventSource.url}`)
    setEventSource(eventSource)

    eventSource.addEventListener('open', () => {
      console.log(`mottar meldinger for ${data.fødselsnummer}`);
    })
    eventSource.addEventListener('error', () => {
      console.log(`feil på tilkobling for ${data.fødselsnummer}`);
    })

    return () => {
      console.log("lukker eventSource " + eventSource.url);
      eventSource.close();
    };
  }, [data?.fødselsnummer])

  useEffect(() => {
    if (!eventSource) return;
    console.log('registrerer lyttere for tilstandsendring')
    eventSource.addEventListener('tilstandsendring', (event) => {
      const message: Message = JSON.parse(event.data);
      switch (message.type) {
        case MessageType.Endring: {
          addMessage({
            id: nanoid(),
            text: `Vedtaksperioden har tilstand: ${message.verdi}`,
            dismissable: true,
          });
          setTilstand(message.verdi);
          break;
        }
        case MessageType.Sletting: {
          addMessage({
            id: nanoid(),
            text: `Personen er slettet fra ${message.verdi}`,
            dismissable: true,
            timeToLiveMs: 5000,
          });
          break;
        }
        default: {
          console.error("Received unknown message:", message);
        }
      }
    });
    }, [eventSource?.url]);

  return [
    (fødselsnummer: string) => setData({ fødselsnummer, key: nanoid() }),
    tilstand,
  ];
};
