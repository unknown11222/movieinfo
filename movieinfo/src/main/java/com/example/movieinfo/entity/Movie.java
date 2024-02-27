package com.example.movieinfo.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "movietbl")
@Data
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)//자동 증가 IDENTITY=mysql, mariaDB, AUTO=oracle
	private Long mcode;

	@Column(nullable = false, length = 100)//not null 설정
	private String mname;

	@Column(nullable = false, length = 50)
	private String mdirector;

	@Column(nullable = false, length = 50)
	private String mnation;

	@Column(nullable = false, length = 100)
	private String mgenre;

	@Column(nullable = false, length = 100)
	private String mactor;

	@Column(nullable = false, length = 10)
	private String mopen;

	@Column(length = 2000)
	private String msynopsis;

	@Column(length = 100)
	private String moriname;

	@Column(length = 100)
	private String msysname;
}