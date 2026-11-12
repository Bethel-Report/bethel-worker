package com.bethelreport.worker.repository;

import java.util.UUID;

import com.bethelreport.worker.model.ReportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportJobRepository extends JpaRepository<ReportJob, UUID> {
}
