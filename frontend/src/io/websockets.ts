import { useEffect, useState } from "react";

const baseUrl: string =
  import.meta.env.MODE === "dev"
    ? "ws://0.0.0.0:8080"
    : `ws://${window.location.host}`;

enum SubscriptionType {
  Vedtaksperiode = "vedtaksperiode",
}

enum MessageType {
  Endring = "endring",
}

type SubscriptionFrame = {
  type: SubscriptionType;
  fødselsnummer: string;
};

type EndringFrame = {
  type: MessageType;
  tilstand: string;
};

type UseSubscribeResult = [
  subscribeFunction: (fødselsnummer: string) => void,
  tilstand: string
];

export const useSubscribe = (): UseSubscribeResult => {
  const [fødselsnummer, setFødselsnummer] = useState<string>();
  const [tilstand, setTilstand] = useState<string>();

  useEffect(() => {
    if (fødselsnummer) {
      const socket = new WebSocket(`${baseUrl}/ws/vedtaksperiode`);

      setTilstand("IKKE_OPPRETTET");

      socket.onopen = () => {
        socket.send(
          JSON.stringify({
            type: SubscriptionType.Vedtaksperiode,
            fødselsnummer: fødselsnummer,
          })
        );
      };

      socket.onmessage = async (event: MessageEvent) => {
        const message = await JSON.parse(event.data);

        console.log("received message", message);

        switch (message.type) {
          case MessageType.Endring: {
            setTilstand(message.tilstand);
            break;
          }
          default: {
            console.error("Received unknown message:", message);
          }
        }
      };

      socket.onclose = (event: CloseEvent) => {
        console.log("Closed socket", event.reason);
      };

      return () => {
        socket.close();
      };
    }
  }, [fødselsnummer]);

  return [(fødselsnummer: string) => setFødselsnummer(fødselsnummer), tilstand];
};
