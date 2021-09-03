import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FetchButton } from "./FetchButton";

describe("FetchButton", () => {
  it("viser ok-ikon om status < 400", () => {
    render(<FetchButton isFetching={false} status={399} />);

    expect(screen.queryByTestId("success")).toBeVisible();
    expect(screen.queryByTestId("error")).toBeNull();
  });

  it("viser error-ikon om status >= 400", () => {
    render(<FetchButton isFetching={false} status={400} />);

    expect(screen.queryByTestId("success")).toBeNull();
    expect(screen.queryByTestId("error")).toBeVisible();
  });

  it("viser ikke statusikoner uten status-prop", () => {
    render(<FetchButton isFetching={false} />);

    expect(screen.queryByTestId("success")).toBeNull();
    expect(screen.queryByTestId("error")).toBeNull();
  });

  it("viser spinner når fetching foregår", () => {
    render(<FetchButton isFetching />);

    expect(screen.queryByTestId("spinner")).toBeVisible();
  });

  it("viser ikke spinner når fetching ikke foregår", () => {
    render(<FetchButton isFetching={false} />);

    expect(screen.queryByTestId("spinner")).toBeNull();
  });
});
