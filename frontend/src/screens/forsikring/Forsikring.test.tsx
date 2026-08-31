import React, { ReactNode } from "react";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";

import { AppProvider } from "../../state/AppContext";
import { Forsikring } from "./Forsikring";
import { ifFkonto12, ifVedfrivt10 } from "./testdata";

vi.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = vi.fn();

const vedfrivtRader = [
  ifVedfrivt10({ ID_VED: 1, IF01_AGNR_FNR: 85_12_31_12345, IF10_PREMIE: 100 }),
  ifVedfrivt10({ ID_VED: 2, IF01_AGNR_FNR: 85_01_01_12345 }),
];

const fkontoRader = [
  ifFkonto12({
    ID_KONT: 10,
    IF01_AGNR_FNR: 85_12_31_12345,
    IF12_FOM: 20260201,
  }),
  ifFkonto12({
    ID_KONT: 11,
    IF01_AGNR_FNR: 85_12_31_12345,
    IF12_FOM: 20260101,
  }),
  ifFkonto12({ ID_KONT: 12, IF01_AGNR_FNR: 85_01_01_12345 }),
];

const mockFetch = () => {
  (fetch as Mock).mockImplementation((url: string, init?: RequestInit) => {
    const metode = (init?.method ?? "get").toLowerCase();
    if (metode !== "get") {
      return Promise.resolve({ ok: true, status: 204 } as Response);
    }
    const rader = url.includes("if-vedfrivt-10") ? vedfrivtRader : fkontoRader;
    return Promise.resolve({
      ok: true,
      status: 200,
      json: () => Promise.resolve(rader),
    } as Response);
  });
};

const wrapper = ({ children }: { children: ReactNode }) => (
  <AppProvider>{children}</AppProvider>
);

const kallTil = (metode: string, url: string) =>
  (fetch as Mock).mock.calls.find(
    ([kalltUrl, init]) =>
      kalltUrl.endsWith(url) &&
      (init?.method ?? "get").toLowerCase() === metode.toLowerCase(),
  );

const vedfrivtTabell = () =>
  screen.getByRole("table", { name: "IF_VEDFRIVT_10" });

const fkontoTabell = () => screen.getByRole("table", { name: /^IF_FKONTO_12/ });

describe("Forsikring", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockFetch();
  });

  it("viser IF_VEDFRIVT_10-rader sortert på faktisk fødselsnummer", async () => {
    render(<Forsikring />, { wrapper });

    await waitFor(() => expect(vedfrivtTabell()).toBeVisible());

    const rader = within(vedfrivtTabell()).getAllByRole("row").slice(1);
    expect(within(rader[0]).getByLabelText("Velg rad 2")).toBeInTheDocument();
    expect(within(rader[1]).getByLabelText("Velg rad 1")).toBeInTheDocument();
  });

  it("viser tilhørende IF_FKONTO_12-rader sortert på IF12_FOM når en rad velges", async () => {
    render(<Forsikring />, { wrapper });

    await waitFor(() => expect(vedfrivtTabell()).toBeVisible());
    await userEvent.click(screen.getByLabelText("Velg rad 1"));

    const rader = within(fkontoTabell()).getAllByRole("row").slice(1);
    expect(rader).toHaveLength(2);
    expect(within(rader[0]).getByLabelText("Slett rad 11")).toBeInTheDocument();
    expect(within(rader[1]).getByLabelText("Slett rad 10")).toBeInTheDocument();
  });

  it("oppdaterer en rad med PUT", async () => {
    render(<Forsikring />, { wrapper });

    await waitFor(() => expect(vedfrivtTabell()).toBeVisible());
    await userEvent.click(screen.getByLabelText("Rediger rad 1"));

    const premie = screen.getByLabelText("IF10_PREMIE rad 1");
    await userEvent.clear(premie);
    await userEvent.type(premie, "250");
    await userEvent.click(screen.getByLabelText("Lagre rad 1"));

    await waitFor(() => {
      const kall = kallTil("put", "/replikabase/if-vedfrivt-10/1");
      expect(kall).toBeDefined();
      expect(JSON.parse(kall![1].body)).toMatchObject({
        IF10_PREMIE: 250,
        IF01_AGNR_FNR: 85_12_31_12345,
      });
      expect(JSON.parse(kall![1].body).ID_VED).toBeUndefined();
    });
  });

  it("sletter en rad med DELETE", async () => {
    render(<Forsikring />, { wrapper });

    await waitFor(() => expect(vedfrivtTabell()).toBeVisible());
    await userEvent.click(screen.getByLabelText("Slett rad 2"));

    await waitFor(() =>
      expect(kallTil("delete", "/replikabase/if-vedfrivt-10/2")).toBeDefined(),
    );
  });

  it("oppretter en ny rad med POST og viser bindestrek som id", async () => {
    render(<Forsikring />, { wrapper });

    await waitFor(() => expect(vedfrivtTabell()).toBeVisible());
    await userEvent.click(
      screen.getByLabelText("Legg til rad i IF_VEDFRIVT_10"),
    );

    expect(within(vedfrivtTabell()).getByText("–")).toBeVisible();

    const fnr = screen.getByLabelText("IF01_AGNR_FNR ny rad");
    await userEvent.clear(fnr);
    await userEvent.type(fnr, "85123112345");
    await userEvent.click(
      screen.getByLabelText("Lagre ny rad i IF_VEDFRIVT_10"),
    );

    await waitFor(() => {
      const kall = kallTil("post", "/replikabase/if-vedfrivt-10");
      expect(kall).toBeDefined();
      const kropp = JSON.parse(kall![1].body);
      expect(kropp.IF01_AGNR_FNR).toBe(85123112345);
      expect(kropp.IF10_GODKJ).toBe("J");
      expect(kropp.ID_VED).toBeUndefined();
    });
  });
});
