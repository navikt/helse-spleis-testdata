import React, { useEffect, useRef, useState } from "react";
import { nanoid } from "nanoid";

import ghost from "../../assets/halloween/ghost.svg";
import witch from "../../assets/halloween/witch.svg";
import { useAddSystemMessage } from "../../state/useSystemMessages";
import { BOO_EVENT } from "./halloweenFelles";

import styles from "./Halloween.module.css";

const KONAMI = [
  "arrowup",
  "arrowup",
  "arrowdown",
  "arrowdown",
  "arrowleft",
  "arrowright",
  "arrowleft",
  "arrowright",
  "b",
  "a",
];

const knaskEllerKnep = [
  "🍫 Knask! Du fikk en sjokolade. Den er dessverre ikke refusjonsberettiget.",
  "🍬 Knask! Spøkelset fant en seigmann bak inntektsmeldingen.",
  "🕷️ Knep! En edderkopp har flyttet inn i sykmeldingen din.",
  "🧛 Knep! Arbeidsgiverperioden er nå 16 netter i stedet for 16 dager.",
  "🍭 Knask! Du fikk en kjærlighet på pinne. Sykmeldingsgrad: 100 % søt.",
  "👻 Bø! Spøkelset er sykmeldt fra spøkingen i dag.",
  "🦇 Knep! En flaggermus har avvist søknaden din. Bare tuller.",
  "🎃 Knask! Gresskaret synes testdataene dine ser skummelt bra ut.",
  "🧟 Knep! Zombiene vil ha hjerner, men nøyer seg med fødselsnumre.",
];

const tilfeldig = <T,>(liste: T[]): T =>
  liste[Math.floor(Math.random() * liste.length)];

const tilfeldigMs = (min: number, maks: number): number =>
  min + Math.floor(Math.random() * (maks - min));

const erSkrivefelt = (target: EventTarget | null): boolean =>
  target instanceof HTMLElement &&
  (target.isContentEditable ||
    ["INPUT", "TEXTAREA", "SELECT"].includes(target.tagName));

type Side = "left" | "right";

const KikkendeSpøkelse = () => {
  const [side, setSide] = useState<Side | null>(null);
  const addMessage = useAddSystemMessage();

  useEffect(() => {
    const timeout =
      side === null
        ? setTimeout(
            () => setSide(Math.random() < 0.5 ? "left" : "right"),
            tilfeldigMs(20_000, 50_000),
          )
        : setTimeout(() => setSide(null), 7_000);
    return () => clearTimeout(timeout);
  }, [side]);

  if (side === null) return null;

  return (
    <button
      type="button"
      className={`${styles.PeekingGhost} ${side === "left" ? styles.PeekLeft : styles.PeekRight}`}
      aria-label="Et spøkelse kikker frem. Klikk for knask eller knep"
      onClick={() => {
        addMessage({
          id: nanoid(),
          text: tilfeldig(knaskEllerKnep),
          dismissable: true,
          timeToLiveMs: 6000,
        });
        setSide(null);
      }}
    >
      <img src={ghost} alt="" />
    </button>
  );
};

export const HalloweenOverraskelser = () => {
  const [skremmer, setSkremmer] = useState(false);
  const [heksFlyr, setHeksFlyr] = useState(false);
  const taster = useRef<string[]>([]);

  useEffect(() => {
    const førsteTur = setTimeout(() => setHeksFlyr(true), 8_000);
    const nyeTurer = setInterval(() => setHeksFlyr(true), 120_000);
    return () => {
      clearTimeout(førsteTur);
      clearInterval(nyeTurer);
    };
  }, []);

  useEffect(() => {
    const skrem = () => setSkremmer(true);

    const onKeyDown = (event: KeyboardEvent) => {
      if (erSkrivefelt(event.target)) return;
      taster.current = [...taster.current, event.key.toLowerCase()].slice(
        -KONAMI.length,
      );
      const skrevet = taster.current.join("");
      if (skrevet.endsWith("boo") || skrevet.endsWith("bø")) {
        taster.current = [];
        skrem();
      } else if (taster.current.join() === KONAMI.join()) {
        taster.current = [];
        setHeksFlyr(true);
      }
    };

    window.addEventListener("keydown", onKeyDown);
    window.addEventListener(BOO_EVENT, skrem);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener(BOO_EVENT, skrem);
    };
  }, []);

  return (
    <>
      <KikkendeSpøkelse />
      {heksFlyr && (
        <img
          src={witch}
          alt=""
          aria-hidden
          className={styles.Witch}
          onAnimationEnd={() => setHeksFlyr(false)}
        />
      )}
      {skremmer && (
        <div
          className={styles.Jumpscare}
          aria-hidden
          onAnimationEnd={(event) => {
            if (event.target === event.currentTarget) setSkremmer(false);
          }}
        >
          <img src={ghost} alt="" />
          <span>BØ!</span>
        </div>
      )}
    </>
  );
};
