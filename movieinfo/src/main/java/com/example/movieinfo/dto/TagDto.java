package com.example.movieinfo.dto;
import lombok.*;
@Getter
@Setter
public class TagDto {
	private Long tcode, tmcode;
	private double tid;
	private String tword, ttype;
	private int tcount;
}