import React, { useState } from "react";
import { useFormContext } from "react-hook-form";
import { nanoid } from "nanoid";
import { Button } from "@navikt/ds-react";

import { del } from "../../io/api";
import { useAddSystemMessage } from "../../state/useSystemMessages";

export const DeleteButton = ({
  errorCallback,
}: {
  errorCallback: (feilet: string | null) => void;
}) => {
  const { getValues } = useFormContext();
  const [isFetching, setIsFetching] = useState<boolean>(false);
  const addMessage = useAddSystemMessage();

  const slettPerson = async () => {
    const fnr = getValues("fnr");
    if (fnr.length !== 11) {
      oppdaterFeilmeldingstekst(`Må være elleve tegn for å kunne slette`);
      return;
    }

    setIsFetching(true);
    await del("/person", { ident: fnr })
      .then((res) => {
        if (res.status !== 200) {
          oppdaterFeilmeldingstekst("Sletting av person feilet mot backend");
        } else errorCallback(null);

        if (res.ok)
          addMessage({
            id: nanoid(),
            text: "Sletting sendt",
            timeToLiveMs: 4000,
          });
      })
      .catch((error) => {
        oppdaterFeilmeldingstekst("Sletting av person feilet");
        return error;
      })
      .finally(() => setIsFetching(false));
  };

  function oppdaterFeilmeldingstekst(melding: string) {
    errorCallback(melding);
    setTimeout(() => errorCallback(null), 3000);
  }


  return (
    <Button
      type="button"
      variant="secondary"
      data-color="neutral"
      size="small"
      onClick={slettPerson}
    >
      {isFetching ? "Sletter" : "Slett"}
    </Button>
  );
};
