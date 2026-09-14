package com.cams.backend.localbody;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ward")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "village_municipality_id", nullable = false)
    private VillageMunicipality villageMunicipality;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "ward_number", length = 20)
    private String wardNumber;

    @Column(name = "centroid_lat", precision = 9, scale = 6)
    private BigDecimal centroidLat;

    @Column(name = "centroid_lng", precision = 9, scale = 6)
    private BigDecimal centroidLng;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public VillageMunicipality getVillageMunicipality() {
        return villageMunicipality;
    }

    public void setVillageMunicipality(VillageMunicipality villageMunicipality) {
        this.villageMunicipality = villageMunicipality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public BigDecimal getCentroidLat() {
        return centroidLat;
    }

    public void setCentroidLat(BigDecimal centroidLat) {
        this.centroidLat = centroidLat;
    }

    public BigDecimal getCentroidLng() {
        return centroidLng;
    }

    public void setCentroidLng(BigDecimal centroidLng) {
        this.centroidLng = centroidLng;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
