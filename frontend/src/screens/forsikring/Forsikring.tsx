import React, { useEffect, useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { ErrorMessage, VStack } from "@navikt/ds-react";

import { del, get, post, put } from "../../io/api";
import { useAddSystemMessage } from "../../state/useSystemMessages";
import { AddButton } from "../../components/AddButton";
import { Spinner } from "../../components/Spinner";

import { ForsikringTabell } from "./ForsikringTabell";
import { FakturaTabell } from "./FakturaTabell";
import type {
  Forsikringsfaktura,
  ForsikringsfakturaPayload,
  IndividuellForsikring,
  IndividuellForsikringPayload,
} from "./typer";

const forsikringerUrl = (identitetsnummer: string) =>
  `/personer/${identitetsnummer}/individuelle-forsikringer`;
const forsikringUrl = (id: number) => `/individuelle-forsikringer/${id}`;
const fakturaerUrl = (forsikringId: number) =>
  `${forsikringUrl(forsikringId)}/forsikringsfakturaer`;
const fakturaUrl = (id: number) => `/forsikringsfakturaer/${id}`;

const feilmelding = async (response: Response): Promise<string> => {
  try {
    const kropp = await response.json();
    if (kropp?.melding) return kropp.melding;
  } catch {
    // responsen hadde ingen lesbar feilmelding
  }
  return `Fikk statuskode ${response.status}`;
};

const sjekkRespons = async (response: Response): Promise<Response> => {
  if (!response.ok) throw new Error(await feilmelding(response));
  return response;
};

export const Forsikring = React.memo(() => {
  const { watch } = useFormContext();
  const addMessage = useAddSystemMessage();

  const fnr: string | undefined = watch("fnr");
  const person = /^\d{11}$/.test(fnr ?? "") ? (fnr as string) : null;

  const [forsikringer, setForsikringer] = useState<IndividuellForsikring[]>([]);
  const [fakturaer, setFakturaer] = useState<Forsikringsfaktura[]>([]);
  const [valgtId, setValgtId] = useState<number | null>(null);
  const [nyForsikringÅpen, setNyForsikringÅpen] = useState(false);
  const [laster, setLaster] = useState(false);
  const [feil, setFeil] = useState<string | null>(null);

  const visFeil = (error: unknown) => {
    const tekst = error instanceof Error ? error.message : String(error);
    setFeil(tekst);
    addMessage({ id: nanoid(), text: tekst, dismissable: true });
  };

  const hentForsikringer = async (forPerson: string) => {
    setLaster(true);
    try {
      const respons = await get(forsikringerUrl(forPerson)).then(sjekkRespons);
      const rader: IndividuellForsikring[] = await respons.json();
      setForsikringer(rader);
      setValgtId((forrige) =>
        rader.some((rad) => rad.id === forrige) ? forrige : null,
      );
      setFeil(null);
    } catch (error) {
      visFeil(error);
    } finally {
      setLaster(false);
    }
  };

  const hentFakturaer = async (forsikringId: number) => {
    try {
      const respons = await get(fakturaerUrl(forsikringId)).then(sjekkRespons);
      setFakturaer(await respons.json());
      setFeil(null);
    } catch (error) {
      visFeil(error);
    }
  };

  useEffect(() => {
    if (person === null) {
      setForsikringer([]);
      setValgtId(null);
      setFeil(null);
      return;
    }
    void hentForsikringer(person);
    // henter på nytt når det fylles inn et nytt fødselsnummer, ellers etter hver endring
  }, [person]);

  useEffect(() => {
    if (valgtId === null) {
      setFakturaer([]);
      return;
    }
    void hentFakturaer(valgtId);
  }, [valgtId]);

  const utfør = async (
    kall: Promise<Response>,
    kvittering: string,
    oppfriskning: () => Promise<void>,
  ) => {
    try {
      await kall.then(sjekkRespons);
      addMessage({ id: nanoid(), text: kvittering, timeToLiveMs: 4000 });
      setFeil(null);
      await oppfriskning();
    } catch (error) {
      visFeil(error);
      throw error;
    }
  };

  const oppfriskForsikringer = async () => {
    if (person !== null) await hentForsikringer(person);
  };

  const oppfriskFakturaer = async () => {
    if (valgtId !== null) await hentFakturaer(valgtId);
  };

  const visForsikringstabell =
    person !== null && (forsikringer.length > 0 || nyForsikringÅpen);

  return (
    <VStack gap="space-16" align="start" minWidth="0" width="100%">
      <AddButton
        data-testid="forsikringButton"
        disabled={person === null || nyForsikringÅpen}
        onClick={() => setNyForsikringÅpen(true)}
      >
        Legg til forsikring
      </AddButton>
      {feil !== null && <ErrorMessage>{feil}</ErrorMessage>}
      {laster && <Spinner />}
      {visForsikringstabell && (
        <ForsikringTabell
          identitetsnummer={person}
          forsikringer={forsikringer}
          valgtId={valgtId}
          onVelg={(forsikring) => setValgtId(forsikring.id)}
          nyRadÅpen={nyForsikringÅpen}
          onLukkNyRad={() => setNyForsikringÅpen(false)}
          onOpprett={(payload: IndividuellForsikringPayload) =>
            utfør(
              post(forsikringerUrl(person), payload),
              "Ny individuell forsikring er lagret.",
              oppfriskForsikringer,
            )
          }
          onOppdater={(forsikring, payload) =>
            utfør(
              put(forsikringUrl(forsikring.id), payload),
              `Individuell forsikring ${forsikring.id} er oppdatert.`,
              oppfriskForsikringer,
            )
          }
          onSlett={(forsikring) =>
            utfør(
              del(forsikringUrl(forsikring.id)),
              `Individuell forsikring ${forsikring.id} er slettet.`,
              oppfriskForsikringer,
            )
          }
        />
      )}
      {valgtId !== null && (
        <VStack minWidth="0" width="100%">
          <FakturaTabell
            fakturaer={fakturaer}
            onOpprett={(payload: ForsikringsfakturaPayload) =>
              utfør(
                post(fakturaerUrl(valgtId), payload),
                "Ny forsikringsfaktura er lagret.",
                oppfriskFakturaer,
              )
            }
            onOppdater={(faktura, payload) =>
              utfør(
                put(fakturaUrl(faktura.id), payload),
                `Forsikringsfaktura ${faktura.id} er oppdatert.`,
                oppfriskFakturaer,
              )
            }
            onSlett={(faktura) =>
              utfør(
                del(fakturaUrl(faktura.id)),
                `Forsikringsfaktura ${faktura.id} er slettet.`,
                oppfriskFakturaer,
              )
            }
          />
        </VStack>
      )}
    </VStack>
  );
});
