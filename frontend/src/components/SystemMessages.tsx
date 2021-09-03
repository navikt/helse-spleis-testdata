import React, { useMemo } from "react";
import styles from "./SystemMessages.module.css";
import { useSystemMessages } from "../state/useSystemMessages";
import { SystemMessage } from "./SystemMessage";
import type { TransitionFn } from "react-spring";
import { animated, useTransition } from "react-spring";

type UseSystemMessageTransitionsResult = [
  TransitionFn<SystemMessageObject, { opacity: number; height: number }>,
  WeakMap<object, HTMLElement>
];

const useSystemMessageTransitions = (
  messages: SystemMessageObject[]
): UseSystemMessageTransitionsResult => {
  const refs = useMemo(() => new WeakMap(), []);

  const transitions = useTransition(messages, {
    keys: (item) => item.id,
    from: { opacity: 0, height: 0 },
    enter: (item) => async (start, stop) => {
      await start({ opacity: 1, height: refs.get(item).offsetHeight });
    },
    leave: [{ opacity: 0 }, { height: 0 }],
    onRest: (result, _spring, item) => {
      result.finished && refs.delete(item);
    },
  });

  return [transitions, refs];
};

export const SystemMessages: React.FC = React.memo(() => {
  const [messages] = useSystemMessages();
  const [transitions, refs] = useSystemMessageTransitions(messages);

  return (
    <div className={styles.SystemMessages}>
      {transitions((style, it) => (
        <animated.div
          style={{ ...style, marginBottom: "var(--block-extra-small)" }}
        >
          <SystemMessage ref={(ref) => ref && refs.set(it, ref)} {...it} />
        </animated.div>
      ))}
    </div>
  );
});
