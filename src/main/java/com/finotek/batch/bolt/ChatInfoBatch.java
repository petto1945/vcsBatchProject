package com.finotek.batch.bolt;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.finotek.batch.controller.UserChatInfoController;
import com.finotek.batch.controller.operChatController;

@Component
public class ChatInfoBatch {

	@Autowired
	private UserChatInfoController chatInfo;
	
	@Autowired
	private operChatController operChat;
	
	@Autowired
	private MongoTemplate mongo;

	/**
	 * CHAT_INFO Batch 
	 * 
	 * MongoDB Collection
	 * 
	 * VCS_REPORT_ANSWER_STATUS
	 * VCS_REPORT_DASH_COUNT
	 * VCS_REPORT_GRAPH
	 * VCS_REPORT_QUESTION
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	@Scheduled(fixedDelayString = "20000")
	public void chatInfo() {
		System.out.println(" chatInfo Start Come On");
		List<HashMap> findMongo = mongo.find(new Query(new Criteria("batchYn").is("N")), HashMap.class,
				"LOG_CHAT_INFO");
		System.out.println(" chatInfo Batch || size : " + findMongo.size());
		
		if(findMongo != null || findMongo.size() != 0) {
			for (HashMap hashMap : findMongo) {
				chatInfo.dashCountBolt(hashMap);
				chatInfo.questionProcessBolt(hashMap);
				chatInfo.writeUserProcessBolt(hashMap);
				batchYnUpdate(hashMap, "LOG_CHAT_INFO");				
			}
		}
	}
	
	/**
	 * USER_CONN Batch
	 * 
	 * MongoDB Collection
	 * 
	 * VCS_REPORT_DASH_COUNT
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Scheduled(fixedDelayString = "30000")
	public void userConn() {
		System.out.println(" userConn Start Batch");
		List<HashMap> findMongo = mongo.find(new Query(new Criteria("batchYn").is("N")), HashMap.class,
				"LOG_USER_CONN");
		System.out.println(" userConn Batch || size : " + findMongo.size());
		
		if(findMongo != null || findMongo.size() != 0) {
			for (HashMap hashMap : findMongo) {
				chatInfo.userConnection(hashMap);
				batchYnUpdate(hashMap, "LOG_USER_CONN");				
			}
		}
	}
	
	/**
	 * OPER_CHAT Batch
	 * 
	 * MongoDB Collection
	 * 
	 * VCS_REPORT_OPER_CHAT_LOG
	 * VCS_REPORT_DAILY
	 * VCS_REPORT_INFO
	 * VCS_REPORT_OPER_CHAT_MONTHLY
	 * VCS_REPORT_OPER_CHAT_YEARLY
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	@Scheduled(fixedDelayString = "15000")
	public void operChat() {
		System.out.println(" operChat Batch Start ");
		List<HashMap> findMongo = mongo.find(new Query(new Criteria("batchYn").is("N")), HashMap.class,
				"LOG_OPER_CHAT");
		System.out.println(" operChat Batch || size : " + findMongo.size());
		int num = 0;
		
		if(findMongo != null || findMongo.size() != 0) {
			for (HashMap hashMap : findMongo) {
				System.out.println("check num : " + ++num);
				operChat.calcTimerInfo(hashMap);
				operChat.calcTimeDailyProcessBolt(hashMap, "Daily");		// 일별 상담원 개입 정보
				operChat.calcTimeDailyProcessBolt(hashMap, "Monthly");	// 월별 상담원 개입 정보
				operChat.calcTimeDailyProcessBolt(hashMap, "Yearly");		// 연별 상담원 개입 정보
				batchYnUpdate(hashMap, "LOG_OPER_CHAT");				
			}
		}
	}
	
	/**
	 * 배치가 성공적으로 돌 경우 MongoDB 배치 완료 표시
	 * 
	 * MongoDB Collection
	 * 
	 * LOG_OPER_CHAT
	 * LOG_USER_CONN
	 * LOG_CHAT_INFO
	 * 
	 * @param hashMap
	 * @param colName
	 */
	@SuppressWarnings({ "rawtypes" })
	private void batchYnUpdate(HashMap hashMap, String colName) {
		System.out.println(" batchYnUpdate Start HashMap : " + hashMap);
		System.out.println(" collectionName : " + colName);
		
		String find = "userid";
		String userid = hashMap.containsKey("userid") ? hashMap.get("userid").toString() : "";
		String date = hashMap.containsKey("date") ? hashMap.get("date").toString() : "";
		
		if(colName.equals("LOG_OPER_CHAT")) {
			find = "roomNo";
			userid = hashMap.containsKey("roomNo") ? hashMap.get("roomNo").toString() : "";
		}
		
		Criteria criteriaBatch = new Criteria(find).is(userid).and("date").is(date).and("batchYn").is("N");
		Query queryBatch = new Query(criteriaBatch);
		mongo.updateFirst(queryBatch, new Update().set("batchYn", "Y"), colName);
	}
}
