import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";

vi.mock("@/services", () => ({
  experimentCopyCandidateService: {
    getAll: vi.fn(),
    importCandidate: vi.fn(),
    dismiss: vi.fn()
  }
}));

import { experimentCopyCandidateService } from "@/services";
import { experimentCopyCandidate } from "./experiment-copy-candidate.module";

describe("experimentCopyCandidate store", () => {
  let store;

  beforeEach(() => {
    setActivePinia(createPinia());
    store = experimentCopyCandidate();
    vi.clearAllMocks();
    vi.spyOn(console, "error").mockImplementation(() => {});
  });

  it("starts with no candidates", () => {
    expect(store.copyCandidates).toEqual([]);
  });

  describe("fetchAll", () => {
    it("stores the returned candidates on success", async () => {
      experimentCopyCandidateService.getAll.mockResolvedValue({
        data: [
          { id: "c1", experimentTitle: "Reading Study" },
          { id: "c2", experimentTitle: "Writing Study" }
        ]
      });

      const result = await store.fetchAll();

      expect(store.copyCandidates).toHaveLength(2);
      expect(result).toBe(store.copyCandidates);
    });

    it("defaults to an empty list when the response has no data", async () => {
      experimentCopyCandidateService.getAll.mockResolvedValue({});

      await store.fetchAll();

      expect(store.copyCandidates).toEqual([]);
    });

    it("logs and swallows errors, leaving state untouched", async () => {
      experimentCopyCandidateService.getAll.mockRejectedValue(new Error("boom"));

      const result = await store.fetchAll();

      expect(result).toEqual([]);
      expect(store.copyCandidates).toEqual([]);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe("importCandidate", () => {
    it("removes the candidate and returns the new import on success", async () => {
      store.copyCandidates = [
        { id: "c1", experimentTitle: "Reading Study" },
        { id: "c2", experimentTitle: "Writing Study" }
      ];
      experimentCopyCandidateService.importCandidate.mockResolvedValue({
        data: { id: "import-1", status: "PROCESSING" }
      });

      const result = await store.importCandidate("c1");

      expect(experimentCopyCandidateService.importCandidate).toHaveBeenCalledWith("c1");
      expect(result).toEqual({ id: "import-1", status: "PROCESSING" });
      expect(store.copyCandidates).toEqual([{ id: "c2", experimentTitle: "Writing Study" }]);
    });

    it("logs and swallows errors, leaving state untouched", async () => {
      store.copyCandidates = [{ id: "c1", experimentTitle: "Reading Study" }];
      experimentCopyCandidateService.importCandidate.mockRejectedValue(new Error("boom"));

      const result = await store.importCandidate("c1");

      expect(result).toBeNull();
      expect(store.copyCandidates).toHaveLength(1);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe("dismiss", () => {
    it("removes the candidate on success", async () => {
      store.copyCandidates = [
        { id: "c1", experimentTitle: "Reading Study" },
        { id: "c2", experimentTitle: "Writing Study" }
      ];
      experimentCopyCandidateService.dismiss.mockResolvedValue({});

      await store.dismiss("c1");

      expect(experimentCopyCandidateService.dismiss).toHaveBeenCalledWith("c1");
      expect(store.copyCandidates).toEqual([{ id: "c2", experimentTitle: "Writing Study" }]);
    });

    it("logs and swallows errors, leaving state untouched", async () => {
      store.copyCandidates = [{ id: "c1", experimentTitle: "Reading Study" }];
      experimentCopyCandidateService.dismiss.mockRejectedValue(new Error("boom"));

      await store.dismiss("c1");

      expect(store.copyCandidates).toHaveLength(1);
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe("reset", () => {
    it("clears the list", () => {
      store.copyCandidates = [{ id: "c1" }];
      store.reset();

      expect(store.copyCandidates).toEqual([]);
    });
  });
});
