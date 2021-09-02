import { useEffect, useState } from "react";
import { useAddSystemMessage } from "../state/useSystemMessages";
import { nanoid } from "nanoid";

type WebsocketProtocol = "ws" | "wss";

const protocol: WebsocketProtocol =
  import.meta.env.MODE === "dev" || window.location.host.includes("0.0.0.0")
    ? "ws"
    : "wss";

const baseUrl: string =
  import.meta.env.MODE === "dev" ? "0.0.0.0:8080" : window.location.host;

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
  const [data, setData] = useState<{ fødselsnummer: string; key: string }>();
  const [tilstand, setTilstand] = useState<string>();
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    if (data?.fødselsnummer) {
      const socket = new WebSocket(
        `${protocol}://${`${baseUrl}/ws/vedtaksperiode`}`
      );

      addMessage({
        id: nanoid(),
        text: "Dokumenter er sendt. Venter på tilstandsendringer i spleis.",
        timeToLiveMs: 5000,
      });

      socket.onopen = () => {
        socket.send(
          JSON.stringify({
            type: SubscriptionType.Vedtaksperiode,
            fødselsnummer: data.fødselsnummer,
          })
        );
      };

      socket.onmessage = async (event: MessageEvent) => {
        const message = await JSON.parse(event.data);

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
      };

      socket.onclose = (event: CloseEvent) => {
        console.log("Closed socket", event.reason);
      };

      return () => {
        socket.close();
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
