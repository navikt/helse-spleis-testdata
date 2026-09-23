import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import { format, startOfMonth, subMonths } from "date-fns";
import { Heading, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { FormDatePicker } from "../../components/FormDatePicker";
import { FormSelect } from "../../components/FormSelect";
import { Checkbox } from "../../components/Checkbox";
import { get } from "../../io/api";
import {
  validateFødselsnummer,
  validateInntekt,
  validateOrganisasjonsnummer,
  validateRefusjonsbeløp,
} from "../formValidation";

const useUnregisterInntektsmeldingCard = () => {
  const { unregister } = useFormContext();
  useEffect(() => {
    return () => {
      unregister("inntektsmelding");
    };
  }, []);
};

const useFetchInntekt = () => {
  const { watch, setValue, clearErrors } = useFormContext();
  const fødselsnummer = watch("fnr");
  const orgnummer = watch("orgnummer");
  const [alleInntekter, setAlleInntekter] = useState<
    Record<string, Record<string, number>>
  >({});

  useEffect(() => {
    if (
      validateFødselsnummer(fødselsnummer) === true &&
      alleInntekter[fødselsnummer] === undefined
    ) {
      get("/person/inntekt", { ident: fødselsnummer })
        .then(async (result) => {
          const response = await result.json();
          const inntekterForFnr = response.arbeidsgivere.reduce(
            (
              acc: Record<string, number>,
              ag: {
                organisasjonsnummer: string;
                beregnetMånedsinntekt: number;
              },
            ) => {
              acc[ag.organisasjonsnummer] = ag.beregnetMånedsinntekt;
              return acc;
            },
            {} as Record<string, number>,
          );

          setAlleInntekter((previous) => ({
            ...previous,
            [fødselsnummer]: inntekterForFnr,
          }));
        })
        .catch((error) => console.log(error));
    }
  }, [fødselsnummer]);

  useEffect(() => {
    if (
      !validateFødselsnummer(fødselsnummer) ||
      !validateOrganisasjonsnummer(orgnummer)
    )
      return;

    const beregnetMånedsinntekt = alleInntekter[fødselsnummer]?.[orgnummer];
    if (beregnetMånedsinntekt === undefined) return;

    clearErrors("inntektsmelding.inntekt");
    clearErrors("inntektsmelding.refusjonsbeløp");
    setValue("inntektsmelding.inntekt", String(beregnetMånedsinntekt));
    setValue("inntektsmelding.refusjonsbeløp", String(beregnetMånedsinntekt));
  }, [alleInntekter, fødselsnummer, orgnummer]);
};

export const InntektsmeldingCard = React.memo(() => {
  const { register, formState, setValue } = useFormContext();

  useUnregisterInntektsmeldingCard();
  useFetchInntekt();

  const defaultDate = format(
    startOfMonth(subMonths(new Date(), 3)),
    "yyyy-MM-dd",
  );

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Inntektsmelding
        </Heading>
        <FormDatePicker
          data-testid="førsteFraværsdag"
          label="Første fraværsdag"
          name="inntektsmelding.førsteFraværsdag"
          defaultValue={defaultDate}
          rules={{ required: "Første fraværsdag må angis" }}
        />
        <FormDatePicker
          data-testid="opphørRefusjon"
          label="Siste dag med refusjon"
          name="inntektsmelding.opphørRefusjon"
        />
        <FormInput
          data-testid="inntekt"
          label="Inntekt"
          errors={formState.errors}
          {...register("inntektsmelding.inntekt", {
            required: "Inntekt må angis",
            validate: validateInntekt,
          })}
        />
        <FormInput
          data-testid="refusjonsbeløp"
          label="Refusjonsbeløp"
          errors={formState.errors}
          {...register("inntektsmelding.refusjonsbeløp", {
            validate: validateRefusjonsbeløp,
          })}
        />
        <FormSelect
          label="Begrunnelse for reduksjon"
          options={[
            { value: "", label: "(Ingen)" },
            "LovligFravaer",
            "FravaerUtenGyldigGrunn",
            "ArbeidOpphoert",
            "BeskjedGittForSent",
            "ManglerOpptjening",
            "IkkeLoenn",
            "BetvilerArbeidsufoerhet",
            "IkkeFravaer",
            "StreikEllerLockout",
            "Permittering",
            "FiskerMedHyre",
            "Saerregler",
            "FerieEllerAvspasering",
            "IkkeFullStillingsandel",
            "TidligereVirksomhet",
          ]}
          {...register(
            "inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt",
          )}
          onChange={(val) => {
            const verdi =
              val.target.options[val.target.options.selectedIndex].value;
            setValue(
              "inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt",
              verdi,
            );
          }}
        />
        <Checkbox
          data-testid="harOpphørAvNaturalytelser"
          label="Har opphør av naturalytelser"
          errors={formState.errors}
          {...register("inntektsmelding.harOpphørAvNaturalytelser")}
        />
      </VStack>
    </Card>
  );
});
