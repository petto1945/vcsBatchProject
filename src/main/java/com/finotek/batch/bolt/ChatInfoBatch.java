package com.finotek.batch.bolt;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChatInfoBatch {
	
	@Scheduled(fixedDelayString = "1000")
	public void test() {
		System.out.println(" ========= test Batch Ok !!!! ============");
	}
}
