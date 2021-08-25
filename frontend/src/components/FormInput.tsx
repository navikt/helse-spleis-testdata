import type { Component, JSX } from "solid-js";
import type {
  FormErrors,
  RegisterFunction,
  ValidationFunction,
} from "../state/useForm";
import { Accessor } from "solid-js";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";

interface FormInputProps extends JSX.InputHTMLAttributes<HTMLInputElement> {
  register: RegisterFunction;
  errors: Accessor<FormErrors>;
  label: string;
  name: string;
  id: string;
  defaultValue?: any;
  validation?: ValidationFunction;
}

export const FormInput: Component<FormInputProps> = (props) => {
  return (
    <InputLabel>
      {props.label}
      <Input
        id={props.id}
        name={props.name}
        type={props.type ?? "text"}
        required={props.required}
        ref={props.register(props.validation, props.defaultValue)}
      />
      {props.errors()[props.name] && (
        <ErrorMessage label-for={props.id}>
          {props.errors()[props.name]}
        </ErrorMessage>
      )}
    </InputLabel>
  );
};
