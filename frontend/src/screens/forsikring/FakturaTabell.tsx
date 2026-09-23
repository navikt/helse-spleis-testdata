import React, { useState } from "react";
import {
  BodyShort,
  Button,
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

import { DatoFelt } from "../../components/DatoFelt";
import type { Forsikringsfaktura, ForsikringsfakturaPayload } from "./typer";

const TITTEL = "Fakturaer";

const halvdeler = ["1", "2"] as const;

interface Utkast {
  år: string;
  halvdel: string;
  betalingsdato: string;
}

const tilUtkast = (faktura: Forsikringsfaktura): Utkast => ({
  år: faktura.år === null ? "" : String(faktura.år),
  halvdel: faktura.halvdel === null ? "" : String(faktura.halvdel),
  betalingsdato: faktura.betalingsdato ?? "",
});

const nyttUtkast = (): Utkast => ({
  år: String(new Date().getFullYear()),
  halvdel: "1",
  betalingsdato: "",
});

const tilPayload = (utkast: Utkast): ForsikringsfakturaPayload => ({
  år: utkast.år === "" ? null : Number(utkast.år),
  halvdel: utkast.halvdel === "" ? null : Number(utkast.halvdel),
  betalingsdato: utkast.betalingsdato === "" ? null : utkast.betalingsdato,
});

// År og halvdel identifiserer fakturaen, og må være fylt ut før den kan lagres.
const manglerPåkrevdeFelter = (utkast: Utkast): boolean =>
  utkast.år.trim() === "" || utkast.halvdel.trim() === "";

interface FakturaTabellProps {
  fakturaer: Forsikringsfaktura[];
  onOpprett: (payload: ForsikringsfakturaPayload) => Promise<void>;
  onOppdater: (
    faktura: Forsikringsfaktura,
    payload: ForsikringsfakturaPayload,
  ) => Promise<void>;
  onSlett: (faktura: Forsikringsfaktura) => Promise<void>;
}

export const FakturaTabell = ({
  fakturaer,
  onOpprett,
  onOppdater,
  onSlett,
}: FakturaTabellProps) => {
  const [utkast, setUtkast] = useState<Record<number, Utkast>>({});
  const [nyRad, setNyRad] = useState<Utkast | null>(null);
  const [lagrer, setLagrer] = useState<number | "ny" | null>(null);

  const endreUtkast = (id: number, endring: Partial<Utkast>) =>
    setUtkast((forrige) => ({
      ...forrige,
      [id]: { ...forrige[id], ...endring },
    }));

  const endreNyRad = (endring: Partial<Utkast>) =>
    setNyRad((forrige) =>
      forrige === null ? forrige : { ...forrige, ...endring },
    );

  const avbrytRedigering = (id: number) =>
    setUtkast((forrige) => {
      const oppdatert = { ...forrige };
      delete oppdatert[id];
      return oppdatert;
    });

  const lagreEndring = async (faktura: Forsikringsfaktura) => {
    setLagrer(faktura.id);
    try {
      await onOppdater(faktura, tilPayload(utkast[faktura.id]));
      avbrytRedigering(faktura.id);
    } catch {
      // feilmeldingen vises av forelderen, raden blir stående i redigeringsmodus
    } finally {
      setLagrer(null);
    }
  };

  const lagreNyRad = async () => {
    if (nyRad === null) return;
    setLagrer("ny");
    try {
      await onOpprett(tilPayload(nyRad));
      setNyRad(null);
    } catch {
      // feilmeldingen vises av forelderen, raden blir stående i redigeringsmodus
    } finally {
      setLagrer(null);
    }
  };

  const slettRad = async (faktura: Forsikringsfaktura) => {
    try {
      await onSlett(faktura);
      avbrytRedigering(faktura.id);
    } catch {
      // feilmeldingen vises av forelderen
    }
  };

  const redigeringsceller = (
    radnavn: string,
    verdier: Utkast,
    endre: (endring: Partial<Utkast>) => void,
  ) => (
    <>
      <Table.DataCell>
        <TextField
          size="small"
          hideLabel
          type="number"
          htmlSize={6}
          label={`År ${radnavn}`}
          value={verdier.år}
          onChange={(event) => endre({ år: event.target.value })}
        />
      </Table.DataCell>
      <Table.DataCell>
        <Select
          size="small"
          hideLabel
          label={`Halvdel ${radnavn}`}
          value={verdier.halvdel}
          onChange={(event) => endre({ halvdel: event.target.value })}
        >
          {halvdeler.map((halvdel) => (
            <option key={halvdel} value={halvdel}>
              {halvdel}
            </option>
          ))}
        </Select>
      </Table.DataCell>
      <Table.DataCell>
        <DatoFelt
          hideLabel
          label={`Betalingsdato ${radnavn}`}
          verdi={verdier.betalingsdato}
          onEndret={(isoDato) => endre({ betalingsdato: isoDato })}
        />
      </Table.DataCell>
    </>
  );

  return (
    <VStack gap="space-16" minWidth="0" asChild>
      <section>
        <HStack gap="space-16" align="center">
          <Button
            type="button"
            variant="tertiary"
            size="small"
            icon={<PlusCircleIcon aria-hidden />}
            aria-label={`Legg til rad i ${TITTEL}`}
            title="Legg til rad"
            disabled={nyRad !== null}
            onClick={() => setNyRad(nyttUtkast())}
          />
          <Heading level="2" size="small">
            {TITTEL}
          </Heading>
        </HStack>
        <Box
          background="raised"
          borderRadius="8"
          padding="space-16"
          overflow="auto"
          minWidth="0"
        >
          <Table size="small" aria-label={TITTEL}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader scope="col" />
                <Table.ColumnHeader scope="col">År</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">Halvdel</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">
                  Betalingsdato
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {fakturaer.map((faktura) => {
                const verdier = utkast[faktura.id];
                const redigeres = verdier !== undefined;
                return (
                  <Table.Row key={faktura.id}>
                    <Table.DataCell>
                      <HStack gap="space-4" wrap={false}>
                        {redigeres ? (
                          <>
                            <Button
                              type="button"
                              variant="tertiary"
                              size="small"
                              icon={<FloppydiskIcon aria-hidden />}
                              aria-label={`Lagre rad ${faktura.id}`}
                              title="Lagre"
                              disabled={
                                lagrer === faktura.id ||
                                manglerPåkrevdeFelter(verdier)
                              }
                              onClick={() => void lagreEndring(faktura)}
                            />
                            <Button
                              type="button"
                              variant="tertiary"
                              size="small"
                              icon={<XMarkIcon aria-hidden />}
                              aria-label={`Avbryt redigering av rad ${faktura.id}`}
                              title="Avbryt"
                              onClick={() => avbrytRedigering(faktura.id)}
                            />
                          </>
                        ) : (
                          <Button
                            type="button"
                            variant="tertiary"
                            size="small"
                            icon={<PencilIcon aria-hidden />}
                            aria-label={`Rediger rad ${faktura.id}`}
                            title="Rediger"
                            onClick={() =>
                              setUtkast((forrige) => ({
                                ...forrige,
                                [faktura.id]: tilUtkast(faktura),
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
                          aria-label={`Slett rad ${faktura.id}`}
                          title="Slett"
                          onClick={() => void slettRad(faktura)}
                        />
                      </HStack>
                    </Table.DataCell>
                    {redigeres ? (
                      redigeringsceller(
                        `rad ${faktura.id}`,
                        verdier,
                        (endring) => endreUtkast(faktura.id, endring),
                      )
                    ) : (
                      <>
                        <Table.DataCell>{faktura.år ?? ""}</Table.DataCell>
                        <Table.DataCell>{faktura.halvdel ?? ""}</Table.DataCell>
                        <Table.DataCell>
                          {faktura.betalingsdato ?? ""}
                        </Table.DataCell>
                      </>
                    )}
                  </Table.Row>
                );
              })}
              {nyRad !== null && (
                <Table.Row selected>
                  <Table.DataCell>
                    <HStack gap="space-4" wrap={false}>
                      <Button
                        type="button"
                        variant="tertiary"
                        size="small"
                        icon={<FloppydiskIcon aria-hidden />}
                        aria-label={`Lagre ny rad i ${TITTEL}`}
                        title="Lagre"
                        disabled={
                          lagrer === "ny" || manglerPåkrevdeFelter(nyRad)
                        }
                        onClick={() => void lagreNyRad()}
                      />
                      <Button
                        type="button"
                        variant="tertiary"
                        size="small"
                        icon={<XMarkIcon aria-hidden />}
                        aria-label={`Avbryt ny rad i ${TITTEL}`}
                        title="Avbryt"
                        onClick={() => setNyRad(null)}
                      />
                    </HStack>
                  </Table.DataCell>
                  {redigeringsceller("ny rad", nyRad, endreNyRad)}
                </Table.Row>
              )}
              {fakturaer.length === 0 && nyRad === null && (
                <Table.Row>
                  <Table.DataCell colSpan={4}>
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
