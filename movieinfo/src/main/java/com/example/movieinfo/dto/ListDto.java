package com.example.movieinfo.dto;
import lombok.*;
@Getter
@Setter
public class ListDto {
	private String colname = "mname", keyword = "";
	private int pageNum = 1, listCnt = 5, totalPage;
}