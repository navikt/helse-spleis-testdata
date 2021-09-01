import React, { useEffect } from "react";
import styles from "./SystemMessage.module.css";
import classNames from "classnames";
import { useRemoveSystemMessage } from "../state/useSystemMessages";
import { Text } from "./Text";

interface SystemMessageProps extends SystemMessageObject {}

import closeIcon from "material-design-icons/navigation/svg/production/ic_close_18px.svg";

export const SystemMessage = React.forwardRef<
  HTMLDivElement,
  SystemMessageProps
>(({ id, text, dismissable, timeToLiveMs }, ref) => {
  if (!dismissable && timeToLiveMs === undefined) {
    throw Error(
      "Kan ikke opprette systemmelding som hverken lukkes automatisk eller kan lukkes av bruker"
    );
  }
  const removeMessage = useRemoveSystemMessage();

  useEffect(() => {
    if (timeToLiveMs !== undefined) {
      const timeout = setTimeout(() => removeMessage(id), timeToLiveMs);
      return () => clearTimeout(timeout);
    }
  }, [id, timeToLiveMs]);

  return (
    <div className={classNames(styles.SystemMessage)} ref={ref}>
      <Text className={styles.MessageText}>{text}</Text>
      {dismissable || (
        <button
          className={styles.DismissButton}
          onClick={() => removeMessage(id)}
        >
          <img src={closeIcon} alt="" />
        </button>
      )}
    </div>
  );
});
