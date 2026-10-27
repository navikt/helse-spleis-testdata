import type React from "react";

export type CssVars = React.CSSProperties & Record<`--${string}`, string>;

export const BOO_EVENT = "halloween:boo";
