import React, { ReactNode } from "react";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { FormProvider, useForm } from "react-hook-form";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";

import { AppProvider } from "../../state/AppContext";
import { Forsikring } from "./Forsikring";
import { forsikringsfaktura, individuellForsikring } from "./testdata";

vi.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = vi.fn();

const IDENTITETSNUMMER = "31128512345";

const forsikringer = [
  individuellForsikring({ id: 1, premiegrunnlag: 100 }),
  individuellForsikring({ id: 2, godkjent: false }),
];

const fakturaer = [
  forsikringsfaktura({ id: 10, halvdel: 1 }),
  forsikringsfaktura({ id: 11, halvdel: 2 }),
];

const mockFetch = (forsikringsrader: unknown[] = forsikringer) => {
  (fetch as Mock).mockImplementation((url: string, init?: RequestInit) => {
    const metode = (init?.method ?? "get").toLowerCase();
    if (metode !== "get") {
      return Promise.resolve({ ok: true, status: 204 } as Response);
    }
    const rader = url.includes("forsikringsfakturaer")
      ? fakturaer
      : forsikringsrader;
    return Promise.resolve({
      ok: true,
      status: 200,
      json: () => Promise.resolve(rader),
    } as Response);
  });
};

const Skjema = ({ children }: { children: ReactNode }) => {
  const form = useForm({ defaultValues: { fnr: "" } });
  return (
    <FormProvider {...form}>
      <label>
        Fødselsnummer
        <input {...form.register("fnr")} />
      </label>
      {children}
    </FormProvider>
  );
};

const wrapper = ({ children }: { children: ReactNode }) => (
  <AppProvider>
    <Skjema>{children}</Skjema>
  </AppProvider>
);

const kallTil = (metode: string, url: string) =>
  (fetch as Mock).mock.calls.find(
    ([kalltUrl, init]) =>
      kalltUrl.endsWith(url) &&
      (init?.method ?? "get").toLowerCase() === metode.toLowerCase(),
  );

const forsikringstabell = () =>
  screen.getByRole("table", { name: /^Individuelle forsikringer/ });

const fakturatabell = () => screen.getByRole("table", { name: "Fakturaer" });

const leggTilForsikring = () => screen.getByText("Legg til forsikring");

const velgForsikring = async (radnummer: number) =>
  userEvent.click(within(forsikringstabell()).getAllByRole("row")[radnummer]);

const skrivFødselsnummer = async (fødselsnummer = IDENTITETSNUMMER) => {
  render(<Forsikring />, { wrapper });
  await userEvent.type(screen.getByLabelText("Fødselsnummer"), fødselsnummer);
};

describe("Forsikring", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFetch();
  });

  it("henter forsikringene til personen som er fylt inn i skjemaet", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    expect(
      kallTil("get", `/personer/${IDENTITETSNUMMER}/individuelle-forsikringer`),
    ).toBeDefined();

    const rader = within(forsikringstabell()).getAllByRole("row").slice(1);
    expect(rader).toHaveLength(2);
  });

  it("viser ikke id eller identitetsnummer, og viser lesbare verdier", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    const tabell = forsikringstabell();

    expect(
      within(tabell)
        .getAllByRole("columnheader")
        .map((it) => it.textContent),
    ).toEqual([
      "",
      "Godkjent",
      "Fom",
      "Virkningsdato",
      "Type",
      "Premiegrunnlag",
      "Opphørsdato",
      "Opphørsgrunn",
    ]);
    expect(within(tabell).queryByText(IDENTITETSNUMMER)).toBe(null);
    expect(
      within(tabell).getAllByText(
        "Selvstendig næringsdrivende 80 % fra 1. dag",
      ),
    ).toHaveLength(2);
    expect(within(tabell).getAllByText("✔️")).toHaveLength(1);
    expect(within(tabell).getAllByText("❌")).toHaveLength(1);
  });

  it("henter ingenting før fødselsnummeret har elleve siffer", async () => {
    await skrivFødselsnummer("311285");

    expect(leggTilForsikring()).toBeDisabled();
    expect(screen.queryByRole("table")).toBe(null);
    expect(fetch).not.toHaveBeenCalled();
  });

  it("viser ingen tabell når personen ikke har forsikringer", async () => {
    mockFetch([]);
    await skrivFødselsnummer();

    await waitFor(() => expect(leggTilForsikring()).toBeEnabled());
    expect(screen.queryByRole("table")).toBe(null);
  });

  it("viser tabellen med forsikringen som holder på å opprettes", async () => {
    mockFetch([]);
    await skrivFødselsnummer();

    await waitFor(() =>
      expect(
        kallTil(
          "get",
          `/personer/${IDENTITETSNUMMER}/individuelle-forsikringer`,
        ),
      ).toBeDefined(),
    );
    await userEvent.click(leggTilForsikring());

    expect(forsikringstabell()).toBeVisible();
    expect(leggTilForsikring()).toBeDisabled();
  });

  it("henter fakturaene til forsikringen som velges", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await velgForsikring(1);

    await waitFor(() => expect(fakturatabell()).toBeVisible());
    expect(
      kallTil("get", "/individuelle-forsikringer/1/forsikringsfakturaer"),
    ).toBeDefined();
    expect(within(fakturatabell()).getAllByRole("row").slice(1)).toHaveLength(
      2,
    );
  });

  it("oppdaterer en forsikring med PUT", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await userEvent.click(screen.getByLabelText("Rediger rad 1"));

    const premiegrunnlag = screen.getByLabelText("Premiegrunnlag rad 1");
    await userEvent.clear(premiegrunnlag);
    await userEvent.type(premiegrunnlag, "250");
    await userEvent.click(screen.getByLabelText("Godkjent rad 1"));
    await userEvent.click(screen.getByLabelText("Lagre rad 1"));

    await waitFor(() => {
      const kall = kallTil("put", "/individuelle-forsikringer/1");
      expect(kall).toBeDefined();
      const kropp = JSON.parse(kall![1].body);
      expect(kropp).toMatchObject({
        premiegrunnlag: 250,
        godkjent: false,
        type: "SELVSTENDIG_80_PROSENT_FRA_DAG_1",
      });
      expect(kropp.id).toBeUndefined();
      expect(kropp.identitetsnummer).toBeUndefined();
    });
  });

  it("sletter en forsikring med DELETE", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await userEvent.click(screen.getByLabelText("Slett rad 2"));

    await waitFor(() =>
      expect(kallTil("delete", "/individuelle-forsikringer/2")).toBeDefined(),
    );
  });

  it("oppretter en forsikring med POST på personen", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await userEvent.click(leggTilForsikring());

    const premiegrunnlag = screen.getByLabelText("Premiegrunnlag ny rad");
    await userEvent.clear(premiegrunnlag);
    await userEvent.type(premiegrunnlag, "500000");
    await userEvent.click(
      screen.getByLabelText(
        `Lagre ny rad i Individuelle forsikringer for ${IDENTITETSNUMMER}`,
      ),
    );

    await waitFor(() => {
      const kall = kallTil(
        "post",
        `/personer/${IDENTITETSNUMMER}/individuelle-forsikringer`,
      );
      expect(kall).toBeDefined();
      const kropp = JSON.parse(kall![1].body);
      expect(kropp.premiegrunnlag).toBe(500000);
      expect(kropp.godkjent).toBe(true);
      expect(kropp.type).toBe("SELVSTENDIG_80_PROSENT_FRA_DAG_1");
      expect(kropp.identitetsnummer).toBeUndefined();
    });
    await waitFor(() => expect(leggTilForsikring()).toBeEnabled());
  });

  it("krever år og halvdel på en ny faktura", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await velgForsikring(1);
    await waitFor(() => expect(fakturatabell()).toBeVisible());

    await userEvent.click(screen.getByLabelText("Legg til rad i Fakturaer"));
    await userEvent.clear(screen.getByLabelText("År ny rad"));

    expect(screen.getByLabelText("Lagre ny rad i Fakturaer")).toBeDisabled();
    expect(
      within(screen.getByLabelText("Halvdel ny rad")).queryByRole("option", {
        name: "",
      }),
    ).toBe(null);
  });

  it("oppretter, oppdaterer og sletter fakturaer på den valgte forsikringen", async () => {
    await skrivFødselsnummer();

    await waitFor(() => expect(forsikringstabell()).toBeVisible());
    await velgForsikring(1);
    await waitFor(() => expect(fakturatabell()).toBeVisible());

    await userEvent.click(screen.getByLabelText("Legg til rad i Fakturaer"));
    await userEvent.selectOptions(screen.getByLabelText("Halvdel ny rad"), "2");
    await userEvent.click(screen.getByLabelText("Lagre ny rad i Fakturaer"));

    await waitFor(() => {
      const kall = kallTil(
        "post",
        "/individuelle-forsikringer/1/forsikringsfakturaer",
      );
      expect(kall).toBeDefined();
      expect(JSON.parse(kall![1].body).halvdel).toBe(2);
    });

    await userEvent.click(screen.getByLabelText("Rediger rad 10"));
    const betalingsdato = screen.getByLabelText("Betalingsdato rad 10");
    await userEvent.type(betalingsdato, "2026-08-15");
    await userEvent.click(screen.getByLabelText("Lagre rad 10"));

    await waitFor(() => {
      const kall = kallTil("put", "/forsikringsfakturaer/10");
      expect(kall).toBeDefined();
      expect(JSON.parse(kall![1].body)).toEqual({
        år: 2026,
        halvdel: 1,
        betalingsdato: "2026-08-15",
      });
    });

    await userEvent.click(screen.getByLabelText("Slett rad 11"));
    await waitFor(() =>
      expect(kallTil("delete", "/forsikringsfakturaer/11")).toBeDefined(),
    );
  });
});
