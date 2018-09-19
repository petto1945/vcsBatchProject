package com.finotek.batch.controller;

import java.util.HashMap;
import java.util.Iterator;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/test2")
public class topicController {
	
	@Autowired
	private MongoTemplate mongo;
	
	/**
	 * 처음 받은 토픽명에 따르 다른 MongoDB Collection 에 Insert 한다.
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/topic", method=RequestMethod.POST)
	public void topic(@RequestBody HashMap<String, Object> request) {
		System.out.println(" topic request : ======================= : " + request);
		
		String topic =  request.get("topic").toString();
		Document data = (Document)  this.setDoc((HashMap<String, Object>) request.get("data"));
		data.put("batchYn", "N");
		if( data != null) {
			switch(topic) {
				case "CHAT_INFO" :
					this.chatInfo(data);
					break;
				case "USER_CONN" :
					this.userConn(data);
					break;
				case "OPER_CHAT" :
					this.operChat(data);
					break;
			}
		}
	}
	
	private void userConn(Document request) {
		mongo.insert(request, "LOG_USER_CONN");
	}
	
	private void chatInfo(Document request) {
		mongo.insert(request, "LOG_CHAT_INFO");
	}
	
	private void operChat(Document request) {
		mongo.insert(request, "LOG_OPER_CHAT");
	}
	
	/**
	 * HashMap -> Document
	 * @param doc
	 * @param jsonObj
	 * @return Document
	 */
	@SuppressWarnings("rawtypes")
	private Document setDoc(HashMap<String, Object> jsonObj){
		Document doc = new Document();
		Iterator itr = jsonObj.keySet().iterator();
		while(itr.hasNext()){
			String strKey = itr.next().toString();
			doc.put(strKey, jsonObj.get(strKey));
		}
		System.out.println(" return doc =============== : " + doc);
		return doc;
	}
	
}
