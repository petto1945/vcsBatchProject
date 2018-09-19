package com.finotek.batch.controller;

import java.util.HashMap;
import java.util.Iterator;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class testController {

	@Autowired
	private MongoTemplate mongo;

	/**
	 * 사용자정보 체크
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/userconnection", method = RequestMethod.POST)
	public void userConnection() {
		HashMap<String, Object> findMongo = mongo.findOne(new Query(new Criteria("batchYn").is("N")), HashMap.class, "LOG_USER_CONN");
		if(findMongo == null) {
			return;
		}
		
		System.out.println("userConnection input data ================== : " + findMongo);
		
		String userid = findMongo.containsKey("userid") ? findMongo.get("userid").toString() : "";
		String date = findMongo.containsKey("date") ? findMongo.get("date").toString() : "";

		if (!"".equals(userid) && !"".equals(date)) {
			findMongo.put("qustType", "userConn");
			this.dashCountProcessBolt(findMongo);
			
			Criteria criteria = new Criteria("userid").is(userid).and("date").is(date).and("batchYn").is("N");
			Query query = new Query(criteria);
			mongo.updateFirst(query, new Update().set("batchYn", "Y"), "LOG_USER_CONN");
		}
	}

	/**
	 * DashCountBolt 질문 성공 / 실패 확인
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/dashcountbolt", method = RequestMethod.POST)
	public void dashCountBolt() {
		HashMap<String, Object> findMongo = mongo.findOne(new Query(new Criteria("batchYn").is("N")), HashMap.class, "LOG_CHAT_INFO");
		if(findMongo == null) {
			return;
		}
		System.out.println("dashCountBolt input data ================== : " + findMongo);
		System.out.println("dashCountBolt input qustType ================== : " + findMongo.get("qustType"));
		
		String status = findMongo.containsKey("status") ? findMongo.get("status").toString() : "";
		String date = findMongo.containsKey("date") ? findMongo.get("date").toString() : "";
		String qustType = findMongo.containsKey("qustType") ? findMongo.get("qustType").toString() : "";
		String userid = findMongo.containsKey("userid") ? findMongo.get("userid").toString() : "";

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		map.put("date", date);
		map.put("qustType", qustType);

		if (!"".equals(status) && !"".equals(date) && !"".equals(qustType)) {
			if ("true".equals(status)) {
				this.dashCountProcessBolt(map);
			} else {
				map.put("qustType", "fail");
				this.dashCountProcessBolt(map);
			}
			Criteria criteria = new Criteria("userid").is(userid).and("date").is(date).and("batchYn").is("N");
			Query query = new Query(criteria);
			mongo.updateFirst(query, new Update().set("batchYn", "Y"), "LOG_CHAT_INFO");
		}
	}

	/**
	 * QuestionProcessBolt 서비스 로직
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/questionprocessbolt", method = RequestMethod.POST)
	public void questionProcessBolt() {
		HashMap<String, Object> findMongo = mongo.findOne(new Query(new Criteria("batchYn").is("N")), HashMap.class, "LOG_CHAT_INFO");
		if(findMongo == null) {
			return;
		}
		
		System.out.println("questionProcessBolt input data ================== : " + findMongo);
		System.out.println("questionProcessBolt input qustType ================== : " + findMongo.get("qustType"));
		
		String userid = findMongo.containsKey("userid") ? findMongo.get("userid").toString() : "";
		String date = findMongo.containsKey("date") ? findMongo.get("date").toString() : "";
		String result = findMongo.containsKey("status") ? findMongo.get("status").toString() : "";
		
		try {
			if (!"".equals(result)) {
				findMongo.remove("userid");
				Document doc = new Document();
				doc = this.setDoc(doc, findMongo);
				doc.remove("status");
	
				if ("true".equals(result)) {
					// 1. VCS_REPORT_QUESTION 저장 시작
					doc = this.saveVcsReportQuestion(doc);
				}
	
				// 2. VCS_REPORT_ANSWER_STATUS 저장 시작
				doc.put("result", result);
				this.saveVcsReportAnswerStatus(doc);
				
				Criteria criteria = new Criteria("userid").is(userid).and("date").is(date).and("batchYn").is("N");
				Query query = new Query(criteria);
				mongo.updateFirst(query, new Update().set("batchYn", "Y"), "LOG_CHAT_INFO");
	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	/**
	 * WriteUserProcessBolt 시간별 사용자 정보 Insert 또는 Update
	 * 
	 * @param request
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@RequestMapping(value = "writeuserprocessbolt", method = RequestMethod.POST)
	public void writeUserProcessBolt() {
		HashMap<String, Object> findMongo = mongo.findOne(new Query(new Criteria("batchYn").is("N")), HashMap.class, "LOG_CHAT_INFO");
		if(findMongo == null) {
			return;
		}
		
		System.out.println("writeuserprocessbolt input data ================== : " + findMongo);
		System.out.println("writeuserprocessbolt input qustType ================== : " + findMongo.get("qustType"));
	
		String userid = findMongo.containsKey("userid") ? findMongo.get("userid").toString() : "";
		String date = findMongo.containsKey("date") ? findMongo.get("date").toString() : "";
		String time = findMongo.containsKey("time") ? findMongo.get("time").toString() : "";
	
		if (!"".equals(userid) && !"".equals(date) && !"".equals(time)) {
			findMongo.remove("status");
			findMongo.remove("qustid_1");
			findMongo.remove("qustid_2");
			findMongo.remove("qustid_3");
			findMongo.remove("qusttype");
			findMongo.remove("question");
	
			Document doc = new Document();
			doc = this.setDoc(doc, findMongo);
	
			Criteria criteria = new Criteria("userid");
			criteria.is(userid).and("date").is(date).and("time").is(time);
			Query query = new Query(criteria);
	
			HashMap<String, Object> findMap = mongo.findOne(query, HashMap.class, "VCS_REPORT_GRAPH");
			System.out.println(" mongoFind =============== : " + findMap);
			if (findMap == null) {
				doc.put("count", 1);
				System.out.println("doc =========> " + doc);
				doc.remove("batchYb");
				mongo.insert(doc, "VCS_REPORT_GRAPH");
			} else {
				doc.remove("batchYb");
				mongo.updateFirst(query, new Update().inc("count", 1), "VCS_REPORT_GRAPH");
			}
			
			Criteria criteriaBatch = new Criteria("userid").is(userid).and("date").is(date).and("batchYn").is("N");
			Query queryBatch = new Query(criteria);
			mongo.updateFirst(query, new Update().set("batchYn", "Y"), "LOG_CHAT_INFO");
		}
	}

	/**
	 * DashCountProcessBolt (건수 저장)
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	private void dashCountProcessBolt(HashMap<String, Object> request) {
		System.out.println("DashCountProcessBolt input data ================== : " + request);
		System.out.println("DashCountProcessBolt input qustType ================== : " + request.get("qustType"));

		String qustType = request.get("qustType").toString();
		String strDate = request.get("date").toString();

		HashMap<String, Object> findMap = mongo.findOne(new Query(Criteria.where("date").is(strDate)), HashMap.class,
				"VCS_REPORT_DASH_COUNT");

		try {
			if (findMap != null) {
				this.updateDashData(findMap, strDate, qustType);
			} else {
				this.insertDashData(findMap, strDate, qustType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DashCountProcessBolt VCS_REPORT_DASH_COUNT 오늘날짜 데이터가 없는경우 insert
	 * 
	 * @param dashCollection
	 * @param strDate
	 * @param qustType
	 * @throws Exception
	 */
	private void insertDashData(HashMap<String, Object> dashCollection, String strDate, String qustType)
			throws Exception {
		int conn_count = 0;
		int general_count = 0;
		int scenario_count = 0;
		int answer_failed_count = 0;

		if ("userConn".equals(qustType)) {
			conn_count++;
		} else if ("normal".equals(qustType)) {
			general_count++;
		} else if ("scenario".equals(qustType)) {
			scenario_count++;
		} else {
			answer_failed_count++;
		}

		Document user = new Document();
		user.put("date", strDate);
		user.put("conn_count", conn_count);
		user.put("general_count", general_count);
		user.put("scenario_count", scenario_count);
		user.put("answer_failed_count", answer_failed_count);

		mongo.insert(user, "VCS_REPORT_DASH_COUNT");
	}

	/**
	 * DashCountProcessBolt VCS_REPORT_DASH_COUNT 오늘날짜가 있는경우 카운팅 후 Update
	 * 
	 * @param dashCollection
	 * @param strDate
	 * @param qustType
	 */
	private void updateDashData(HashMap<String, Object> dashCollection, String strDate, String qustType) {
		Criteria criteria = new Criteria("date").is(strDate);
		Query query = new Query(criteria);
		Update update = new Update();
		String collectionName = "VCS_REPORT_DASH_COUNT";
		if ("userConn".equals(qustType)) {
			mongo.updateFirst(query, update.inc("conn_count", 1), collectionName);
		} else if ("normal".equals(qustType)) {
			mongo.updateFirst(query, update.inc("general_count", 1), collectionName);
		} else if ("scenario".equals(qustType)) {
			mongo.updateFirst(query, update.inc("scenario_count", 1), collectionName);
		} else {
			mongo.updateFirst(query, update.inc("answer_failed_count", 1), collectionName);
		}
	}

	/**
	 * QuestionProcessBolt Collection Name : VCS_REPORT_QUESTION 일반/시나리오 성공한 정보를
	 * 저장한다. 랭킹용
	 * 
	 * @param db
	 * @param dataObj
	 * @return
	 * @throws Exception
	 */
	private Document saveVcsReportQuestion(Document doc) throws Exception {
		mongo.insert(doc, "VCS_REPORT_QUESTION");
		return doc;
	}

	/**
	 * QuestionProcessBolt Collection Name : VCS_REPORT_ANSWER_STATUS 질문에 대한 성공/실패
	 * 정보를 저장한다.
	 * 
	 * @param db
	 * @param dataObj
	 * @param doc
	 * @throws Exception
	 */
	private void saveVcsReportAnswerStatus(Document doc) throws Exception {
		doc.remove("status");
		doc.remove("qustid_1");
		doc.remove("qustid_2");
		doc.remove("qustid_3");
		doc.remove("qustType");
		doc.remove("itype");
		mongo.insert(doc, "VCS_REPORT_ANSWER_STATUS");
	}

	/**
	 * HashMap -> Document
	 * 
	 * @param doc
	 * @param jsonObj
	 * @return Document
	 */
	@SuppressWarnings("rawtypes")
	private Document setDoc(Document doc, HashMap<String, Object> jsonObj) {
		Iterator itr = jsonObj.keySet().iterator();
		while (itr.hasNext()) {
			String strKey = itr.next().toString();
			doc.put(strKey, jsonObj.get(strKey));
		}
		System.out.println(" return doc =============== : " + doc);
		return doc;
	}

}
