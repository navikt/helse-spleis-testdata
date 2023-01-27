import styles from "./OpprettDokumenter.module.css";
import {Checkbox} from "../../components/Checkbox";
import {Card} from "../../components/Card";
import {useFormContext} from "react-hook-form";
import React from "react";

export const DiverseCard = React.memo(() => {
  const { register } = useFormContext();

  return (
    <Card>
      <h2 className={styles.Title}>Diverse</h2>
      <div className={styles.CardContainer}>
        <Checkbox
          data-testid="medlemskap"
          label="Medlemskap avklart"
          {...register("medlemskapAvklart")}
        />
      </div>
    </Card>
  );
});
