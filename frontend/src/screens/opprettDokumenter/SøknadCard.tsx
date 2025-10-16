import styles from "./OpprettDokumenter.module.css";
import addDays from "date-fns/addDays";
import format from "date-fns/format";
import {Card} from "../../components/Card";
import {Checkbox} from "../../components/Checkbox";
import {FormInput} from "../../components/FormInput";
import React from "react";
import {useFormContext} from "react-hook-form";
import {
    validateArbeidsgrad,
    validateInntekt,
    validateOptionalOrganisasjonsnummer,
    validateSykdomsgrad
} from "../formValidation";
import {endOfMonth, subMonths} from "date-fns";
import {ArbeidssituasjonDTO} from "../../utils/types";
import {FormSelect} from "../../components/FormSelect";


const formatDateString = (date: Date): string => format(date, "yyyy-MM-dd");

const nextDay = (date: Date): Date => addDays(date, 1);

export const SøknadCard = React.memo(() => {
    const {watch, register, formState, setValue} = useFormContext();

    const sykdomFom = watch("sykdomFom");
    const sykdomTom = watch("sykdomTom");
    const skalSendeSykmelding = watch("skalSendeSykmelding");

    const arbeidssituasjon: ArbeidssituasjonDTO = watch("arbeidssituasjon")
    const skalViseTidligereArbeidsgiverOrgnummer = arbeidssituasjon == 'ARBEIDSLEDIG'

    const defaultDate = format(addDays(endOfMonth(subMonths(new Date(), 3)), 1), "yyyy-MM-dd")

    return (
        <Card >
            <h2 className={styles.Title}>Søknad</h2>
            <div className={styles.CardContainer}>
                {skalViseTidligereArbeidsgiverOrgnummer && <FormInput
                    data-testid="tidligereArbeidsgiverOrgnummer"
                    label="Tidligere arbeidsgiver sitt orgnummer"
                    errors={formState.errors}
                    {...register("søknad.tidligereArbeidsgiverOrgnummer", {
                        validate: validateOptionalOrganisasjonsnummer,
                        shouldUnregister: true
                    })}
                />}
                <FormInput
                    data-testid="sendtNav"
                    label="Søknad sendt Nav"
                    type="date"
                    errors={formState.errors}
                    defaultValue={
                        sykdomTom
                            ? formatDateString(nextDay(new Date(sykdomTom)))
                            : defaultDate
                    }
                    {...register("søknad.sendtNav")}
                />
                <FormInput
                    label="Søknad sendt arbeidsgiver"
                    type="date"
                    errors={formState.errors}
                    {...register("søknad.sendtArbeidsgiver")}
                />
                <FormInput
                    label="Arbeid gjenopptatt"
                    type="date"
                    errors={formState.errors}
                    {...register("søknad.arbeidGjenopptatt", {
                        required: false,
                        validate: (value?: string): boolean | string =>
                            value ? ((new Date(sykdomFom) <= new Date(value) && new Date(sykdomTom) >= new Date(value)) || 'Arbeid gjenopptatt kan ikke være eldre enn sykdomFom, eller nyere enn sykdomTom') : true,
                    })}
                />
                <FormInput
                    data-testid="faktiskgrad"
                    label="Faktisk arbeidsgrad"
                    errors={formState.errors}
                    {...register("søknad.faktiskgrad", {
                        required: false,
                        validate: validateArbeidsgrad,
                    })}
                />
                {(arbeidssituasjon === 'SELVSTENDIG_NARINGSDRIVENDE' || arbeidssituasjon === 'BARNEPASSER') && <>
                    <FormInput
                        label="Årsinntekt fra Sigrun"
                        errors={formState.errors}
                        {...register("søknad.inntektFraSigrun", {
                            required: "Inntekt fra Sigrun må fylles ut",
                            validate: validateInntekt
                        })}
                    />
                    <FormInput
                        data-testid="ventetidFom"
                        label="Ventetid fom"
                        type="date"
                        errors={formState.errors}
                        defaultValue={
                            sykdomFom
                                ? formatDateString(new Date(sykdomFom))
                                : defaultDate
                        }
                        {...register("søknad.ventetidFom")}
                    />
                    <FormInput
                        data-testid="ventetidTom"
                        label="Ventetid tom"
                        type="date"
                        errors={formState.errors}
                        defaultValue={
                            sykdomFom
                                ? formatDateString(addDays(new Date(sykdomFom), 15))
                                : defaultDate
                        }
                        {...register("søknad.ventetidTom")}
                    />
                    <FormSelect
                        label="Fravær før sykmeldingen"
                        options={[
                            { value: "", label: "Ikke spurt om" },
                            "Ja",
                            "Nei"
                        ]}
                        {...register("søknad.fraværFørSykmeldingen")}
                        onChange={val => {
                            const verdi = val.target.options[val.target.options.selectedIndex].value
                            setValue("søknad.fraværFørSykmeldingen", verdi)
                        } }
                    />
                    <Checkbox
                        data-testid="harBrukerOppgittForsikring"
                        label="Har bruker oppgitt forsikring?"
                        defaultChecked={false}
                        errors={formState.errors}
                        {...register("søknad.harBrukerOppgittForsikring")}
                    />
                </>

                }
                <Checkbox
                    data-testid="harAndreInntektskilder"
                    label="Har andre inntektskilder"
                    errors={formState.errors}
                    {...register("søknad.harAndreInntektskilder")}
                />
                {!skalSendeSykmelding && (
                    <FormInput
                        label="Sykdomsgrad i sykmeldingen"
                        errors={formState.errors}
                        defaultValue={100}
                        {...register("søknad.sykmeldingsgrad", {
                            required: "Sykmeldingsgrad må fylles ut",
                            validate: validateSykdomsgrad,
                            shouldUnregister: true
                        })}
                    />
                )}
            </div>
        </Card>
    );
});
