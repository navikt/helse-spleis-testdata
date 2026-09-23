import React from "react";
import { Button } from "@navikt/ds-react";
import { SidebarLeftIcon, SidebarRightIcon } from "@navikt/aksel-icons";
import styles from "./ExpandButton.module.css";

interface ExpandButtonProps extends Omit<
  React.ButtonHTMLAttributes<HTMLButtonElement>,
  "color"
> {
  expanded: boolean;
  onExpand: () => void;
}

export const ExpandButton: React.FC<ExpandButtonProps> = ({
  expanded,
  onExpand,
  ...rest
}) => (
  <Button
    type="button"
    variant="tertiary"
    data-color="neutral"
    size="small"
    className={styles.ExpandButton}
    icon={
      expanded ? (
        <SidebarLeftIcon aria-hidden />
      ) : (
        <SidebarRightIcon aria-hidden />
      )
    }
    aria-label={expanded ? "Gjør menyen smalere" : "Gjør menyen bredere"}
    onClick={onExpand}
    {...rest}
  />
);
