import type { Forsikringsfaktura, IndividuellForsikring } from "./typer";

export const individuellForsikring = (
  overstyringer: Partial<IndividuellForsikring> &
    Pick<IndividuellForsikring, "id">,
): IndividuellForsikring => ({
  identitetsnummer: "31128512345",
  godkjent: true,
  fom: "2026-01-01",
  virkningsdato: "2026-01-01",
  type: "SELVSTENDIG_80_PROSENT_FRA_DAG_1",
  premiegrunnlag: 0,
  opphørsdato: null,
  opphørsgrunn: null,
  ...overstyringer,
});

export const forsikringsfaktura = (
  overstyringer: Partial<Forsikringsfaktura> & Pick<Forsikringsfaktura, "id">,
): Forsikringsfaktura => ({
  år: 2026,
  halvdel: 1,
  betalingsdato: null,
  ...overstyringer,
});
