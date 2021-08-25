import type { Component } from "solid-js";
import { createMemo, Match, Switch } from "solid-js";
import classNames from "classnames";
import styles from "./Period.module.css";
import differenceInDays from "date-fns/differenceInDays";
import format from "date-fns/format";

import sykmeldingIcon from "material-design-icons/image/svg/production/ic_healing_24px.svg";
import søknadIcon from "material-design-icons/action/svg/production/ic_description_24px.svg";
import inntektsmeldingIcon from "material-design-icons/editor/svg/production/ic_attach_money_24px.svg";

interface PeriodProps {
  type: "sykmelding" | "søknad" | "inntektsmelding";
  start: Date;
  end: Date;
  daysInTimeline: number;
  timelineStart: Date;
  timelineEnd: Date;
}

export const Period: Component<PeriodProps> = (props) => {
  const daysSinceStart = createMemo(
    () => differenceInDays(props.start, props.timelineStart) + 1
  );

  const daysSinceEnd = createMemo(
    () => differenceInDays(props.end, props.timelineStart) + 1
  );

  const left = createMemo(
    () => (daysSinceStart() / props.daysInTimeline) * 100
  );

  const width = createMemo(
    () => ((daysSinceEnd() - daysSinceStart()) / props.daysInTimeline) * 100
  );

  return (
    <div
      class={classNames(styles.Period, styles[props.type])}
      style={{ "--period-width": `${width()}%`, "--period-left": `${left()}%` }}
    >
      <Switch>
        <Match when={props.type === "sykmelding"}>
          <img src={sykmeldingIcon} alt="" />
        </Match>
        <Match when={props.type === "søknad"}>
          <img src={søknadIcon} alt="" />
        </Match>
        <Match when={props.type === "inntektsmelding"}>
          <img src={inntektsmeldingIcon} alt="" />
        </Match>
      </Switch>
      <p>
        {format(props.start, "dd.MM.yyyy")} - {format(props.end, "dd.MM.yyyy")}
      </p>
    </div>
  );
};
