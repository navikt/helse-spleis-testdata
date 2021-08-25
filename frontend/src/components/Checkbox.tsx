import type { Accessor, Component, JSX } from "solid-js";
import { Show, splitProps } from "solid-js";
import type { FormErrors, RegisterFunction, ValidationFunction } from "../state/useForm";
import styles from "./Checkbox.module.css";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";
import classNames from "classnames";

interface CheckboxProps extends JSX.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  name: string;
  id: string;
  register: RegisterFunction;
  errors?: Accessor<FormErrors>;
  validation?: ValidationFunction;
  defaultValue?: any;
}

export const Checkbox: Component<CheckboxProps> = (props) => {
  const [local, others] = splitProps(props, ["label", "register"]);
  return (
    <InputLabel class={classNames(styles.Label, others.class)}>
      <Input
        type="checkbox"
        id={others.id}
        name={others.name}
        required={others.required}
        ref={local.register(others.validation, others.defaultValue)}
        class={styles.Checkbox}
        {...others}
      />
      <div>
        {local.label}
        <Show when={others.errors?.()[others.name]}>
          <ErrorMessage label-for={others.id}>
            {others.errors()[others.name]}
          </ErrorMessage>
        </Show>
      </div>
    </InputLabel>
  );
};
