import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { DeleteButton } from "./DeleteButton";
import userEvent from "@testing-library/user-event";
import { FormProvider, useForm } from "react-hook-form";
import { validateFødselsnummer } from "../formValidation";
import { FormInput } from "../../components/FormInput";

jest.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = jest.fn();

const mockFetchSuccess = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 200 } as Response)
  );
};

const mockFetchError = () => {
  (fetch as jest.Mock).mockImplementationOnce(() =>
    Promise.resolve({ status: 500 } as Response)
  );
};

const FormWrapper: React.FC = ({ children }) => {
  const form = useForm();
  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(() => null)}>
        {children}
        <FormInput
          data-testid="fnr"
          label="Fødselsnummer"
          {...form.register("fnr", {
            required: "Fødselsnummer må fylles ut",
            validate: validateFødselsnummer,
          })}
        />
      </form>
    </FormProvider>
  );
};

describe("DeleteButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("sletter person", async () => {
    mockFetchSuccess();
    render(<DeleteButton errorCallback={jest.fn} />, { wrapper: FormWrapper });
    userEvent.type(screen.getByRole("textbox"), "12345678900");
    userEvent.click(screen.getByRole("button"));
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("button")).toHaveTextContent("✔️️");
    });
  });

  it("viser feilmelding om sletting feiler", async () => {
    mockFetchError();
    let errorCallbackWasCalled = false;
    const errorCallback = (value) => {
      if (value == null) return;
      errorCallbackWasCalled = true;
      expect(value).toBe("Sletting av person feilet");
    };
    render(<DeleteButton errorCallback={errorCallback} />, {
      wrapper: FormWrapper,
    });
    userEvent.type(screen.getByRole("textbox"), "12345678900");
    userEvent.click(screen.getByRole("button"));
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("button")).toHaveTextContent("☠️");
    });
    expect(errorCallbackWasCalled).toBeTruthy();
  });

  it("gjør ingenting om fødselsnummer ikke inneholder elleve tegn", async () => {
    render(<DeleteButton errorCallback={jest.fn} />, { wrapper: FormWrapper });
    userEvent.type(screen.getByRole("textbox"), "1234567890");
    userEvent.click(screen.getByRole("button"));

    // Må vente litt, før vi kan sjekke at det ikke skjedde noe
    await new Promise((r) => setTimeout(r, 500));

    expect(fetch).toHaveBeenCalledTimes(0);
    expect(screen.getByRole("button")).toHaveTextContent("❌");
  });
});
