import { useContext } from "solid-js";
import { SystemMessageContext } from "../state/SystemMessageContext";

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

export const subscribe = (fødselsnummer: string) => {
  const socket = new WebSocket(`${baseUrl}/ws/vedtaksperiode`);
  const [messagesContext, { add, remove }] = useContext(SystemMessageContext);

  let currentTilstand = "IKKE_OPPRETTET";
  add({ id: currentTilstand, text: currentTilstand });

  socket.onopen = () => {
    socket.send(
      JSON.stringify({
        type: SubscriptionType.Vedtaksperiode,
        fødselsnummer: fødselsnummer,
      })
    );
  };

  console.log(add, messagesContext.messages);

  socket.onmessage = async (event: MessageEvent) => {
    const message = await JSON.parse(event.data);

    console.log("received message", message);

    switch (message.type) {
      case MessageType.Endring: {
        remove(currentTilstand);
        currentTilstand = message.tilstand;
        add({ id: currentTilstand, text: currentTilstand });
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
};
