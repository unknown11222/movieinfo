package com.example.movieinfo.util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.example.movieinfo.dto.TagDto;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashTagProccess {
	public List<TagDto> addTagWord(String content, long mcode) throws Exception {
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
		String accessKey = "668aa5dd-e166-4865-b99e-48c38e23b8de";// 발급받은 API Key
		String analysisCode = "ner";// 언어 분석 코드
		String text = content;// 분석할 텍스트 데이터
		Gson gson = new Gson();

		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();

		argument.put("analysis_code", analysisCode);
		argument.put("text", text);

		request.put("argument", argument);

		URL url;
		Integer responseCode = null;
		String responBodyJson = null;
		Map<String, Object> responeBody = null;

		url = new URL(openApiURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setRequestProperty("Authorization", accessKey);

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.write(gson.toJson(request).getBytes("UTF-8"));
		wr.flush();
		wr.close();
		//여기까지 etri로 분석용 데이터를 전송하는 부분

		//여기부터 결과값을 받는 부분
		responseCode = con.getResponseCode();
		InputStream is = con.getInputStream();//입력 통로
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();

		String inputLine = "";
		while ((inputLine = br.readLine()) != null)
			sb.append(inputLine);
		responBodyJson = sb.toString();

		// http 요청 오류 시 처리
		if (responseCode != 200) {
			// 오류 내용 출력
			log.error("[error] {}", responBodyJson);
			return null;
		}

		responeBody = gson.fromJson(responBodyJson, Map.class);
		log.info(responBodyJson);//다운로드한 Json 객체 확인
		Integer result = ((Double) responeBody.get("result")).intValue();
		Map<String, Object> returnObject;
		List<Map> sentences;

		// 분석 요청 오류 시 처리
		if (result != 0) {
			// 오류 내용 출력
			log.error("[error] {}", responeBody.get("result"));
			return null;
		}

		// 분석 결과 활용
		returnObject = (Map<String, Object>) responeBody.get("return_object");
		sentences = (List<Map>) returnObject.get("sentence");

		Map<String, TagDto> wordMap = new HashMap<String, TagDto>();
		List<TagDto> words = null;//최종 데이터 저장 리스트 DB

		for (Map<String, Object> sentence : sentences) {
			// 형태소 분석기 결과 수집 및 정렬
			List<Map<String, Object>> analysisResult = (List<Map<String, Object>>) sentence.get("NE");

			for (Map<String, Object> wInfo : analysisResult) {
				String type = (String) wInfo.get("type");
				String word = (String) wInfo.get("text");
				//map에 저장되어 있는 지 확인
				//반복으로 처리하는 부분이라 같은 word가 이미 map에 저장되어 있으면 새로 tag를 만들지 않고 카운트만 늘림.
				//map에 저장되어 있지 않은 word 라면 새로 tag를 생성하여 map에 저장
				TagDto tag = wordMap.get(word);

				if (tag == null) {//만약 tag가 없다면
					tag = new TagDto();
					//각 데이터 setting
					tag.setTmcode(mcode);
					tag.setTid((Double) wInfo.get("id"));
					tag.setTword(word);
					tag.setTtype(type);
					tag.setTcount(1);
					wordMap.put(word, tag);
				} else {
					tag.setTcount(tag.getTcount() + 1);
				}
			}
		}

		if (0 < wordMap.size()) {//정렬하여 최종 목록 형성. map은 순서가 없음
			words = new ArrayList<TagDto>(wordMap.values());
			words.sort((words1, words2) -> {
				return (int) (words1.getTid() - words2.getTid());//id 값으로 정렬
			});
		}
		return words;//완성된 해시 태그 목록을 넘김
	}//method end
}//class end