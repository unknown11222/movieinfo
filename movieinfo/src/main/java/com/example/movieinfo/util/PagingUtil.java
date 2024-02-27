package com.example.movieinfo.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PagingUtil {
	private int totalPage, pageNum, pageCnt; //전체 페이지 개수, 현재 보이는 페이지 번호, 페이지당 보여질 번호 개수
	private String urlStr; //링크 url

	public String makePaging() {
		String pageHtml = null;
		StringBuffer sb = new StringBuffer();

		//현재 그룹
		int curGroup = (pageNum % pageCnt) > 0 ? pageNum / pageCnt + 1 : pageNum / pageCnt;
		//그룹의 시작번호
		int start = (curGroup * pageCnt) - (pageCnt - 1);
		//그룹의 끝번호
		int end = (curGroup * pageCnt) >= totalPage ? totalPage : curGroup * pageCnt;

		//page html 작성
		if (start != 1) {
			sb.append("<a class='pno' href='/" + urlStr + "pageNum=" + (start - 1) + "'>◀</a>");
		}

		for (int i = start; i <= end; i++) {
			if (pageNum == i)
				sb.append("<font class='pno'>" + i + "</font>");
			else
				sb.append("<a class='pno' href='/" + urlStr + "pageNum=" + i + "'>" + i + "</a>");
		}

		if (end != totalPage) {
			sb.append("<a class='pno' href='/" + urlStr + "pageNum=" + (end + 1) + "'>▶</a>");
		}//<a class='pno' href='/?pageNum=6'>▶</a>

		//Stringbuffer -> String 변환
		pageHtml = sb.toString();

		return pageHtml;
	}
}//class end