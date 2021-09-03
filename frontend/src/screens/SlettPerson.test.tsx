import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { SlettPerson } from "./SlettPerson";
import userEvent from "@testing-library/user-event";

jest.mock("../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = jest.fn();

const mockFetchSuccess = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 204 } as Response)
  );
};

const mockFetchError = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 404 } as Response)
  );
};

describe("SlettPerson", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("sletter person", async () => {
    mockFetchSuccess();
    render(<SlettPerson />);
    userEvent.type(screen.getByRole("textbox"), "12345678900");
    userEvent.click(screen.getByRole("button"));
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("success")).toBeVisible();
    });
  });

  it("viser feilmelding om sletting feiler", async () => {
    mockFetchError();
    render(<SlettPerson />);
    userEvent.type(screen.getByRole("textbox"), "12345678900");
    userEvent.click(screen.getByRole("button"));
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByTestId("error")).toBeVisible();
      expect(screen.getByText("Kunne ikke slette person")).toBeVisible();
    });
  });

  it("viser feilmelding om personnummer ikke er fylt ut", async () => {
    render(<SlettPerson />);
    userEvent.click(screen.getByRole("button"));
    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
    });
  });
});
