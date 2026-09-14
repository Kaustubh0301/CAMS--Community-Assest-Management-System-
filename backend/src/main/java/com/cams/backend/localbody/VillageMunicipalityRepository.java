package com.cams.backend.localbody;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface VillageMunicipalityRepository extends JpaRepository<VillageMunicipality, UUID> {
}
