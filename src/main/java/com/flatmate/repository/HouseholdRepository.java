package com.flatmate.repository;

import com.flatmate.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<Household, Integer>{
}
