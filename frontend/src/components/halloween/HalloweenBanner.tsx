import React, { useRef, useState } from "react";
import { BodyShort, Box, HStack } from "@navikt/ds-react";
import { nanoid } from "nanoid";

import pumpkin from "../../assets/halloween/pumpkin.svg";
import { dagerTilHalloween, halloweenTekst } from "./halloweenNedtelling";
import { BOO_EVENT, type CssVars } from "./halloweenFelles";

import styles from "./Halloween.module.css";

const godteri = ["🍬", "🍭", "🍫", "🍡", "🧁", "🍪", "🎃", "🦇", "👻"];
const ANTALL_GODBITER_PER_KLIKK = 10;
const KLIKK_FØR_SKREKK = 13;

interface Godbit {
  id: string;
  emoji: string;
  style: CssVars;
}

const lagGodbit = (indeks: number): Godbit => {
  const vinkel =
    (indeks / ANTALL_GODBITER_PER_KLIKK) * 2 * Math.PI + Math.random() * 0.6;
  const avstand = 3 + Math.random() * 3;
  return {
    id: nanoid(),
    emoji: godteri[Math.floor(Math.random() * godteri.length)],
    style: {
      "--dx": `${Math.cos(vinkel) * avstand}rem`,
      "--dy": `${Math.sin(vinkel) * avstand - 1.5}rem`,
      "--rot": `${Math.round(Math.random() * 720 - 360)}deg`,
    },
  };
};

export const HalloweenBanner = () => {
  const [godbiter, setGodbiter] = useState<Godbit[]>([]);
  const antallKlikk = useRef(0);
  const tekst = halloweenTekst(dagerTilHalloween(new Date()));

  const kastGodteri = () => {
    antallKlikk.current += 1;
    if (antallKlikk.current % KLIKK_FØR_SKREKK === 0) {
      window.dispatchEvent(new Event(BOO_EVENT));
    }
    const nye = Array.from({ length: ANTALL_GODBITER_PER_KLIKK }, (_, i) =>
      lagGodbit(i),
    );
    setGodbiter((gamle) => [...gamle, ...nye]);
  };

  const fjernGodbit = (id: string) =>
    setGodbiter((gamle) => gamle.filter((it) => it.id !== id));

  return (
    <Box
      background="meta-purple-moderateA"
      borderColor="meta-purple-subtle"
      borderWidth="0 0 1 0"
      paddingBlock="space-8"
      paddingInline="space-16"
    >
      <HStack gap="space-12" align="center" justify="center">
        <span aria-hidden className={styles.Swing}>
          🦇
        </span>
        <span className={styles.PumpkinWrapper}>
          <button
            type="button"
            className={styles.PumpkinButton}
            onClick={kastGodteri}
            aria-label="Gresskar. Klikk for godteri"
            title="Knask eller knep?"
          >
            <img src={pumpkin} alt="" />
          </button>
          {godbiter.map((it) => (
            <span
              key={it.id}
              aria-hidden
              className={styles.Candy}
              style={it.style}
              onAnimationEnd={() => fjernGodbit(it.id)}
            >
              {it.emoji}
            </span>
          ))}
        </span>
        <BodyShort weight="semibold" data-color="meta-purple">
          {tekst}
        </BodyShort>
        <span aria-hidden className={styles.Flicker}>
          🕯️
        </span>
        <span aria-hidden className={styles.Float}>
          👻
        </span>
      </HStack>
    </Box>
  );
};
