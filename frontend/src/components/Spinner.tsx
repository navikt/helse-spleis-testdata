import React from "react";

import styles from "./halloween/Halloween.module.css";

export const Spinner = () => (
  <span
    role="img"
    aria-label="Laster"
    title="Laster"
    data-testid="spinner"
    className={styles.Spinner}
  >
    🎃
  </span>
);
