package com.example.movieinfo.repository;

import com.example.movieinfo.entity.Tag;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Transactional
public interface TagRepository extends JpaRepository<Tag, Long> {
	@Query("select t.tword from Tag t where t.tmcode = :tmc")
	List<String> findByTmcode(@Param("tmc") Long mcode);

	@Query(value = "delete from tagtbl where tmcode = :tmc", nativeQuery = true)
	@Modifying
	void deleteByTmcode(@Param("tmc") Long mcode);
}