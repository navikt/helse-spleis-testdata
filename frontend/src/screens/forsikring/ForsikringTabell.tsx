import React, { useState } from "react";
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
  ReceiptIcon,
  TrashIcon,
  XMarkIcon,
} from "@navikt/aksel-icons";

import { DatoFelt } from "../../components/DatoFelt";
import {
  individuelleForsikringstyper,
  type IndividuellForsikring,
  type IndividuellForsikringPayload,
  type IndividuellForsikringstype,
} from "./typer";

interface Utkast {
  godkjent: boolean;
  fom: string;
  virkningsdato: string;
  type: IndividuellForsikringstype | "";
  premiegrunnlag: string;
  opphørsdato: string;
  opphørsgrunn: string;
}

const iDag = (): string => new Date().toISOString().slice(0, 10);

const tomTilNull = (verdi: string): string | null =>
  verdi === "" ? null : verdi;

const tilUtkast = (forsikring: IndividuellForsikring): Utkast => ({
  godkjent: forsikring.godkjent,
  fom: forsikring.fom ?? "",
  virkningsdato: forsikring.virkningsdato ?? "",
  type: forsikring.type ?? "",
  premiegrunnlag: String(forsikring.premiegrunnlag),
  opphørsdato: forsikring.opphørsdato ?? "",
  opphørsgrunn: forsikring.opphørsgrunn ?? "",
});

const nyttUtkast = (): Utkast => ({
  godkjent: true,
  fom: iDag(),
  virkningsdato: iDag(),
  type: individuelleForsikringstyper[0].verdi,
  premiegrunnlag: "0",
  opphørsdato: "",
  opphørsgrunn: "",
});

const tilPayload = (utkast: Utkast): IndividuellForsikringPayload => ({
  godkjent: utkast.godkjent,
  fom: tomTilNull(utkast.fom),
  virkningsdato: tomTilNull(utkast.virkningsdato),
  type: utkast.type === "" ? null : utkast.type,
  premiegrunnlag:
    utkast.premiegrunnlag === "" ? null : Number(utkast.premiegrunnlag),
  opphørsdato: tomTilNull(utkast.opphørsdato),
  opphørsgrunn: tomTilNull(utkast.opphørsgrunn),
});

const visType = (type: IndividuellForsikringstype | null): string =>
  type === null
    ? ""
    : (individuelleForsikringstyper.find((valg) => valg.verdi === type)
        ?.tekst ?? type);

interface ForsikringTabellProps {
  identitetsnummer: string;
  forsikringer: IndividuellForsikring[];
  valgtId: number | null;
  onVelg: (forsikring: IndividuellForsikring) => void;
  /** «Legg til»-knappen ligger utenfor tabellen, så forelderen eier av/på-bryteren. */
  nyRadÅpen: boolean;
  onLukkNyRad: () => void;
  onOpprett: (payload: IndividuellForsikringPayload) => Promise<void>;
  onOppdater: (
    forsikring: IndividuellForsikring,
    payload: IndividuellForsikringPayload,
  ) => Promise<void>;
  onSlett: (forsikring: IndividuellForsikring) => Promise<void>;
}

export const ForsikringTabell = ({
  identitetsnummer,
  forsikringer,
  valgtId,
  onVelg,
  nyRadÅpen,
  onLukkNyRad,
  onOpprett,
  onOppdater,
  onSlett,
}: ForsikringTabellProps) => {
  const [utkast, setUtkast] = useState<Record<number, Utkast>>({});
  const [nyRad, setNyRad] = useState<Utkast | null>(null);
  const [lagrer, setLagrer] = useState<number | "ny" | null>(null);

  // Kladden følger av/på-bryteren hos forelderen, og justeres derfor under render.
  if (nyRadÅpen && nyRad === null) setNyRad(nyttUtkast());
  if (!nyRadÅpen && nyRad !== null) setNyRad(null);

  const tittel = `Individuelle forsikringer for ${identitetsnummer}`;

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

  const lagreEndring = async (forsikring: IndividuellForsikring) => {
    setLagrer(forsikring.id);
    try {
      await onOppdater(forsikring, tilPayload(utkast[forsikring.id]));
      avbrytRedigering(forsikring.id);
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
      onLukkNyRad();
    } catch {
      // feilmeldingen vises av forelderen, raden blir stående i redigeringsmodus
    } finally {
      setLagrer(null);
    }
  };

  const slettRad = async (forsikring: IndividuellForsikring) => {
    try {
      await onSlett(forsikring);
      avbrytRedigering(forsikring.id);
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
        <Checkbox
          size="small"
          hideLabel
          checked={verdier.godkjent}
          onChange={(event) => endre({ godkjent: event.target.checked })}
        >
          {`Godkjent ${radnavn}`}
        </Checkbox>
      </Table.DataCell>
      <Table.DataCell>
        <DatoFelt
          hideLabel
          label={`Fom ${radnavn}`}
          verdi={verdier.fom}
          onEndret={(isoDato) => endre({ fom: isoDato })}
        />
      </Table.DataCell>
      <Table.DataCell>
        <DatoFelt
          hideLabel
          label={`Virkningsdato ${radnavn}`}
          verdi={verdier.virkningsdato}
          onEndret={(isoDato) => endre({ virkningsdato: isoDato })}
        />
      </Table.DataCell>
      <Table.DataCell>
        <Select
          size="small"
          hideLabel
          label={`Type ${radnavn}`}
          value={verdier.type}
          onChange={(event) =>
            endre({
              type: event.target.value as IndividuellForsikringstype | "",
            })
          }
        >
          <option value="" />
          {individuelleForsikringstyper.map((valg) => (
            <option key={valg.verdi} value={valg.verdi}>
              {valg.tekst}
            </option>
          ))}
        </Select>
      </Table.DataCell>
      <Table.DataCell>
        <TextField
          size="small"
          hideLabel
          type="number"
          htmlSize={10}
          label={`Premiegrunnlag ${radnavn}`}
          value={verdier.premiegrunnlag}
          onChange={(event) => endre({ premiegrunnlag: event.target.value })}
        />
      </Table.DataCell>
      <Table.DataCell>
        <DatoFelt
          hideLabel
          label={`Opphørsdato ${radnavn}`}
          verdi={verdier.opphørsdato}
          onEndret={(isoDato) => endre({ opphørsdato: isoDato })}
        />
      </Table.DataCell>
      <Table.DataCell>
        <TextField
          size="small"
          hideLabel
          type="text"
          htmlSize={14}
          label={`Opphørsgrunn ${radnavn}`}
          value={verdier.opphørsgrunn}
          onChange={(event) => endre({ opphørsgrunn: event.target.value })}
        />
      </Table.DataCell>
    </>
  );

  return (
    <VStack gap="space-16" minWidth="0" asChild>
      <section>
        <Heading level="2" size="small">
          {tittel}
        </Heading>
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
                <Table.ColumnHeader scope="col">Godkjent</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">Fom</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">
                  Virkningsdato
                </Table.ColumnHeader>
                <Table.ColumnHeader scope="col">Type</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">
                  Premiegrunnlag
                </Table.ColumnHeader>
                <Table.ColumnHeader scope="col">Opphørsdato</Table.ColumnHeader>
                <Table.ColumnHeader scope="col">
                  Opphørsgrunn
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {forsikringer.map((forsikring) => {
                const verdier = utkast[forsikring.id];
                const redigeres = verdier !== undefined;
                const erValgt = valgtId === forsikring.id;
                return (
                  <Table.Row
                    key={forsikring.id}
                    selected={erValgt}
                    aria-current={erValgt ? "true" : undefined}
                    onRowClick={() => onVelg(forsikring)}
                  >
                    <Table.DataCell>
                      <HStack gap="space-4" wrap={false}>
                        <Button
                          type="button"
                          variant="tertiary"
                          size="small"
                          icon={<ReceiptIcon aria-hidden />}
                          aria-label={`Vis fakturaer for rad ${forsikring.id}`}
                          aria-pressed={erValgt}
                          title="Vis fakturaer"
                          onClick={() => onVelg(forsikring)}
                        />
                        {redigeres ? (
                          <>
                            <Button
                              type="button"
                              variant="tertiary"
                              size="small"
                              icon={<FloppydiskIcon aria-hidden />}
                              aria-label={`Lagre rad ${forsikring.id}`}
                              title="Lagre"
                              disabled={lagrer === forsikring.id}
                              onClick={() => void lagreEndring(forsikring)}
                            />
                            <Button
                              type="button"
                              variant="tertiary"
                              size="small"
                              icon={<XMarkIcon aria-hidden />}
                              aria-label={`Avbryt redigering av rad ${forsikring.id}`}
                              title="Avbryt"
                              onClick={() => avbrytRedigering(forsikring.id)}
                            />
                          </>
                        ) : (
                          <Button
                            type="button"
                            variant="tertiary"
                            size="small"
                            icon={<PencilIcon aria-hidden />}
                            aria-label={`Rediger rad ${forsikring.id}`}
                            title="Rediger"
                            onClick={() =>
                              setUtkast((forrige) => ({
                                ...forrige,
                                [forsikring.id]: tilUtkast(forsikring),
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
                          aria-label={`Slett rad ${forsikring.id}`}
                          title="Slett"
                          onClick={() => void slettRad(forsikring)}
                        />
                      </HStack>
                    </Table.DataCell>
                    {redigeres ? (
                      redigeringsceller(
                        `rad ${forsikring.id}`,
                        verdier,
                        (endring) => endreUtkast(forsikring.id, endring),
                      )
                    ) : (
                      <>
                        <Table.DataCell>
                          {forsikring.godkjent ? "✔️" : "❌"}
                        </Table.DataCell>
                        <Table.DataCell>{forsikring.fom ?? ""}</Table.DataCell>
                        <Table.DataCell>
                          {forsikring.virkningsdato ?? ""}
                        </Table.DataCell>
                        <Table.DataCell>
                          {visType(forsikring.type)}
                        </Table.DataCell>
                        <Table.DataCell>
                          {forsikring.premiegrunnlag}
                        </Table.DataCell>
                        <Table.DataCell>
                          {forsikring.opphørsdato ?? ""}
                        </Table.DataCell>
                        <Table.DataCell>
                          {forsikring.opphørsgrunn ?? ""}
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
                        aria-label={`Lagre ny rad i ${tittel}`}
                        title="Lagre"
                        disabled={lagrer === "ny"}
                        onClick={() => void lagreNyRad()}
                      />
                      <Button
                        type="button"
                        variant="tertiary"
                        size="small"
                        icon={<XMarkIcon aria-hidden />}
                        aria-label={`Avbryt ny rad i ${tittel}`}
                        title="Avbryt"
                        onClick={onLukkNyRad}
                      />
                    </HStack>
                  </Table.DataCell>
                  {redigeringsceller("ny rad", nyRad, endreNyRad)}
                </Table.Row>
              )}
              {forsikringer.length === 0 && nyRad === null && (
                <Table.Row>
                  <Table.DataCell colSpan={8}>
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
