import styles from "./OpprettDokumenter.module.css";
import { useFormContext } from "react-hook-form";
import React, { useEffect, useState } from "react";
import { del } from "../../io/api";
import { Spinner } from "../../components/Spinner";
import classNames from "classnames";
import {nanoid} from "nanoid";
import {useAddSystemMessage} from "../../state/useSystemMessages";

const error = (status?: number): boolean =>
  status !== undefined && status !== null && status >= 400;

const success = (status?: number): boolean =>
  status !== undefined && status !== null && status < 400;

export const DeleteButton = ({
  errorCallback,
}: {
  errorCallback: (feilet: string) => void;
}) => {
  const { getValues } = useFormContext();
  const [isFetching, setIsFetching] = useState<boolean>(false);
  const [status, setStatus] = useState<number>();
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    if (status !== undefined)
      setTimeout(() => setStatus(undefined), 3000);
  }, [status]);

  const ignorer = (event: React.SyntheticEvent) =>
    (event as React.KeyboardEvent).key !== "Enter" &&
    (event as React.KeyboardEvent).key !== " " &&
    (event as React.MouseEvent).button !== 0;

  const slettPerson = async (event: React.SyntheticEvent) => {
    let fnr = getValues("fnr");
    if (ignorer(event)) return
    if (fnr.length !== 11) {
      errorCallback(`Kan ikke slette! ${fnr} er ikke nøyaktig elleve tegn langt!`)
      return;
    }

    event.preventDefault();
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
    if (status != null && status !== 200) errorCallback("Sletting av person feilet")
    else errorCallback(null)
  }, [status]);

  return (
    <span
      tabIndex={0}
      role={"button"}
      className={classNames(
        styles.SlettPersonButton,
        error(status) && styles.error
      )}
      onClick={slettPerson}
      onKeyDown={slettPerson}
    >
      {isFetching ? (
        <Spinner />
      ) : error(status) ? (
        "☠️"
      ) : success(status) ? (
        "✔️️"
      ) : (
        "❌"
      )}
    </span>
  );
};
