import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { HentAktørId } from "./HentAktørId";
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
      text: () => Promise.resolve("en-aktørId"),
    } as Response)
  );
};

const mockFetchError = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 404 } as Response)
  );
};

describe("HentAktørId", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("henter aktørId", async () => {
    mockFetchSuccess();
    render(<HentAktørId />);
    userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("success")).toBeVisible();
      expect(screen.getAllByRole("textbox")[1]).toHaveValue("en-aktørId");
    });
  });

  it("viser feilmelding om henting feiler", async () => {
    mockFetchError();
    render(<HentAktørId />);
    userEvent.type(screen.getAllByRole("textbox")[0], "12345678900");
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("error")).toBeVisible();
      expect(screen.getByText("Kunne ikke hente aktør-ID")).toBeVisible();
    });
  });

  it("viser feilmelding om personnummer ikke er fylt ut", async () => {
    render(<HentAktørId />);
    userEvent.click(screen.getAllByRole("button")[0]);
    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
    });
  });
});
