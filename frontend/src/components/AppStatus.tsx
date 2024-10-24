import React from "react";
import styles from "./AppStatus.module.css";

export const AppStatus: React.FC = () => {
  return (
    <div className={styles.AppStatus}>
      <iframe src="https://grafana.nav.cloud.nais.io/d-solo/a66ahZ0Wk/alt-i-ett-dashboard?orgId=1&refresh=1m&from=now-1m&to=now&panelId=237&var-datasource=dev-gcp" />
    </div>
  );
};
