import React, { useEffect, useState } from "react";
import styles from "./CopyField.module.css";
import classNames from "classnames";
import { Input } from "./Input";
import { Button } from "./Button";
import { ErrorMessage } from "./ErrorMessage";

import copyIcon from "material-design-icons/content/svg/production/ic_content_copy_24px.svg";
import copiedIcon from "material-design-icons/action/svg/production/ic_check_circle_24px.svg";
import errorIcon from "material-design-icons/alert/svg/production/ic_error_18px.svg";

interface CopyFieldProps {
  label: string;
  value: string;
}

export const CopyField: React.FC<CopyFieldProps> = (props) => {
  const [error, setError] = useState(false);
  const [copied, setCopied] = useState(false);

  const copyValueToClipboard = () => {
    try {
      navigator.clipboard.writeText(props.value).then(() => {
        setError(false);
        setCopied(true);
      });
    } catch {
      setCopied(false);
      setError(true);
    }
  };

  useEffect(() => {
    props.value && setCopied(false);
  }, [props.value]);

  return (
    <label className={styles.Label}>
      {props.label}
      <span className={styles.Flex}>
        <Input type="text" value={props.value} disabled />
        <Button
          type="button"
          className={classNames(
            styles.Button,
            copied && styles.copied,
            error && styles.error
          )}
          onClick={copyValueToClipboard}
        >
          {copied && <img src={copiedIcon} alt="" />}
          {error && <img src={errorIcon} alt="" />}
          {!copied && !error && <img src={copyIcon} alt="" />}
        </Button>
      </span>
      {error && (
        <ErrorMessage>Kunne ikke kopiere til utklippstavle</ErrorMessage>
      )}
    </label>
  );
};
