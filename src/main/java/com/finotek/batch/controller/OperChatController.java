package com.finotek.batch.controller;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@RestController
@RequestMapping(value = "/test3")
public class operChatController {

	@Autowired
	private MongoTemplate mongo;

	private static HashMap<String, Object> statiHm;

	/**
	 * CalctimerInfo - 개입/종료 시간계산 하
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/calctimerinfo", method = RequestMethod.POST)
	public void calcTimerInfo() {
		List<HashMap> findMongo = mongo.find(new Query(new Criteria("batchYn").is("N")), HashMap.class,
				"LOG_OPER_CHAT");
		
		for (HashMap hashMap : findMongo) {
			String roomNo = hashMap.containsKey("roomNo") ? hashMap.get("roomNo").toString() : "";
			String date = hashMap.containsKey("date") ? hashMap.get("date").toString() : "";
			String chatTime = hashMap.containsKey("chatTime") ? hashMap.get("chatTime").toString() : "";
			String operName = hashMap.containsKey("operName") ? hashMap.get("operName").toString() : "";
			String operEmail = hashMap.containsKey("operatorEmail") ? hashMap.get("operatorEmail").toString() : "";
			try {
				if (!"".equals(roomNo) && !"".equals(date) && !"".equals(chatTime) && !"".equals(operName)
						&& !"".equals(operEmail)) {

					if (statiHm == null) {
						statiHm = new HashMap<String, Object>();
						statiHm.put(roomNo, hashMap);
						hashMap.remove("batchYn");
						updateJoinCnt(hashMap, "");
					} else {
						HashMap<String, Object> tHm = (HashMap<String, Object>) statiHm.get(roomNo);
						if (tHm == null) {
							statiHm.put(roomNo, hashMap);
							updateJoinCnt(hashMap, "");
						} else {
							String fDate = hashMap.get("datß").toString();
							String fChatTime = hashMap.get("chatTime").toString();

							String tDate = tHm.get("date").toString();
							String tChatTime = tHm.get("chatTime").toString();

							String sDate = fDate + fChatTime;
							String eDate = tDate + tChatTime;

							int diffmin = (int) getSubrCal(sDate, eDate);

							tHm.put("chatTime", diffmin);
							tHm.put("sTime", fChatTime);
							tHm.put("eTime", tChatTime);
							tHm.remove("roomNo");
							
							System.out.println(tHm);
							CalcTimeProcessBolt(tHm); // 상담로그
							
							statiHm.remove(roomNo);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 상담로그 insert
	 * VCS_REPORT_OPER_CHAT_LOG
	 * @param request
	 */
	@SuppressWarnings("rawtypes")
	public void CalcTimeProcessBolt(HashMap<String, Object> request) {
		 //insert
		Document doc = new Document();
		Iterator itr = request.keySet().iterator();
		while(itr.hasNext()){
			String strKey = itr.next().toString();
			doc.put(strKey, request.get(strKey));
		}
		mongo.insert(doc, "VCS_REPORT_OPER_CHAT_LOG") ;
	}

	public long getSubrCal(String pSDate, String pEDate) throws java.text.ParseException {
		SimpleDateFormat smt = new SimpleDateFormat("yyyyMMddHHmm");
		Date sDate = smt.parse(pSDate);
		Date eDate = smt.parse(pEDate);
		long diff = sDate.getTime() - eDate.getTime();
		long diffmin = (diff / 1000) / 60;
		return diffmin;
	}

	/**
	 * 채팅상담사 추가 update 또는 카운팅 update
	 * @param data
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateJoinCnt(HashMap<String, Object> data, String type) throws Exception {
		String date = data.get("date").toString();
		String operName = data.get("operName").toString();
		String operEmail = data.get("operatorEmail").toString();
		String collectionName = "VCS_REPORT_DASH_OPER";

		HashMap<String, Object> fMap = mongo.findOne(new Query(new Criteria("date").is(date)),
				HashMap.class, collectionName);
		System.out.println("fMap : " + fMap);
		System.out.println("operEmail : " + operEmail);
		if (fMap == null) {
			// 내부문서
			Document inDoc = new Document();
			inDoc.put("operName", operName);
			inDoc.put("operEmail", operEmail);
			inDoc.put("chatCount", 1);

			List<Document> arrOperators = new ArrayList<Document>();
			arrOperators.add(inDoc);

			Document doc = new Document();
			doc.put("date", date);
			doc.put("opChatTodayCount", 1);
			doc.put("operators", arrOperators);

			mongo.insert(doc, collectionName);

		} else {
			mongo.updateFirst(new Query(new Criteria("date").is(date)), new Update().inc("opChatTodayCount", 1),
					collectionName);
			
			MongoCollection dashOperCollection = mongo.getCollection("VCS_REPORT_DASH_OPER");
			Bson bsonFilter = Filters.and(Filters.eq("date", date)
                    , Filters.elemMatch("operators", Filters.eq("operEmail", operEmail))
            );
			Document operDoc = (Document) dashOperCollection.find(bsonFilter).first();
			System.out.println("operDoc : " + operDoc);
			
			if (operDoc == null || operDoc.size() == 0) {
				// 내부문서
				Document inDoc = new Document();
				inDoc.put("operName", operName);
				inDoc.put("operEmail", operEmail);
				inDoc.put("chatCount", 1);
				mongo.updateFirst(new Query(new Criteria("date").is(date)), new Update().push("operators", inDoc),
						collectionName);
			} else {
				// update
				dashOperCollection.updateOne(bsonFilter, new Document("$inc", new Document("operators.$.chatCount", 1)));
			}
		}
	}

}
