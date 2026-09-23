import React from "react";
import { Button } from "@navikt/ds-react";
import { TrashIcon } from "@navikt/aksel-icons";

interface DeleteButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const DeleteButton: React.FC<DeleteButtonProps> = ({
  children,
  ...rest
}) => (
  <Button
    variant="tertiary"
    data-color="danger"
    size="small"
    type="button"
    icon={<TrashIcon aria-hidden />}
    aria-label={children === undefined ? "Slett" : undefined}
    {...rest}
  >
    {children}
  </Button>
);
