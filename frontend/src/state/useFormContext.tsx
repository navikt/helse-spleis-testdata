import type { Component } from "solid-js";
import { createContext, useContext } from "solid-js";
import { useForm, UseFormResult } from "./useForm";

export const FormContext = createContext<UseFormResult>();

export const useFormContext = () => {
  return useContext(FormContext);
};

export const FormProvider: Component = (props) => {
  const form = useForm();
  return (
    <FormContext.Provider value={form}>{props.children}</FormContext.Provider>
  );
};

export const withFormProvider = (Component: Component) => (props) =>
  (
    <FormProvider>
      <Component {...props} />
    </FormProvider>
  );
