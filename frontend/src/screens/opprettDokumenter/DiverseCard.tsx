import styles from "./OpprettDokumenter.module.css";
import { Card } from "../../components/Card";
import { useFormContext } from "react-hook-form";
import React from "react";
import { Sykmelding } from "./Sykmelding";
import { FormSelect } from "../../components/FormSelect";

export const DiverseCard = React.memo(() => {
  const { register, setValue, watch } = useFormContext();
  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const lovmeMockFinnes = false; // akkurat nå finnes det ikke noen LovMe-mock som kan plukke opp Medlemskapsvurdering

  return (
    <Card>
      <h2 className={styles.Title}>Diverse</h2>
      {lovmeMockFinnes && (
        <div className={styles.CardContainer}>
          <FormSelect
            label="Medlemskapsvurdering"
            options={["JA", "NEI", "UAVKLART", "UAVKLART_MED_BRUKERSPORSMAAL"]}
            {...register("medlemskapVerdi")}
            onChange={(val) => {
              const verdi =
                val.target.options[val.target.options.selectedIndex].value;
              setValue("medlemskapVerdi", verdi);
            }}
          />
        </div>
      )}
      {skalSendeSykmelding && <Sykmelding />}
    </Card>
  );
});
