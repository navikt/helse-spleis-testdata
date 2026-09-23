import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import { nanoid } from "nanoid";
import { Button } from "@navikt/ds-react";

import { del } from "../../io/api";
import { Spinner } from "../../components/Spinner";
import { useAddSystemMessage } from "../../state/useSystemMessages";

const error = (status?: number): boolean =>
  status !== undefined && status !== null && status >= 400;

const success = (status?: number): boolean =>
  status !== undefined && status !== null && status < 400;

export const DeleteButton = ({
  errorCallback,
}: {
  errorCallback: (feilet: string | null) => void;
}) => {
  const { getValues } = useFormContext();
  const [isFetching, setIsFetching] = useState<boolean>(false);
  const [status, setStatus] = useState<number>();
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    if (status !== undefined) setTimeout(() => setStatus(undefined), 3000);
  }, [status]);

  const slettPerson = async () => {
    const fnr = getValues("fnr");
    if (fnr.length !== 11) {
      errorCallback(
        `Kan ikke slette! ${fnr} er ikke nøyaktig elleve tegn langt!`,
      );
      return;
    }

    setIsFetching(true);
    await del("/person", { ident: fnr })
      .then((res) => {
        setStatus(res.status);
        if (res.ok)
          addMessage({
            id: nanoid(),
            text: "Sletting sendt",
            timeToLiveMs: 4000,
          });
      })
      .catch((error) => {
        setStatus(error.status ?? 404);
        return error;
      })
      .finally(() => setIsFetching(false));
  };

  useEffect(() => {
    if (status != null && status !== 200)
      errorCallback("Sletting av person feilet");
    else errorCallback(null);
  }, [status]);

  const innhold = () => {
    if (isFetching) return <Spinner />;
    if (error(status)) return "☠️";
    if (success(status)) return "✔️️";
    return "❌";
  };

  return (
    <Button
      type="button"
      variant="secondary"
      data-color="neutral"
      size="small"
      aria-label="Slett person"
      onClick={slettPerson}
    >
      {innhold()}
    </Button>
  );
};
