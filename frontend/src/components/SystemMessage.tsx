import React, { useEffect } from "react";
import { LocalAlert } from "@navikt/ds-react";
import { useRemoveSystemMessage } from "../state/useSystemMessages";

export const SystemMessageInitializationError = () =>
  Error(
    "Kan ikke opprette systemmelding som hverken lukkes automatisk eller kan lukkes av bruker",
  );

interface SystemMessageProps
  extends
    SystemMessageObject,
    Omit<React.HTMLAttributes<HTMLDivElement>, "id" | "data-color"> {}

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
    <LocalAlert status="announcement" as="div" size="small" ref={ref} {...rest}>
      <LocalAlert.Header>
        <LocalAlert.Title as="div">{text}</LocalAlert.Title>
        {dismissable && (
          <LocalAlert.CloseButton onClick={() => removeMessage(id)} />
        )}
      </LocalAlert.Header>
    </LocalAlert>
  );
});
