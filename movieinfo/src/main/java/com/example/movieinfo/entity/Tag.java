package com.example.movieinfo.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "tagtbl")
@Data
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)//자동 증가 IDENTITY=mysql, mariaDB, AUTO=oracle
	private Long tcode;

	@Column(nullable = false)
	private Long tmcode;

	@Column(nullable = false)
	private double tid;

	@Column(nullable = false, length = 20)
	private String tword;

	@Column(nullable = false, length = 30)
	private String ttype;

	@Column(nullable = false)
	private int tcount;
}
