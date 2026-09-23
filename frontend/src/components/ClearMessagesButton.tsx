import React from "react";
import { Button } from "@navikt/ds-react";
import { useSystemMessages } from "../state/useSystemMessages";
import styles from "./ClearMessagesButton.module.css";

export const ClearMessagesButton: React.FC = () => {
  const [, { clearMessages }] = useSystemMessages();
  return (
    <Button
      type="button"
      variant="secondary"
      size="small"
      className={styles.ClearMessagesButton}
      onClick={clearMessages}
    >
      Fjern alle meldinger
    </Button>
  );
};
