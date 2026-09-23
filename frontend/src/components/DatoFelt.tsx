import React, { useEffect, useRef } from "react";
import { DatePicker, useDatepicker } from "@navikt/ds-react";
import { format, isValid, parse } from "date-fns";

const ISO_FORMAT = "yyyy-MM-dd";

/**
 * Datepicker viser dd.mm.yyyy, mens skjematilstanden og API-et bruker ISO.
 * All konvertering mellom de to representasjonene skjer her.
 */
export const tilIsoDato = (dato: Date | undefined): string =>
  dato === undefined || !isValid(dato) ? "" : format(dato, ISO_FORMAT);

export const fraIsoDato = (iso: string | undefined): Date | undefined => {
  if (!iso) return undefined;
  const dato = parse(iso, ISO_FORMAT, new Date());
  return isValid(dato) ? dato : undefined;
};

interface DatoFeltProps {
  label: string;
  /** ISO-dato (yyyy-MM-dd), eller tom streng når ingen dato er valgt. */
  verdi: string;
  /** Kalles med ISO-dato, eller tom streng når feltet ikke inneholder en gyldig dato. */
  onEndret: (isoDato: string) => void;
  name?: string;
  error?: string;
  hideLabel?: boolean;
  disabled?: boolean;
  className?: string;
  onBlur?: () => void;
  "data-testid"?: string;
}

export const DatoFelt = React.forwardRef<HTMLInputElement, DatoFeltProps>(
  (
    {
      label,
      verdi,
      onEndret,
      name,
      error,
      hideLabel,
      disabled,
      className,
      onBlur,
      "data-testid": dataTestId,
    },
    ref,
  ) => {
    // Skiller endringer feltet selv har gjort fra endringer som kommer utenfra.
    const egenVerdi = useRef(verdi);

    const { datepickerProps, inputProps, setSelected } = useDatepicker({
      defaultSelected: fraIsoDato(verdi),
      onDateChange: (dato) => {
        const iso = tilIsoDato(dato);
        egenVerdi.current = iso;
        onEndret(iso);
      },
    });

    useEffect(() => {
      if (verdi === egenVerdi.current) return;
      egenVerdi.current = verdi;
      setSelected(fraIsoDato(verdi));
    }, [verdi]);

    return (
      <DatePicker {...datepickerProps}>
        <DatePicker.Input
          {...inputProps}
          label={label}
          size="small"
          hideLabel={hideLabel}
          disabled={disabled}
          className={className}
          name={name}
          data-testid={dataTestId}
          error={error}
          ref={ref}
          onBlur={(event) => {
            inputProps.onBlur?.(event);
            onBlur?.();
          }}
        />
      </DatePicker>
    );
  },
);
