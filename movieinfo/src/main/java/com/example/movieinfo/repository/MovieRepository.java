package com.example.movieinfo.repository;

import com.example.movieinfo.entity.Movie;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface MovieRepository extends JpaRepository<Movie, Long> {
	//페이징 처리를 위한 Pageable 객체가 두번쩨 패러미터로 작성
	Page<Movie> findByMnameContains(String keyword, Pageable pb);

	Page<Movie> findByMdirectorContains(String keyword, Pageable pb);
}