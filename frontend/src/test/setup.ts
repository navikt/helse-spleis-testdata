import "@testing-library/jest-dom/vitest";
import { vi, beforeEach } from "vitest";

// Mock console.error to reduce noise in tests
Object.assign(console, {
  error: vi.fn(),
});

// Clear localStorage before each test
beforeEach(() => {
  localStorage.clear();
});

