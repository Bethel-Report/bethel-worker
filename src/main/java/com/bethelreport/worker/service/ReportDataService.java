package com.bethelreport.worker.service;

import java.util.List;
import java.util.UUID;

import com.bethelreport.worker.dto.ReportData;
import com.bethelreport.worker.dto.ReportData.ActivityInfo;
import com.bethelreport.worker.dto.ReportData.BethelInfo;
import com.bethelreport.worker.dto.ReportData.FormAnswer;
import com.bethelreport.worker.dto.ReportData.MemberInfo;
import com.bethelreport.worker.dto.ReportData.OfficerInfo;
import com.bethelreport.worker.dto.ReportData.PeriodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportDataService {

	private static final String PARAM_BETHEL_ID = "bethelId";
	private static final String PARAM_PERIOD_ID = "periodId";

	private final JdbcClient jdbcClient;

	public ReportDataService(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public ReportData consolidate(UUID periodId, UUID bethelId) {
		log.info("Consolidando dados para período {} e bethel {}", periodId, bethelId);

		BethelInfo bethel = fetchBethelInfo(bethelId);
		PeriodInfo period = fetchPeriodInfo(periodId);
		List<MemberInfo> members = fetchMembers(bethelId);
		List<ActivityInfo> activities = fetchActivities(bethelId, periodId);
		List<OfficerInfo> officers = fetchOfficers(bethelId, periodId);
		List<FormAnswer> answers = fetchFormAnswers(bethelId, periodId);

		return new ReportData(bethel, period, answers, members, activities, officers);
	}

	private BethelInfo fetchBethelInfo(UUID bethelId) {
		return jdbcClient
				.sql("SELECT bethel_id AS bethelId, number, name, city, state FROM bethel WHERE bethel_id = :bethelId")
				.param(PARAM_BETHEL_ID, bethelId).query(BethelInfo.class).single();
	}

	private PeriodInfo fetchPeriodInfo(UUID periodId) {
		return jdbcClient.sql("""
				SELECT period_id AS periodId, year, description,
				       opening_date AS openingDate, closing_date AS closingDate
				FROM report_period WHERE period_id = :periodId""").param(PARAM_PERIOD_ID, periodId)
				.query(PeriodInfo.class).single();
	}

	private List<MemberInfo> fetchMembers(UUID bethelId) {
		return jdbcClient.sql("""
				SELECT member_id AS memberId, name, birth_date AS birthDate,
				       initiation_date AS initiationDate, status
				FROM member WHERE bethel_id = :bethelId ORDER BY name""").param(PARAM_BETHEL_ID, bethelId)
				.query(MemberInfo.class).list();
	}

	private List<ActivityInfo> fetchActivities(UUID bethelId, UUID periodId) {
		return jdbcClient.sql("""
				SELECT a.activity_id AS activityId, a.title, a.description,
				       a.activity_date AS activityDate
				FROM activity a
				JOIN report_period rp ON a.activity_date BETWEEN rp.opening_date AND rp.closing_date
				WHERE a.bethel_id = :bethelId AND rp.period_id = :periodId
				ORDER BY a.activity_date""").param(PARAM_BETHEL_ID, bethelId).param(PARAM_PERIOD_ID, periodId)
				.query(ActivityInfo.class).list();
	}

	private List<OfficerInfo> fetchOfficers(UUID bethelId, UUID periodId) {
		return jdbcClient.sql("""
				SELECT bo.bethel_officer_id AS officerId, m.name AS memberName,
				       bo.position, bo.start_date AS startDate, bo.end_date AS endDate
				FROM bethel_officer bo
				JOIN member m ON bo.member_id = m.member_id
				JOIN bethel_term bt ON bo.bethel_term_id = bt.bethel_term_id
				JOIN report_period rp ON bt.start_date <= rp.closing_date
				     AND bt.end_date >= rp.opening_date
				WHERE bt.bethel_id = :bethelId AND rp.period_id = :periodId
				ORDER BY bo.position""").param(PARAM_BETHEL_ID, bethelId).param(PARAM_PERIOD_ID, periodId)
				.query(OfficerInfo.class).list();
	}

	private List<FormAnswer> fetchFormAnswers(UUID bethelId, UUID periodId) {
		return jdbcClient.sql("""
				SELECT ff.section, ff.label, ra.value,
				       ff.type AS fieldType, ff.financial
				FROM report r
				JOIN report_answer ra ON r.report_id = ra.report_id
				JOIN form_field ff ON ra.field_id = ff.field_id
				WHERE r.bethel_id = :bethelId AND r.period_id = :periodId
				ORDER BY ff.sort_order""").param(PARAM_BETHEL_ID, bethelId).param(PARAM_PERIOD_ID, periodId)
				.query(FormAnswer.class).list();
	}
}
