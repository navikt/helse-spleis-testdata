import React from "react";
import { Button } from "@navikt/ds-react";
import { PlusCircleIcon } from "@navikt/aksel-icons";

interface AddCardButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const AddButton: React.FC<AddCardButtonProps> = ({
  children,
  ...rest
}) => (
  <Button
    variant="tertiary"
    size="small"
    type="button"
    icon={<PlusCircleIcon aria-hidden />}
    {...rest}
  >
    {children}
  </Button>
);
