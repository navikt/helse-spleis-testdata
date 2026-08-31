import React, { useState } from "react";
import classNames from "classnames";

import styles from "./RedigerbarTabell.module.css";
import type { Kolonne } from "./kolonner";

type Utkast = Record<string, string>;

const NY_RAD = "ny-rad";

const tilUtkast = <T,>(kolonner: Kolonne<T>[], rad: T): Utkast =>
  Object.fromEntries(
    kolonner.map((kolonne) => {
      const verdi = rad[kolonne.key];
      return [
        kolonne.key,
        verdi === null || verdi === undefined ? "" : String(verdi),
      ];
    }),
  );

const tilPayload = <T,>(
  kolonner: Kolonne<T>[],
  utkast: Utkast,
): Record<string, unknown> => {
  const payload: Record<string, unknown> = {};
  kolonner.forEach((kolonne) => {
    if (kolonne.readOnly) return;
    const verdi = utkast[kolonne.key] ?? "";
    if (verdi === "") {
      payload[kolonne.key] = null;
    } else {
      payload[kolonne.key] = kolonne.type === "number" ? Number(verdi) : verdi;
    }
  });
  return payload;
};

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
}: RedigerbarTabellProps<T>) => {
  const [utkast, setUtkast] = useState<Record<string, Utkast>>({});
  const [nyRad, setNyRad] = useState<Utkast | null>(null);
  const [lagrer, setLagrer] = useState<string | null>(null);

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
      setNyRad(null);
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
        {kolonne.readOnly ? (
          <span className={styles.Verdi}>
            {erNyRad ? "–" : (verdier[kolonne.key] ?? "")}
          </span>
        ) : (
          <input
            className={styles.Input}
            type={kolonne.type === "number" ? "number" : "text"}
            aria-label={`${kolonne.key} ${radnavn}`}
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
        <h2 className={styles.Tittel}>{tittel}</h2>
        <button
          type="button"
          className={styles.Ikonknapp}
          aria-label={`Legg til rad i ${tittel}`}
          title="Legg til rad"
          disabled={nyRad !== null}
          onClick={() => setNyRad(nyRadVerdier())}
        >
          <i className="material-icons add_circle_outline" />
        </button>
      </div>
      <div className={styles.TabellContainer}>
        <table className={styles.Tabell} aria-label={tittel}>
          <thead>
            <tr>
              {onVelg && <th scope="col" className={styles.Handlinger} />}
              {kolonner.map((kolonne) => (
                <th
                  scope="col"
                  key={kolonne.key}
                  className={styles.Kolonnetittel}
                >
                  {kolonne.key}
                </th>
              ))}
              <th scope="col" className={styles.Handlinger}>
                Handlinger
              </th>
            </tr>
          </thead>
          <tbody>
            {rader.map((rad) => {
              const nøkkel = String(radId(rad));
              const verdier = utkast[nøkkel];
              const redigeres = verdier !== undefined;
              const erValgt = valgtId !== undefined && valgtId === radId(rad);
              return (
                <tr
                  key={nøkkel}
                  className={classNames(styles.Rad, erValgt && styles.erValgt)}
                  onClick={onVelg ? () => onVelg(rad) : undefined}
                >
                  {onVelg && (
                    <td className={styles.Celle}>
                      <input
                        type="radio"
                        name={`valgtRad-${tittel}`}
                        aria-label={`Velg rad ${nøkkel}`}
                        checked={erValgt}
                        onChange={() => onVelg(rad)}
                      />
                    </td>
                  )}
                  {redigeres
                    ? cellerForUtkast(nøkkel, verdier, false)
                    : kolonner.map((kolonne) => {
                        const verdi = rad[kolonne.key];
                        return (
                          <td key={kolonne.key} className={styles.Celle}>
                            <span className={styles.Verdi}>
                              {verdi === null || verdi === undefined
                                ? ""
                                : String(verdi)}
                            </span>
                          </td>
                        );
                      })}
                  <td className={classNames(styles.Celle, styles.Handlinger)}>
                    {redigeres ? (
                      <>
                        <button
                          type="button"
                          className={styles.Ikonknapp}
                          aria-label={`Lagre rad ${nøkkel}`}
                          title="Lagre"
                          disabled={lagrer === nøkkel}
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
                </tr>
              );
            })}
            {nyRad !== null && (
              <tr className={classNames(styles.Rad, styles.NyRad)}>
                {onVelg && <td className={styles.Celle} />}
                {cellerForUtkast(NY_RAD, nyRad, true)}
                <td className={classNames(styles.Celle, styles.Handlinger)}>
                  <button
                    type="button"
                    className={styles.Ikonknapp}
                    aria-label={`Lagre ny rad i ${tittel}`}
                    title="Lagre"
                    disabled={lagrer === NY_RAD}
                    onClick={() => void lagreNyRad()}
                  >
                    <i className="material-icons save" />
                  </button>
                  <button
                    type="button"
                    className={styles.Ikonknapp}
                    aria-label={`Avbryt ny rad i ${tittel}`}
                    title="Avbryt"
                    onClick={() => setNyRad(null)}
                  >
                    <i className="material-icons close" />
                  </button>
                </td>
              </tr>
            )}
            {rader.length === 0 && nyRad === null && (
              <tr>
                <td
                  className={styles.TomTabell}
                  colSpan={kolonner.length + (onVelg ? 2 : 1)}
                >
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
