package edu.iu.terracotta.model.canvas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "canvas_api_token_scope")
public class CanvasAPITokenScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "canvas_api_token_token_id",
        nullable = false
    )
    private CanvasAPITokenEntity canvasAPITokenEntity;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "canvas_api_scope_id",
        nullable = false
    )
    private CanvasAPIScope canvasAPIScope;

}
