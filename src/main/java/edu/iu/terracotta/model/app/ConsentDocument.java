package edu.iu.terracotta.model.app;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "terr_consent_document")
@Entity
public class ConsentDocument {
    @Column(name = "consent_document_id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long consentDocumentId;

    @Column(name = "title")
    private String title;

    @Column(name = "file_pointer")
    private String filePointerd;

    @Column(name = "html")
    @Lob
    private String html;

    @JoinColumn(name = "experiment_experiment_id")
    @OneToOne(orphanRemoval = true)
    private Experiment experiment;

    @Column(name = "lms_assignment_id")
    private String lmsAssignmentId;

    public String getLmsAssignmentId() {
        return lmsAssignmentId;
    }

    public void setLmsAssignmentId(String lmsAssignmentId) {
        this.lmsAssignmentId = lmsAssignmentId;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getFilePointerd() {
        return filePointerd;
    }

    public void setFilePointerd(String filePointerd) {
        this.filePointerd = filePointerd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getConsentDocumentId() {
        return consentDocumentId;
    }

    public void setConsentDocumentId(Long consentDocumentId) {
        this.consentDocumentId = consentDocumentId;
    }
}