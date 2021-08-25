import styles from "./Timeline.module.css";
import type { Component } from "solid-js";
import { createMemo, For } from "solid-js";
import { Period } from "./Period";

import differenceInDays from "date-fns/differenceInDays";
import startOfDay from "date-fns/startOfDay";
import endOfDay from "date-fns/endOfDay";
import addDays from "date-fns/addDays";
import subDays from "date-fns/subDays";
import { PeriodObject } from "../../screens/opprettPerson/OpprettPerson";

const earliestStartDate = (a: PeriodObject, b: PeriodObject): number =>
  a.start.getMilliseconds() - b.start.getMilliseconds();

const latestEndDate = (a: PeriodObject, b: PeriodObject): number =>
  b.end.getMilliseconds() - a.end.getMilliseconds();

const getTimelineStartDate = (periods: PeriodObject[]): Date =>
  [...periods].sort(earliestStartDate)[0].start;

const getTimelineEndDate = (periods: PeriodObject[]): Date =>
  [...periods].sort(latestEndDate)[periods.length - 1].end;

interface TimelineProps {
  periods: PeriodObject[];
}

export const Timeline: Component<TimelineProps> = (props) => {
  const padding = 2;

  const boundaries = createMemo(() => ({
    start: subDays(getTimelineStartDate(props.periods), padding),
    end: addDays(getTimelineEndDate(props.periods), padding),
  }));

  const daysInTimeline = createMemo(() => {
    return differenceInDays(boundaries().end, boundaries().start) + 1;
  });

  return (
    <div class={styles.Timeline}>
      <For each={props.periods}>
        {(period) => (
          <Period
            type={period.type}
            start={startOfDay(period.start)}
            end={endOfDay(period.end)}
            daysInTimeline={daysInTimeline()}
            timelineStart={boundaries().start}
            timelineEnd={boundaries().end}
          />
        )}
      </For>
    </div>
  );
};
