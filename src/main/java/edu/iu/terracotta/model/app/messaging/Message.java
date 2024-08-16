package edu.iu.terracotta.model.app.messaging;

import edu.iu.terracotta.model.LtiUserEntity;
import edu.iu.terracotta.model.PlatformDeployment;
import edu.iu.terracotta.model.app.Condition;
import edu.iu.terracotta.model.app.Experiment;
import edu.iu.terracotta.model.app.ExposureGroupCondition;
import edu.iu.terracotta.model.app.messaging.enums.MessageStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "terr_messaging_message")
public class Message extends BaseMessageEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "exposure_group_condition_id")
    private ExposureGroupCondition exposureGroupCondition;

    @OneToOne(mappedBy = "message")
    private MessageConfiguration configuration;

    @OneToOne(mappedBy = "message")
    private MessageContent content;

    @ManyToOne
    @JoinColumn(
        name = "group_id",
        nullable = false
    )
    private MessageGroup group;

    @Transient
    public LtiUserEntity getOwner() {
        return group.getOwner();
    }

    @Transient
    public long getExposureGroupConditionId() {
        return exposureGroupCondition.getExposureGroupConditionId();
    }

    @Transient
    public Condition getCondition() {
        return exposureGroupCondition.getCondition();
    }

    @Transient
    public long getConditionId() {
        return exposureGroupCondition.getCondition().getConditionId();
    }

    @Transient
    public long getExposureId() {
        return exposureGroupCondition.getExposure().getExposureId();
    }

    @Transient
    public long getGroupId() {
        return exposureGroupCondition.getGroup().getGroupId();
    }

    @Transient
    public Experiment getExperiment() {
        return exposureGroupCondition.getCondition().getExperiment();
    }

    @Transient
    public long getExperimentId() {
        return getExperiment().getExperimentId();
    }

    @Transient
    public PlatformDeployment getPlatformDeployment() {
        return getExperiment().getPlatformDeployment();
    }

    @Transient
    public MessageStatus getStatus() {
        return configuration.getStatus();
    }

}
