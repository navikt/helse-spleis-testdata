import React from "react";
import { Button } from "@navikt/ds-react";
import { CheckmarkCircleIcon, XMarkOctagonIcon } from "@navikt/aksel-icons";
import { Spinner } from "./Spinner";

const error = (status?: number): boolean =>
  status !== undefined && status !== null && status >= 400;

const success = (status?: number): boolean =>
  status !== undefined && status !== null && status < 400;

interface FetchButtonProps extends Omit<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  "color"
> {
  isFetching: boolean;
  status?: number;
}

const ikon = (isFetching: boolean, status?: number) => {
  if (isFetching) return <Spinner />;
  if (success(status))
    return <CheckmarkCircleIcon data-testid="success" aria-hidden />;
  if (error(status))
    return <XMarkOctagonIcon data-testid="error" aria-hidden />;
  return undefined;
};

export const FetchButton: React.FC<FetchButtonProps> = ({
  isFetching,
  status,
  children,
  ...rest
}) => (
  <Button
    data-color={
      error(status) ? "danger" : success(status) ? "success" : undefined
    }
    icon={ikon(isFetching, status)}
    iconPosition="right"
    {...rest}
  >
    {children}
  </Button>
);
