import styles from "./OpprettDokumenter.module.css";
import {Checkbox} from "../../components/Checkbox";
import {Card} from "../../components/Card";
import {useFormContext} from "react-hook-form";
import React from "react";
import {Select} from "../../components/Select";
import {FormSelect} from "../../components/FormSelect";

export const DiverseCard = React.memo(() => {
  const { register , setValue} = useFormContext();

  return (
    <Card>
      <h2 className={styles.Title}>Diverse</h2>
      <div className={styles.CardContainer}>
        <Checkbox
          data-testid="medlemskap"
          label="Medlemskap avklart"
          {...register("medlemskapAvklart")}
        />
          <FormSelect
              label="Medlemskapsvurdering"
              options={[
                  { value: "", label: "(Ingen)" },
                  "JA",
                  "NEI",
                  "UAVKLART",
                  "UAVKLART_MED_BRUKERSPORMSMAAL"
              ]}
              {...register("medlemskapVerdi")}
              onChange={val => {
                  const verdi = val.target.options[val.target.options.selectedIndex].value
                  setValue("medlemskapVerdi", verdi)
              } }
          />
      </div>
    </Card>
  );
});
