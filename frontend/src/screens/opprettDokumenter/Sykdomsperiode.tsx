import React, { useState } from "react";
import { useController, useFormContext } from "react-hook-form";
import { addDays, endOfMonth, format, startOfMonth, subMonths } from "date-fns";
import { DatePicker, ErrorMessage, HStack, useRangeDatepicker } from "@navikt/ds-react";

function tilLocalDate(date: Date) {
    return format(date, "yyyy-MM-dd",);
}

const startverdier = {
    fom: tilLocalDate(startOfMonth(subMonths(new Date(), 3))),
    tom: tilLocalDate(endOfMonth(subMonths(new Date(), 3))),
}

export const Sykdomsperiode = () => {
    const { control, setValue } = useFormContext();
    const { field } = useController({ name: 'sykdom', control, defaultValue: startverdier });
    const [valideringsfeil, setValideringsfeil] = useState<string | null>(null)

    const oppdaterSendtNav = (date: Date) => {
        const dagenEtter = format(addDays(date, 1), "yyyy-MM-dd")
        setValue("søknad.sendtNav", dagenEtter);
    };
    const oppdaterFørsteFraværsdag = (date: Date) => {
        setValue("inntektsmelding.førsteFraværsdag", format(date, "yyyy-MM-dd"))
    }
    const { datepickerProps, toInputProps, fromInputProps } = useRangeDatepicker({
        defaultSelected: {
            from: new Date(startverdier.fom),
            to: new Date(startverdier.tom),
        },
        onRangeChange: (range) => {
            if (range?.from)
                field.onChange({
                    fom: tilLocalDate(range.from),
                    tom: range?.to ? format(range?.to, "yyyy-MM-dd") : undefined,
                });
            if (range?.from != null) oppdaterFørsteFraværsdag(range.from)
            if (range?.to != null) oppdaterSendtNav(range.to)
        },
        onValidate: (newValidation) => {
            if (!newValidation.to.isValidDate)
                setValideringsfeil("Til-dato må være lik eller etter fra-dato")
            else setValideringsfeil(null)
        },
    });

    return (
        <DatePicker{...datepickerProps}>
            <HStack gap="space-8">
                <DatePicker.Input
                    {...fromInputProps}
                    label="Sykdom fra"
                    size="small"
                />
                <DatePicker.Input
                    {...toInputProps}
                    label="Sykdom til"
                    size="small"
                />
            </HStack>
            {valideringsfeil != null &&
                <ErrorMessage size="small">
                    {valideringsfeil}
                </ErrorMessage>
            }
        </DatePicker>
    );
};
