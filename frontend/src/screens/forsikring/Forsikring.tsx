import React, { useEffect, useMemo, useState } from "react";
import { nanoid } from "nanoid";

import { del, get, post, put } from "../../io/api";
import { useAddSystemMessage } from "../../state/useSystemMessages";
import { ErrorMessage } from "../../components/ErrorMessage";
import { Spinner } from "../../components/Spinner";

import { RedigerbarTabell } from "./RedigerbarTabell";
import {
  faktiskFødselsnummer,
  ifFkonto12Kolonner,
  ifVedfrivt10Kolonner,
  nyIfFkonto12,
  nyIfVedfrivt10,
  sorterIfFkonto12,
  sorterIfVedfrivt10,
} from "./kolonner";
import type { IfFkonto12, IfVedfrivt10 } from "./typer";

import styles from "./Forsikring.module.css";

const vedfrivt10Url = "/replikabase/if-vedfrivt-10";
const fkonto12Url = "/replikabase/if-fkonto-12";

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

export const Forsikring: React.FC = () => {
  const addMessage = useAddSystemMessage();

  const [vedfrivt10, setVedfrivt10] = useState<IfVedfrivt10[]>([]);
  const [fkonto12, setFkonto12] = useState<IfFkonto12[]>([]);
  const [valgtIdVed, setValgtIdVed] = useState<number | null>(null);
  const [laster, setLaster] = useState(true);
  const [feil, setFeil] = useState<string | null>(null);

  const visFeil = (error: unknown) => {
    const tekst = error instanceof Error ? error.message : String(error);
    setFeil(tekst);
    addMessage({ id: nanoid(), text: tekst, dismissable: true });
  };

  const hentAlt = async () => {
    try {
      const [vedfrivtRespons, fkontoRespons] = await Promise.all([
        get(vedfrivt10Url).then(sjekkRespons),
        get(fkonto12Url).then(sjekkRespons),
      ]);
      setVedfrivt10(sorterIfVedfrivt10(await vedfrivtRespons.json()));
      setFkonto12(await fkontoRespons.json());
      setFeil(null);
    } catch (error) {
      visFeil(error);
    } finally {
      setLaster(false);
    }
  };

  useEffect(() => {
    void hentAlt();
    // henter kun ved oppstart, senere henting skjer etter hver endring
  }, []);

  const valgtRad = useMemo(
    () => vedfrivt10.find((rad) => rad.ID_VED === valgtIdVed) ?? null,
    [vedfrivt10, valgtIdVed],
  );

  const valgteFkonto12 = useMemo(() => {
    if (valgtRad === null) return [];
    return sorterIfFkonto12(
      fkonto12.filter(
        (rad) =>
          rad.IF01_AGNR_FNR === valgtRad.IF01_AGNR_FNR &&
          rad.IF01_KODE === valgtRad.IF01_KODE &&
          rad.IF10_FORSFOM_SEQ === valgtRad.IF10_FORSFOM_SEQ,
      ),
    );
  }, [fkonto12, valgtRad]);

  const utfør = async (kall: Promise<Response>, kvittering: string) => {
    try {
      await kall.then(sjekkRespons);
      addMessage({ id: nanoid(), text: kvittering, timeToLiveMs: 4000 });
      setFeil(null);
      await hentAlt();
    } catch (error) {
      visFeil(error);
      throw error;
    }
  };

  return (
    <div className={styles.Forsikring}>
      <h1 className={styles.Overskrift}>Forsikring</h1>
      {feil !== null && <ErrorMessage>{feil}</ErrorMessage>}
      {laster ? (
        <Spinner />
      ) : (
        <>
          <RedigerbarTabell<IfVedfrivt10>
            tittel="IF_VEDFRIVT_10"
            kolonner={ifVedfrivt10Kolonner}
            rader={vedfrivt10}
            radId={(rad) => rad.ID_VED}
            valgtId={valgtIdVed}
            onVelg={(rad) => setValgtIdVed(rad.ID_VED)}
            nyRadVerdier={nyIfVedfrivt10}
            onOpprett={(verdier) =>
              utfør(
                post(vedfrivt10Url, verdier),
                "Ny rad i IF_VEDFRIVT_10 er lagret.",
              )
            }
            onOppdater={(rad, verdier) =>
              utfør(
                put(`${vedfrivt10Url}/${rad.ID_VED}`, verdier),
                `IF_VEDFRIVT_10 ${rad.ID_VED} er oppdatert.`,
              )
            }
            onSlett={(rad) =>
              utfør(
                del(`${vedfrivt10Url}/${rad.ID_VED}`),
                `IF_VEDFRIVT_10 ${rad.ID_VED} er slettet.`,
              )
            }
          />
          {valgtRad === null ? (
            <p className={styles.IngenValgt}>
              Velg en rad i IF_VEDFRIVT_10 for å se tilhørende
              IF_FKONTO_12-rader.
            </p>
          ) : (
            <RedigerbarTabell<IfFkonto12>
              tittel={`IF_FKONTO_12 for ${faktiskFødselsnummer(valgtRad.IF01_AGNR_FNR)}`}
              kolonner={ifFkonto12Kolonner}
              rader={valgteFkonto12}
              radId={(rad) => rad.ID_KONT}
              nyRadVerdier={() => nyIfFkonto12(valgtRad)}
              onOpprett={(verdier) =>
                utfør(
                  post(fkonto12Url, verdier),
                  "Ny rad i IF_FKONTO_12 er lagret.",
                )
              }
              onOppdater={(rad, verdier) =>
                utfør(
                  put(`${fkonto12Url}/${rad.ID_KONT}`, verdier),
                  `IF_FKONTO_12 ${rad.ID_KONT} er oppdatert.`,
                )
              }
              onSlett={(rad) =>
                utfør(
                  del(`${fkonto12Url}/${rad.ID_KONT}`),
                  `IF_FKONTO_12 ${rad.ID_KONT} er slettet.`,
                )
              }
            />
          )}
        </>
      )}
    </div>
  );
};
