import React from "react";
import { Box } from "@navikt/ds-react/Box";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {}

export const Card: React.FC<CardProps> = ({ children, ...rest }) => (
  <Box
    background="raised"
    borderRadius="8"
    padding="space-24"
    width="max-content"
    {...rest}
  >
    {children}
  </Box>
);
