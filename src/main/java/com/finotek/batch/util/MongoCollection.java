package com.finotek.batch.util;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoCollection {
	
	// CHAT_INFO 질문 성공/실패 정보
	public static String VCS_REPORT_DASH_COUNT = "VCS_REPORT_DASH_COUNT";			// 메인 대시보드 카운팅 정보
	public static String VCS_REPORT_ANSWER_STATUS = "VCS_REPORT_ANSWER_STATUS";	// 일반/시나리오 성공한 정보
	public static String VCS_REPORT_QUESTION = "VCS_REPORT_QUESTION";				// 랭킹정보
	public static String VCS_REPORT_GRAPH = "VCS_REPORT_GRAPH";					// 질문한 사용자 정보
	
	// OPER_CHAT 상담원 개입 정보
	public static String VCS_REPORT_DASH_OPER = "VCS_REPORT_DASH_OPER";					// 상담원 개입 건수
	public static String VCS_REPORT_OPER_CHAT_LOG = "VCS_REPORT_OPER_CHAT_LOG";			// 상담원 개입 정보
	public static String VCS_REPORT_OPER_CHAT_DAILY = "VCS_REPORT_OPER_CHAT_DAILY";		// 일별 상담원 개입 정보
	public static String VCS_REPORT_OPER_CHAT_INFO = "VCS_REPORT_OPER_CHAT_INFO"; 		// 일별 상담원별 개입 정보
	public static String VCS_REPORT_OPER_CHAT_MONTHLY = "VCS_REPORT_OPER_CHAT_MONTHLY";	// 월별 상담원 개입 정보
	public static String VCS_REPORT_OPER_CHAT_YEARLY = "VCS_REPORT_OPER_CHAT_YEARLY"; 	// 년벌 상담원 개입 정보
	
	// Topic CollectionName 기본 토픽 정보 저
	public static String LOG_CHAT_INFO = "LOG_CHAT_INFO";	// 질문 성공/실패 정보
	public static String LOG_OPER_CHAT = "LOG_OPER_CHAT";	// 상담원 개입 정보
	public static String LOG_USER_CONN = "LOG_USER_CONN";	// 사용자 정보

}
