import styles from "./ClearMessagesButton.module.css";
import classNames from "classnames";
import React from "react";
import { useSystemMessages } from "../state/useSystemMessages";

export const ClearMessagesButton: React.FC = () => {
  const [, { clearMessages }] = useSystemMessages();
  return (
    <button
      className={classNames(styles.ClearMessagesButton)}
      onClick={clearMessages}
    >
      Fjern alle meldinger
    </button>
  );
};
