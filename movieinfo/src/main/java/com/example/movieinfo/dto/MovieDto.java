package com.example.movieinfo.dto;
import lombok.*;
@Getter
@Setter
public class MovieDto {
	private Long mcode;
	private String mname, mdirector, mnation, mgenre, mactor, mopen, msynopsis, moriname, msysname;
}