import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Modal from "./Modal";

describe("Modal", () => {
  it("renders nothing when closed", () => {
    render(
      <Modal isOpen={false} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>,
    );

    expect(screen.queryByText("Content")).not.toBeInTheDocument();
  });

  it("moves focus into the dialog when opened", () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Confirm">
        <button type="button">Confirm</button>
      </Modal>,
    );

    expect(screen.getByRole("dialog")).toHaveFocus();
  });

  it("calls onClose on Escape", async () => {
    const onClose = vi.fn();
    render(
      <Modal isOpen={true} onClose={onClose} title="Confirm">
        <button type="button">Confirm</button>
      </Modal>,
    );

    await userEvent.keyboard("{Escape}");

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("traps Tab focus within the dialog", async () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <button type="button">First</button>
        <button type="button">Last</button>
      </Modal>,
    );

    const first = screen.getByRole("button", { name: "First" });
    const last = screen.getByRole("button", { name: "Last" });

    last.focus();
    await userEvent.tab();
    expect(first).toHaveFocus();

    await userEvent.tab({ shift: true });
    expect(last).toHaveFocus();
  });

  it("restores focus to the previously focused element on close", () => {
    render(
      <div>
        <button type="button">Open</button>
      </div>,
    );
    const trigger = screen.getByRole("button", { name: "Open" });
    trigger.focus();

    const { rerender } = render(
      <Modal isOpen={true} onClose={vi.fn()} title="Confirm">
        <button type="button">Confirm</button>
      </Modal>,
    );

    expect(screen.getByRole("dialog")).toHaveFocus();

    rerender(
      <Modal isOpen={false} onClose={vi.fn()} title="Confirm">
        <button type="button">Confirm</button>
      </Modal>,
    );

    expect(trigger).toHaveFocus();
  });
});
