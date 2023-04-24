import React, { useEffect, useState } from "react";
import styles from "./CopyField.module.css";
import classNames from "classnames";
import { Input } from "./Input";
import { Button } from "./Button";
import { ErrorMessage } from "./ErrorMessage";


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
          {copied && <i className="material-icons check_circle" />}
          {error && <i className="material-icons error" />}
          {!copied && !error && <i className="material-icons content_copy" />}
        </Button>
      </span>
      {error && (
        <ErrorMessage>Kunne ikke kopiere til utklippstavle</ErrorMessage>
      )}
    </label>
  );
};
