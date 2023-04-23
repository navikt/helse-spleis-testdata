import styles from "./OpprettDokumenter.module.css";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { get } from "../../io/api";
import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import {validateFødselsnummer, validateInntekt, validateOrganisasjonsnummer, validateRefusjonsbeløp} from "../formValidation";
import format from "date-fns/format";
import {startOfMonth, subMonths} from "date-fns";
import {FormSelect} from "../../components/FormSelect";
import {Checkbox} from "../../components/Checkbox";

const useUnregisterInntektsmeldingCard = () => {
  const { unregister } = useFormContext();
  useEffect(() => {
    return () => {
        console.log(`Avregistrerer inntektsmelding`)
      unregister("inntektsmelding");
    };
  }, []);
};

const useFetchInntekt = () => {
  const { watch, setValue, clearErrors } = useFormContext();
  const fødselsnummer = watch("fnr");
  const orgnummer = watch("orgnummer");
  const [alleInntekter, setAlleInntekter] = useState({});

  useEffect(() => {
    if (validateFødselsnummer(fødselsnummer) === true && alleInntekter[fødselsnummer] === undefined) {
      get("/person/inntekt", { ident: fødselsnummer })
        .then(async (result) => {
          const response = await result.json();
          const inntekterForFnr = response.arbeidsgivere.reduce((acc, ag) => {
            acc[ag.organisasjonsnummer] = ag.beregnetMånedsinntekt
            return acc
          }, {})

          setAlleInntekter((previous) => ({...previous, [fødselsnummer]: inntekterForFnr}))
        })
        .catch((error) => console.log(error));
    }
  }, [fødselsnummer]);

  useEffect(() => {
    if (!validateFødselsnummer(fødselsnummer) || !validateOrganisasjonsnummer(orgnummer)) return

    const beregnetMånedsinntekt = alleInntekter[fødselsnummer]?.[orgnummer]
    if (beregnetMånedsinntekt === undefined) return

    clearErrors("inntektsmelding.inntekt");
    clearErrors("inntektsmelding.refusjonsbeløp");
    setValue("inntektsmelding.inntekt", String(beregnetMånedsinntekt));
    setValue("inntektsmelding.refusjonsbeløp", String(beregnetMånedsinntekt));
  }, [alleInntekter, fødselsnummer, orgnummer])

};

export const InntektsmeldingCard = React.memo(() => {
  const { register, formState, setValue } = useFormContext();

  useUnregisterInntektsmeldingCard();
  useFetchInntekt();

    const defaultDate = format(startOfMonth(subMonths(new Date(), 3)), "yyyy-MM-dd")

    return (
    <Card>
      <h2 className={styles.Title}>Inntektsmelding</h2>
      <div className={styles.CardContainer}>
        <FormInput
          data-testid="førsteFraværsdag"
          type="date"
          label="Første fraværsdag"
          errors={formState.errors}
          defaultValue={defaultDate}
          {...register("inntektsmelding.førsteFraværsdag", {
            required: "Første fraværsdag må angis",
          })}
        />
        <FormInput
          data-testid="opphørRefusjon"
          type="date"
          label="Siste dag med refusjon"
          errors={formState.errors}
          {...register("inntektsmelding.opphørRefusjon")}
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
                "TidligereVirksomhet"
            ]}
            {...register("inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt")}
            onChange={val => {
                const verdi = val.target.options[val.target.options.selectedIndex].value
                setValue("inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt", verdi)
            } }
        />
          <Checkbox
              data-testid="harOpphørAvNaturalytelser"
              label="Har opphør av naturalytelser"
              errors={formState.errors}
              {...register("inntektsmelding.harOpphørAvNaturalytelser")}
          />
      </div>
    </Card>
  );
});
