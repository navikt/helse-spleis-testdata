import React, { useEffect, useState } from "react";
import {
  BodyShort,
  Button,
  Checkbox,
  Heading,
  HStack,
  Select,
  Table,
  TextField,
  VStack,
} from "@navikt/ds-react";
import { Box } from "@navikt/ds-react/Box";
import {
  FloppydiskIcon,
  PencilIcon,
  PlusCircleIcon,
  TrashIcon,
  XMarkIcon,
} from "@navikt/aksel-icons";

import styles from "./RedigerbarTabell.module.css";
import { DatoFelt } from "../../components/DatoFelt";
import {
  manglerPåkrevdeFelter,
  tilPayload,
  tilUtkast,
  visVerdi,
  type Kolonne,
  type Utkast,
} from "./kolonner";

const NY_RAD = "ny-rad";

interface RedigerbarTabellProps<T> {
  tittel: string;
  kolonner: Kolonne<T>[];
  rader: T[];
  radId: (rad: T) => number;
  valgtId?: number | null;
  onVelg?: (rad: T) => void;
  onOpprett: (verdier: Record<string, unknown>) => Promise<void>;
  onOppdater: (rad: T, verdier: Record<string, unknown>) => Promise<void>;
  onSlett: (rad: T) => Promise<void>;
  nyRadVerdier: () => Utkast;
  /** Settes når «legg til»-modus styres utenfra. Da vises ikke tabellens egen knapp. */
  nyRadÅpen?: boolean;
  onNyRadÅpenEndret?: (åpen: boolean) => void;
}

export const RedigerbarTabell = <T,>({
  tittel,
  kolonner,
  rader,
  radId,
  valgtId,
  onVelg,
  onOpprett,
  onOppdater,
  onSlett,
  nyRadVerdier,
  nyRadÅpen,
  onNyRadÅpenEndret,
}: RedigerbarTabellProps<T>) => {
  const [utkast, setUtkast] = useState<Record<string, Utkast>>({});
  const [nyRad, setNyRad] = useState<Utkast | null>(null);
  const [lagrer, setLagrer] = useState<string | null>(null);

  const styresUtenfra = onNyRadÅpenEndret !== undefined;

  useEffect(() => {
    if (!styresUtenfra) return;
    if (nyRadÅpen === true && nyRad === null) setNyRad(nyRadVerdier());
    if (nyRadÅpen !== true && nyRad !== null) setNyRad(null);
    // holder den interne kladden i takt med den eksterne av/på-bryteren
  }, [styresUtenfra, nyRadÅpen]);

  const åpneNyRad = () => {
    setNyRad(nyRadVerdier());
    onNyRadÅpenEndret?.(true);
  };

  const lukkNyRad = () => {
    setNyRad(null);
    onNyRadÅpenEndret?.(false);
  };

  const oppdaterFelt = (nøkkel: string, felt: string, verdi: string) => {
    if (nøkkel === NY_RAD) {
      setNyRad((forrige) =>
        forrige === null ? forrige : { ...forrige, [felt]: verdi },
      );
    } else {
      setUtkast((forrige) => ({
        ...forrige,
        [nøkkel]: { ...forrige[nøkkel], [felt]: verdi },
      }));
    }
  };

  const avbrytRedigering = (nøkkel: string) => {
    setUtkast((forrige) => {
      const oppdatert = { ...forrige };
      delete oppdatert[nøkkel];
      return oppdatert;
    });
  };

  const lagreEndring = async (rad: T) => {
    const nøkkel = String(radId(rad));
    setLagrer(nøkkel);
    try {
      await onOppdater(rad, tilPayload(kolonner, utkast[nøkkel]));
      avbrytRedigering(nøkkel);
    } catch {
      // feilmeldingen vises av forelderen, raden blir stående i redigeringsmodus
    } finally {
      setLagrer(null);
    }
  };

  const lagreNyRad = async () => {
    if (nyRad === null) return;
    setLagrer(NY_RAD);
    try {
      await onOpprett(tilPayload(kolonner, nyRad));
      lukkNyRad();
    } catch {
      // feilmeldingen vises av forelderen, raden blir stående i redigeringsmodus
    } finally {
      setLagrer(null);
    }
  };

  const slettRad = async (rad: T) => {
    try {
      await onSlett(rad);
      avbrytRedigering(String(radId(rad)));
    } catch {
      // feilmeldingen vises av forelderen
    }
  };

  const cellerForUtkast = (
    nøkkel: string,
    verdier: Utkast,
    erNyRad: boolean,
  ) => {
    const radnavn = erNyRad ? "ny rad" : `rad ${nøkkel}`;
    return kolonner.map((kolonne) => (
      <Table.DataCell key={kolonne.key}>
        {kolonne.type === "boolean" ? (
          <Checkbox
            size="small"
            hideLabel
            checked={verdier[kolonne.key] === "true"}
            onChange={(event) =>
              oppdaterFelt(nøkkel, kolonne.key, String(event.target.checked))
            }
          >
            {`${kolonne.tittel} ${radnavn}`}
          </Checkbox>
        ) : kolonne.type === "select" ? (
          <Select
            size="small"
            hideLabel
            label={`${kolonne.tittel} ${radnavn}`}
            className={styles.Felt}
            value={verdier[kolonne.key] ?? ""}
            onChange={(event) =>
              oppdaterFelt(nøkkel, kolonne.key, event.target.value)
            }
          >
            {!kolonne.påkrevd && <option value="" />}
            {kolonne.valg?.map((valg) => (
              <option key={valg.verdi} value={valg.verdi}>
                {valg.tekst}
              </option>
            ))}
          </Select>
        ) : kolonne.type === "date" ? (
          <DatoFelt
            hideLabel
            label={`${kolonne.tittel} ${radnavn}`}
            className={styles.Felt}
            verdi={verdier[kolonne.key] ?? ""}
            onEndret={(isoDato) => oppdaterFelt(nøkkel, kolonne.key, isoDato)}
          />
        ) : (
          <TextField
            size="small"
            hideLabel
            label={`${kolonne.tittel} ${radnavn}`}
            className={styles.Felt}
            type={kolonne.type === "number" ? "number" : "text"}
            value={verdier[kolonne.key] ?? ""}
            onChange={(event) =>
              oppdaterFelt(nøkkel, kolonne.key, event.target.value)
            }
          />
        )}
      </Table.DataCell>
    ));
  };

  return (
    <VStack gap="space-16" minWidth="0" asChild>
      <section>
        <HStack gap="space-16" align="center">
          {!styresUtenfra && (
            <Button
              type="button"
              variant="tertiary"
              size="small"
              icon={<PlusCircleIcon aria-hidden />}
              aria-label={`Legg til rad i ${tittel}`}
              title="Legg til rad"
              disabled={nyRad !== null}
              onClick={åpneNyRad}
            />
          )}
          <Heading level="2" size="small">
            {tittel}
          </Heading>
        </HStack>
        <Box
          background="raised"
          borderRadius="8"
          padding="space-16"
          overflow="auto"
          minWidth="0"
        >
          <Table size="small" aria-label={tittel}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader scope="col" />
                {kolonner.map((kolonne) => (
                  <Table.ColumnHeader scope="col" key={kolonne.key}>
                    {kolonne.tittel}
                  </Table.ColumnHeader>
                ))}
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {rader.map((rad) => {
                const nøkkel = String(radId(rad));
                const verdier = utkast[nøkkel];
                const redigeres = verdier !== undefined;
                const erValgt = valgtId !== undefined && valgtId === radId(rad);
                const velg = onVelg ? () => onVelg(rad) : undefined;
                return (
                  <Table.Row
                    key={nøkkel}
                    selected={erValgt}
                    className={onVelg ? styles.Velgbar : undefined}
                    aria-current={erValgt ? "true" : undefined}
                    tabIndex={onVelg ? 0 : undefined}
                    onClick={velg}
                    onKeyDown={(event) => {
                      if (velg === undefined) return;
                      if (event.key !== "Enter" && event.key !== " ") return;
                      event.preventDefault();
                      velg();
                    }}
                  >
                    <Table.DataCell className={styles.Handlinger}>
                      {redigeres ? (
                        <>
                          <Button
                            type="button"
                            variant="tertiary"
                            size="small"
                            icon={<FloppydiskIcon aria-hidden />}
                            aria-label={`Lagre rad ${nøkkel}`}
                            title="Lagre"
                            disabled={
                              lagrer === nøkkel ||
                              manglerPåkrevdeFelter(kolonner, verdier)
                            }
                            onClick={() => void lagreEndring(rad)}
                          />
                          <Button
                            type="button"
                            variant="tertiary"
                            size="small"
                            icon={<XMarkIcon aria-hidden />}
                            aria-label={`Avbryt redigering av rad ${nøkkel}`}
                            title="Avbryt"
                            onClick={() => avbrytRedigering(nøkkel)}
                          />
                        </>
                      ) : (
                        <Button
                          type="button"
                          variant="tertiary"
                          size="small"
                          icon={<PencilIcon aria-hidden />}
                          aria-label={`Rediger rad ${nøkkel}`}
                          title="Rediger"
                          onClick={() =>
                            setUtkast((forrige) => ({
                              ...forrige,
                              [nøkkel]: tilUtkast(kolonner, rad),
                            }))
                          }
                        />
                      )}
                      <Button
                        type="button"
                        variant="tertiary"
                        data-color="danger"
                        size="small"
                        icon={<TrashIcon aria-hidden />}
                        aria-label={`Slett rad ${nøkkel}`}
                        title="Slett"
                        onClick={() => void slettRad(rad)}
                      />
                    </Table.DataCell>
                    {redigeres
                      ? cellerForUtkast(nøkkel, verdier, false)
                      : kolonner.map((kolonne) => (
                          <Table.DataCell key={kolonne.key}>
                            {visVerdi(kolonne, rad)}
                          </Table.DataCell>
                        ))}
                  </Table.Row>
                );
              })}
              {nyRad !== null && (
                <Table.Row selected>
                  <Table.DataCell className={styles.Handlinger}>
                    <Button
                      type="button"
                      variant="tertiary"
                      size="small"
                      icon={<FloppydiskIcon aria-hidden />}
                      aria-label={`Lagre ny rad i ${tittel}`}
                      title="Lagre"
                      disabled={
                        lagrer === NY_RAD ||
                        manglerPåkrevdeFelter(kolonner, nyRad)
                      }
                      onClick={() => void lagreNyRad()}
                    />
                    <Button
                      type="button"
                      variant="tertiary"
                      size="small"
                      icon={<XMarkIcon aria-hidden />}
                      aria-label={`Avbryt ny rad i ${tittel}`}
                      title="Avbryt"
                      onClick={lukkNyRad}
                    />
                  </Table.DataCell>
                  {cellerForUtkast(NY_RAD, nyRad, true)}
                </Table.Row>
              )}
              {rader.length === 0 && nyRad === null && (
                <Table.Row>
                  <Table.DataCell colSpan={kolonner.length + 1}>
                    <BodyShort size="small" textColor="subtle">
                      Ingen rader
                    </BodyShort>
                  </Table.DataCell>
                </Table.Row>
              )}
            </Table.Body>
          </Table>
        </Box>
      </section>
    </VStack>
  );
};
