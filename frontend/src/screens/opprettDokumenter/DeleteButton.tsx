import styles from "./OpprettDokumenter.module.css";
import { useFormContext } from "react-hook-form";
import React, { useEffect, useState } from "react";
import { del } from "../../io/api";
import { Spinner } from "../../components/Spinner";
import classNames from "classnames";

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

  useEffect(() => {
    if (status !== undefined)
      setTimeout(() => {
        setStatus(undefined);
      }, 3000);
  }, [status]);

  const ignorer = (event: React.SyntheticEvent) =>
    (event as React.KeyboardEvent).key !== "Enter" &&
    (event as React.KeyboardEvent).key !== " " &&
    (event as React.MouseEvent).button !== 0;

  const slettPerson = async (event: React.SyntheticEvent) => {
    let fnr = getValues("fnr");
    if (fnr.length !== 11 || ignorer(event)) return;

    event.preventDefault();
    setIsFetching(true);
    await del("/person", { ident: fnr })
      .then((res) => setStatus(res.status))
      .catch((error) => {
        setStatus(error.status ?? 404);
        return error;
      })
      .finally(() => setIsFetching(false));
  };

  useEffect(() => {
    let errorMessage =
      status && status !== 200 ? "Sletting av person feilet" : undefined;
    errorCallback(errorMessage);
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
