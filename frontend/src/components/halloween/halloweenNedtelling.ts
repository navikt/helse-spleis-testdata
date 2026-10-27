import { differenceInCalendarDays } from "date-fns";

const halloween = (år: number): Date => new Date(år, 9, 31);

export const dagerTilHalloween = (idag: Date): number => {
  const iÅr = differenceInCalendarDays(halloween(idag.getFullYear()), idag);
  if (iÅr >= 0) return iÅr;
  return differenceInCalendarDays(halloween(idag.getFullYear() + 1), idag);
};

export const halloweenTekst = (dager: number): string => {
  if (dager === 0) return "Glad Halloween! Knask eller knep!";
  if (dager === 1) return "Halloween er i morgen. Er kostymet klart?";
  return `${dager} netter igjen til Halloween`;
};
