package com.project.gripgravity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a bouldering route in the gym
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Tape color used to mark the route holds.
     */
    @Column(nullable = false)
    private String color;

    /**
     * Physical section of the wall
     */
    @Column(name = "wall_section", nullable = false)
    private String wallSection;

    @Column(name = "setter_name", nullable = false)
    private String setterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "official_grade", nullable = false)
    private Grade officialGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "community_grade")
    private Grade communityGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RouteStatus status = RouteStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Set when the route is first transitioned to ACTIVE
     */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
