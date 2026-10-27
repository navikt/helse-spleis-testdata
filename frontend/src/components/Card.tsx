import React from "react";
import { Box } from "@navikt/ds-react/Box";

import styles from "./Card.module.css";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {}

export const Card: React.FC<CardProps> = ({ children, className, ...rest }) => (
  <Box
    background="raised"
    borderRadius="8"
    padding="space-24"
    width="max-content"
    className={className ? `${styles.Card} ${className}` : styles.Card}
    {...rest}
  >
    {children}
  </Box>
);
