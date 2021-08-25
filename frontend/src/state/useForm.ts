import { Accessor, batch, createSignal } from "solid-js";

export type InputElements = {
  [key: string]: HTMLInputElement | HTMLInputElement[];
};

export type FormValues = { [key: string]: any };

export type FormErrors = { [key: string]: string };

export type ValidationFunction = (value: string) => false | string;

export type RegisterFunction = (
  validation?: ValidationFunction,
  defaultValue?: any
) => (element: HTMLInputElement | HTMLFieldSetElement) => void;

export type DeregisterFunction = (name: string) => void;

const validationMessage = (
  element: HTMLInputElement,
  validation?: ValidationFunction
): string | void => {
  if (element.validationMessage) return element.validationMessage;
  if (!element.required && element.value.length === 0) return;

  const maybeMessage = validation?.(element.value);
  if (maybeMessage) {
    element.setCustomValidity(maybeMessage);
    return maybeMessage;
  }
};

const getElementValue = (element: HTMLInputElement): string | number | boolean =>
  element.type === "checkbox" ? element.checked : element.value;

const updateAriaAttributes = (element: HTMLInputElement): void => {
  if (element.type === "checkbox") {
    element.setAttribute("aria-checked", Boolean(getElementValue(element)).toString());
  }
};

const assignDefaultValue = (
  element: HTMLInputElement,
  defaultValue?: any
) => {
  if (element.type === "checkbox") {
    element.checked = Boolean(defaultValue) ?? element.checked;
  } else {
    element.value = defaultValue ?? element.value;
  }
};

export type UseFormResult = {
  register: RegisterFunction;
  deregister: DeregisterFunction;
  values: Accessor<FormValues>;
  setValue: (name: string, newValue: any) => void;
  errors: Accessor<FormErrors>;
  clearError: (...names: string[]) => void;
};

export const useForm = (): UseFormResult => {
  const [values, setValues] = createSignal<FormValues>({});
  const [errors, setErrors] = createSignal<FormErrors>({});

  const updateValue = (element: HTMLInputElement) => {
    const existingValue = values()[element.name];
    const newValue = getElementValue(element);

    if (!existingValue) {
      setValues((old) => ({ ...old, [element.name]: newValue }));
    } else if (Array.isArray(existingValue)) {
      setValues((old) => ({ ...old, [element.name]: [...existingValue, newValue] }));
    } else {
      setValues((old) => ({ ...old, [element.name]: [existingValue, newValue] }));
    }
  };

  const registerInputElement = (element: HTMLInputElement, validation?: ValidationFunction, defaultValue?: any): void => {
    assignDefaultValue(element, defaultValue);
    updateAriaAttributes(element);
    updateValue(element);

    element.onchange = (event: InputEvent & { target: HTMLInputElement }) => {
      updateAriaAttributes(element);
      setValues((old) => ({
        ...old,
        [element.name]: element.type === "checkbox" ? event.target.checked : event.target.value
      }));
    };

    element.onblur = () => {
      setTimeout(() => {
        element.setCustomValidity("");
        element.checkValidity();
        const message = validationMessage(element, validation);
        if (message) {
          setErrors((old) => ({ ...old, [element.name]: message }));
          element.classList.toggle("error", true);
        } else {
          setErrors((old) => ({ ...old, [element.name]: null }));
          element.classList.toggle("error", false);
        }
      }, 0);
    };
  };

  const registerFieldSetElement = (element: HTMLFieldSetElement, validation?: ValidationFunction): void => {
    console.log("registering fieldset element");
  };

  const register = (validation?: ValidationFunction, defaultValue?: any) => {
    return (element: HTMLInputElement | HTMLFieldSetElement) => {
      if (element.type === "fieldset") {
        registerFieldSetElement(element as HTMLFieldSetElement, validation);
      } else {
        registerInputElement(element as HTMLInputElement, validation, defaultValue);
      }
    };
  };

  const deregister = (name: string) => {
    delete values()[name];
    setValues((old) => old);
  };

  const clearError = (...names: string[]): void => {
    batch(() => {
      names.forEach(name => setErrors(old => ({ ...old, [name]: null })));
    });
  };

  const setValue = (name: string, value: any) => {
    const element = Array.from(document.getElementsByName(name)).pop() as HTMLInputElement;
    element.value = value;
    setValues(old => ({ ...old, [name]: value }));
  };

  return { register, deregister, errors, values, setValue, clearError };
};
