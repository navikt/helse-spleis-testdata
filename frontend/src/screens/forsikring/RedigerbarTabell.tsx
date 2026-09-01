import React, { useEffect, useState } from "react";
import classNames from "classnames";

import styles from "./RedigerbarTabell.module.css";
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
      <td key={kolonne.key} className={styles.Celle}>
        {kolonne.type === "boolean" ? (
          <input
            className={styles.Input}
            type="checkbox"
            aria-label={`${kolonne.tittel} ${radnavn}`}
            checked={verdier[kolonne.key] === "true"}
            onChange={(event) =>
              oppdaterFelt(nøkkel, kolonne.key, String(event.target.checked))
            }
          />
        ) : kolonne.type === "select" ? (
          <select
            className={styles.Input}
            aria-label={`${kolonne.tittel} ${radnavn}`}
            required={kolonne.påkrevd}
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
          </select>
        ) : (
          <input
            className={styles.Input}
            type={kolonne.type === "text" ? "text" : kolonne.type}
            aria-label={`${kolonne.tittel} ${radnavn}`}
            required={kolonne.påkrevd}
            value={verdier[kolonne.key] ?? ""}
            onChange={(event) =>
              oppdaterFelt(nøkkel, kolonne.key, event.target.value)
            }
          />
        )}
      </td>
    ));
  };

  return (
    <section className={styles.Tabellseksjon}>
      <div className={styles.Overskrift}>
        {!styresUtenfra && (
          <button
            type="button"
            className={styles.Ikonknapp}
            aria-label={`Legg til rad i ${tittel}`}
            title="Legg til rad"
            disabled={nyRad !== null}
            onClick={åpneNyRad}
          >
            <i className="material-icons add_circle_outline" />
          </button>
        )}
        <h2 className={styles.Tittel}>{tittel}</h2>
      </div>
      <div className={styles.TabellContainer}>
        <table className={styles.Tabell} aria-label={tittel}>
          <thead>
            <tr>
              <th scope="col" className={styles.Handlinger} />
              {kolonner.map((kolonne) => (
                <th
                  scope="col"
                  key={kolonne.key}
                  className={styles.Kolonnetittel}
                >
                  {kolonne.tittel}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rader.map((rad) => {
              const nøkkel = String(radId(rad));
              const verdier = utkast[nøkkel];
              const redigeres = verdier !== undefined;
              const erValgt = valgtId !== undefined && valgtId === radId(rad);
              const velg = onVelg ? () => onVelg(rad) : undefined;
              return (
                <tr
                  key={nøkkel}
                  className={classNames(
                    styles.Rad,
                    onVelg && styles.Velgbar,
                    erValgt && styles.erValgt,
                  )}
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
                  <td className={classNames(styles.Celle, styles.Handlinger)}>
                    {redigeres ? (
                      <>
                        <button
                          type="button"
                          className={styles.Ikonknapp}
                          aria-label={`Lagre rad ${nøkkel}`}
                          title="Lagre"
                          disabled={
                            lagrer === nøkkel ||
                            manglerPåkrevdeFelter(kolonner, verdier)
                          }
                          onClick={() => void lagreEndring(rad)}
                        >
                          <i className="material-icons save" />
                        </button>
                        <button
                          type="button"
                          className={styles.Ikonknapp}
                          aria-label={`Avbryt redigering av rad ${nøkkel}`}
                          title="Avbryt"
                          onClick={() => avbrytRedigering(nøkkel)}
                        >
                          <i className="material-icons close" />
                        </button>
                      </>
                    ) : (
                      <button
                        type="button"
                        className={styles.Ikonknapp}
                        aria-label={`Rediger rad ${nøkkel}`}
                        title="Rediger"
                        onClick={() =>
                          setUtkast((forrige) => ({
                            ...forrige,
                            [nøkkel]: tilUtkast(kolonner, rad),
                          }))
                        }
                      >
                        <i className="material-icons edit" />
                      </button>
                    )}
                    <button
                      type="button"
                      className={classNames(styles.Ikonknapp, styles.Slett)}
                      aria-label={`Slett rad ${nøkkel}`}
                      title="Slett"
                      onClick={() => void slettRad(rad)}
                    >
                      <i className="material-icons delete_forever" />
                    </button>
                  </td>
                  {redigeres
                    ? cellerForUtkast(nøkkel, verdier, false)
                    : kolonner.map((kolonne) => (
                        <td key={kolonne.key} className={styles.Celle}>
                          <span className={styles.Verdi}>
                            {visVerdi(kolonne, rad)}
                          </span>
                        </td>
                      ))}
                </tr>
              );
            })}
            {nyRad !== null && (
              <tr className={classNames(styles.Rad, styles.NyRad)}>
                <td className={classNames(styles.Celle, styles.Handlinger)}>
                  <button
                    type="button"
                    className={styles.Ikonknapp}
                    aria-label={`Lagre ny rad i ${tittel}`}
                    title="Lagre"
                    disabled={
                      lagrer === NY_RAD ||
                      manglerPåkrevdeFelter(kolonner, nyRad)
                    }
                    onClick={() => void lagreNyRad()}
                  >
                    <i className="material-icons save" />
                  </button>
                  <button
                    type="button"
                    className={styles.Ikonknapp}
                    aria-label={`Avbryt ny rad i ${tittel}`}
                    title="Avbryt"
                    onClick={lukkNyRad}
                  >
                    <i className="material-icons close" />
                  </button>
                </td>
                {cellerForUtkast(NY_RAD, nyRad, true)}
              </tr>
            )}
            {rader.length === 0 && nyRad === null && (
              <tr>
                <td className={styles.TomTabell} colSpan={kolonner.length + 1}>
                  Ingen rader
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
};
