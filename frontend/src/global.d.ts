declare type SystemMessageObject = {
  id: string;
  text: string;
  dismissable?: boolean;
  timeToLiveMs?: number;
};
