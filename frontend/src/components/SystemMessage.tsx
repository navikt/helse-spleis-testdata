import React, { useEffect } from "react";
import styles from "./SystemMessage.module.css";
import classNames from "classnames";
import { useRemoveSystemMessage } from "../state/useSystemMessages";
import { Text } from "./Text";

export const SystemMessageInitializationError = () =>
  Error(
    "Kan ikke opprette systemmelding som hverken lukkes automatisk eller kan lukkes av bruker"
  );

interface SystemMessageProps
  extends SystemMessageObject,
    Omit<React.HTMLAttributes<HTMLDivElement>, "id"> {}

export const SystemMessage = React.forwardRef<
  HTMLDivElement,
  SystemMessageProps
>(({ id, text, dismissable, timeToLiveMs, ...rest }, ref) => {
  if (!dismissable && timeToLiveMs === undefined) {
    throw SystemMessageInitializationError();
  }

  const removeMessage = useRemoveSystemMessage();

  useEffect(() => {
    if (timeToLiveMs !== undefined) {
      const timeout = setTimeout(() => removeMessage(id), timeToLiveMs);
      return () => clearTimeout(timeout);
    }
  }, [id, timeToLiveMs]);

  return (
    <div className={classNames(styles.SystemMessage)} ref={ref} {...rest}>
      <Text className={styles.MessageText}>{text}</Text>
      {dismissable && (
        <button
          className={styles.DismissButton}
          onClick={() => removeMessage(id)}
        >
          <i className="material-icons close" />
        </button>
      )}
    </div>
  );
});
