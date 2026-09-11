import { defineStore } from "pinia";

import { experimentCopyCandidateService } from "@/services";

export const experimentCopyCandidate = defineStore("experimentCopyCandidate", {
  state: () => ({
    copyCandidates: []
  }),

  actions: {
    async fetchAll() {
      try {
        const response = await experimentCopyCandidateService.getAll();

        this.copyCandidates = response?.data || [];

        return this.copyCandidates;
      } catch (e) {
        console.error("experimentCopyCandidate/fetchAll | catch", e);

        return [];
      }
    },

    async importCandidate(candidateId) {
      try {
        const response = await experimentCopyCandidateService.importCandidate(candidateId);

        this.removeCandidate(candidateId);

        return response?.data;
      } catch (e) {
        console.error("experimentCopyCandidate/importCandidate | catch", e);

        return null;
      }
    },

    async dismiss(candidateId) {
      try {
        await experimentCopyCandidateService.dismiss(candidateId);

        this.removeCandidate(candidateId);
      } catch (e) {
        console.error("experimentCopyCandidate/dismiss | catch", e);
      }
    },

    removeCandidate(candidateId) {
      this.copyCandidates = this.copyCandidates.filter(
        candidate => candidate.id !== candidateId
      );
    },

    reset() {
      this.copyCandidates = [];
    }
  }
});
