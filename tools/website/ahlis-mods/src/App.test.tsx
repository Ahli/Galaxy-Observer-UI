import { describe, it, expect } from "vitest";
import { render } from "@testing-library/react";
import App from "./App";

describe("App component", () => {
  it("renders without crashing", () => {
    const { container } = render(<App />);
    expect(container).toBeTruthy();
  });
});
