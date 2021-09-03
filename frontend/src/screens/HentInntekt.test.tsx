import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { HentInntekt } from "./HentInntekt";
import userEvent from "@testing-library/user-event";

jest.mock("../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = jest.fn();

const mockFetchSuccess = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({
      status: 204,
      json: () => Promise.resolve({ beregnetMånedsinntekt: 54321 }),
    } as Response)
  );
};

const mockFetchError = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 404 } as Response)
  );
};

describe("HentInntekt", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("henter inntekt person", async () => {
    mockFetchSuccess();
    render(<HentInntekt />);
    userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("success")).toBeVisible();
      expect(screen.getAllByRole("textbox")[1]).toHaveValue("54321");
    });
  });

  it("viser feilmelding om henting feiler", async () => {
    mockFetchError();
    render(<HentInntekt />);
    userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("error")).toBeVisible();
      expect(screen.getByText("Kunne ikke hente inntekt")).toBeVisible();
    });
  });

  it("viser feilmelding om personnummer ikke er fylt ut", async () => {
    render(<HentInntekt />);
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
    });
  });
});
