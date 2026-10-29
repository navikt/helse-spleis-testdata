import React from "react";
import { Heading, Link, List, VStack } from "@navikt/ds-react";
import { ExternalLinkIcon } from "@navikt/aksel-icons";

export type Tracelenke = {
  tekst: string;
  url: string;
};

export const traceUrlFra = (body: unknown): string | undefined => {
  if (typeof body !== "string" || body.length === 0) return undefined;
  try {
    const traceUrl = JSON.parse(body)?.traceUrl;
    return typeof traceUrl === "string" ? traceUrl : undefined;
  } catch {
    return undefined;
  }
};

export const Tracelenker = ({ lenker }: { lenker: Tracelenke[] }) => {
  if (lenker.length === 0) return null;
  return (
    <VStack gap="space-8" data-testid="tracelenker">
      <Heading level="3" size="xsmall">
        Logger i Google Cloud
      </Heading>
      <List size="small">
        {lenker.map((lenke, i) => (
          <List.Item key={i}>
            <Link href={lenke.url} target="_blank" rel="noopener noreferrer">
              {lenke.tekst}
              <ExternalLinkIcon title="åpnes i ny fane" />
            </Link>
          </List.Item>
        ))}
      </List>
    </VStack>
  );
};
