package com.bethelreport.worker.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReportData(BethelInfo bethel, PeriodInfo period, List<FormAnswer> answers, List<MemberInfo> members,
		List<ActivityInfo> activities, List<OfficerInfo> officers) {

	public record BethelInfo(UUID bethelId, Integer number, String name, String city, String state) {
	}

	public record PeriodInfo(UUID periodId, Integer year, String description, LocalDate openingDate,
			LocalDate closingDate) {
	}

	public record MemberInfo(UUID memberId, String name, LocalDate birthDate, LocalDate initiationDate, String status) {
	}

	public record ActivityInfo(UUID activityId, String title, String description, LocalDate activityDate) {
	}

	public record OfficerInfo(UUID officerId, String memberName, String position, LocalDate startDate,
			LocalDate endDate) {
	}

	public record FormAnswer(String section, String label, String value, String fieldType, boolean financial) {
	}
}
