import React from "react";
import { Loader } from "@navikt/ds-react";

export const Spinner = () => (
  <Loader size="small" data-testid="spinner" title="Laster" />
);
