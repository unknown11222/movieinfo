package com.example.movieinfo.controller;

import com.example.movieinfo.dto.*;
import com.example.movieinfo.service.MovieService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@Slf4j
public class MovieController {
	private ModelAndView mv;
	@Autowired
	private MovieService mServ;

	@GetMapping("/")
	public ModelAndView home(ListDto list, HttpSession session) {
		log.info("home()");
		mv = mServ.getMovieList(list, session);
		return mv;
	}

	@GetMapping("writeForm")
	public String writeForm() {
		log.info("writeForm()");
		return "writeForm";
	}

	@PostMapping("writeProc")
	public String writeProc(@RequestPart List<MultipartFile> files, MovieDto movie, HttpSession session, RedirectAttributes rttr) {
		log.info("writeProc()");
		String view = mServ.insertMovie(files, movie, session, rttr);
		return view;
	}

	@GetMapping("detail")
	public ModelAndView detail(Long mcode) {
		log.info("detail()");
		mv = mServ.getMovie(mcode);
		return mv;
	}

	@GetMapping("updateForm")
	public ModelAndView updateForm(Long mcode) {
		log.info("updateForm()");
		mv = mServ.getMovie(mcode);
		mv.setViewName("updateForm");
		return mv;
	}

	@PostMapping("updateProc")
	public String updateProc(@RequestPart List<MultipartFile> files, MovieDto movie, HttpSession session, RedirectAttributes rttr) {
		log.info("updateProc()");
		String view = mServ.updateMovie(files, movie, session, rttr);
		return view;
	}

	@GetMapping("delete")
	public String deleteProc(Long mcode, String msysname, HttpSession session, RedirectAttributes rttr){
		log.info("deleteProc()");
		String view=mServ.deleteMovie(mcode, msysname,session, rttr);
		return view;
	}
}