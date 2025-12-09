import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { HentInntekt } from "./HentInntekt";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";

vi.mock("../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = vi.fn();

const mockFetchSuccess = () => {
  (fetch as Mock).mockImplementationOnce(() =>
    Promise.resolve({
      status: 204,
      json: () => Promise.resolve({ beregnetMånedsinntekt: 54321 }),
    } as Response),
  );
};

const mockFetchError = () => {
  (fetch as Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 404 } as Response),
  );
};

describe("HentInntekt", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("henter inntekt person", async () => {
    mockFetchSuccess();
    render(<HentInntekt />);
    await userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    await userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("success")).toBeVisible();
      expect(screen.getAllByRole("textbox")[1]).toHaveValue("54321");
    });
  });

  it("viser feilmelding om henting feiler", async () => {
    mockFetchError();
    render(<HentInntekt />);
    await userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    await userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("error")).toBeVisible();
      expect(screen.getByText("Kunne ikke hente inntekt")).toBeVisible();
    });
  });

  it("viser feilmelding om personnummer ikke er fylt ut", async () => {
    render(<HentInntekt />);
    await userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
    });
  });
});
