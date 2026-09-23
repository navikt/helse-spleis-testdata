import React, { useEffect, useState } from "react";
import { Button, ErrorMessage, HStack, TextField } from "@navikt/ds-react";
import {
  CheckmarkCircleIcon,
  FilesIcon,
  XMarkOctagonIcon,
} from "@navikt/aksel-icons";

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

  const ikon = () => {
    if (copied) return <CheckmarkCircleIcon aria-hidden />;
    if (error) return <XMarkOctagonIcon aria-hidden />;
    return <FilesIcon aria-hidden />;
  };

  return (
    <div>
      <HStack gap="space-8" align="end" wrap={false}>
        <TextField
          label={props.label}
          size="small"
          type="text"
          value={props.value}
          readOnly
        />
        <Button
          type="button"
          variant="secondary"
          size="small"
          data-color={copied ? "success" : error ? "danger" : "neutral"}
          icon={ikon()}
          aria-label={`Kopier ${props.label}`}
          onClick={copyValueToClipboard}
        />
      </HStack>
      {error && (
        <ErrorMessage size="small">
          Kunne ikke kopiere til utklippstavle
        </ErrorMessage>
      )}
    </div>
  );
};
