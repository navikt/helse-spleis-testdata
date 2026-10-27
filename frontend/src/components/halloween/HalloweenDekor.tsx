import React from "react";

import bat from "../../assets/halloween/bat.svg";
import cobweb from "../../assets/halloween/cobweb.svg";
import moon from "../../assets/halloween/moon.svg";
import spider from "../../assets/halloween/spider.svg";

import type { CssVars } from "./halloweenFelles";

import styles from "./Halloween.module.css";

const flaggermus: CssVars[] = [
  { "--top": "18%", "--duration": "19s", "--delay": "0s", "--size": "3rem" },
  { "--top": "32%", "--duration": "26s", "--delay": "-9s", "--size": "2rem" },
  { "--top": "55%", "--duration": "22s", "--delay": "-4s", "--size": "2.5rem" },
  {
    "--top": "74%",
    "--duration": "31s",
    "--delay": "-17s",
    "--size": "1.75rem",
  },
];

const fallende: { emoji: string; style: CssVars }[] = [
  {
    emoji: "🍂",
    style: { "--left": "6%", "--duration": "21s", "--delay": "-3s" },
  },
  {
    emoji: "🍁",
    style: { "--left": "17%", "--duration": "27s", "--delay": "-14s" },
  },
  {
    emoji: "🍬",
    style: { "--left": "29%", "--duration": "24s", "--delay": "-8s" },
  },
  {
    emoji: "🍂",
    style: { "--left": "42%", "--duration": "30s", "--delay": "-21s" },
  },
  {
    emoji: "🕸️",
    style: { "--left": "55%", "--duration": "26s", "--delay": "-2s" },
  },
  {
    emoji: "🍁",
    style: { "--left": "67%", "--duration": "23s", "--delay": "-11s" },
  },
  {
    emoji: "🍭",
    style: { "--left": "79%", "--duration": "29s", "--delay": "-18s" },
  },
  {
    emoji: "🍂",
    style: { "--left": "91%", "--duration": "25s", "--delay": "-6s" },
  },
];

export const HalloweenBakgrunn = () => (
  <div className={styles.Background} aria-hidden>
    <img src={moon} alt="" className={styles.Moon} />
    {flaggermus.map((style, i) => (
      <img key={i} src={bat} alt="" className={styles.Bat} style={style} />
    ))}
    {fallende.map(({ emoji, style }, i) => (
      <span key={i} className={styles.Falling} style={style}>
        {emoji}
      </span>
    ))}
    <div className={styles.Fog} />
  </div>
);

export const HalloweenForgrunn = () => (
  <div className={styles.Foreground} aria-hidden>
    <img
      src={cobweb}
      alt=""
      className={`${styles.Cobweb} ${styles.CobwebLeft}`}
    />
    <img
      src={cobweb}
      alt=""
      className={`${styles.Cobweb} ${styles.CobwebRight}`}
    />
    <div className={styles.Spider}>
      <span className={styles.Thread} />
      <img src={spider} alt="" />
    </div>
  </div>
);
