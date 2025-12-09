import styles from "./ExpandButton.module.css";
import classNames from "classnames";
import React from "react";

interface ExpandButtonProps extends React.HTMLAttributes<HTMLButtonElement> {
  expanded: boolean;
  onExpand: () => void;
}

export const ExpandButton: React.FC<ExpandButtonProps> = ({
  expanded,
  onExpand,
  className,
  ...rest
}) => {
  return (
    <button
      className={classNames(
        styles.ExpandButton,
        expanded ? styles.expanded : styles.minified,
        className,
      )}
      onClick={onExpand}
      {...rest}
    />
  );
};
