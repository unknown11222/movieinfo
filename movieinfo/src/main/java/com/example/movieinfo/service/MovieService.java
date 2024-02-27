package com.example.movieinfo.service;

import com.example.movieinfo.dto.*;
import com.example.movieinfo.entity.*;
import com.example.movieinfo.repository.*;
import com.example.movieinfo.util.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.*;

@Service
@Slf4j
public class MovieService {
	private ModelAndView mv;

	@Autowired
	private MovieRepository mRepo;
	@Autowired
	private TagRepository tRepo;

	private ModelMapper mapper = new ModelMapper();

	//영화 목록 처리 메소드
	public ModelAndView getMovieList(ListDto list, HttpSession session) {
		log.info("getMovieList()");

		//Pageable 생성
		Pageable pb = PageRequest.of(list.getPageNum() - 1,//페이지 번호
				list.getListCnt(), //페이지 목록 개수
				Sort.Direction.DESC, //정렬 방식
				"mcode" //정렬 기준 컬럼명
		);
		Page<Movie> result = null;//페이징된 조회 결과를 받는 객체
		if (list.getColname().equals("mname")) result = mRepo.findByMnameContains(list.getKeyword(), pb);//제목 검색
		else result = mRepo.findByMdirectorContains(list.getKeyword(), pb);//감독 검색

		//페이지 형태의 결과를 목록(Entity List) 형태로 변환
		List<Movie> meList = result.getContent();

		//Entity List->Dto List로 변환
		List<MovieDto> mList = mapper.map(meList, new TypeToken<List<MovieDto>>() {
		}.getType());

		//세션에 listDto 저장
		if (list.getKeyword().equals("")) list.setColname("mname");//기본값 으로 변경

		session.setAttribute("list", list);//뒤로가기 버튼에서 사용
		mv = new ModelAndView();
		mv.addObject("mList", mList);

		//페이지 html 추가
		//전체 페이지 개수를 listDto에 저장
		int totalPage = result.getTotalPages();
		list.setTotalPage(totalPage);
		String paging = getPaging(list);
		mv.addObject("paging", paging);
		mv.setViewName("index");//보여질 html 지정
		return mv;
	}

	private String getPaging(ListDto list) {
		log.info("getPaging()");
		String pageHtml = null, urlStr = "?";
		int pageCnt = 2;//페이지에서 보여지는 페이지 번호 개수
		PagingUtil paging = new PagingUtil(list.getTotalPage(), list.getPageNum(), pageCnt, urlStr);
		pageHtml = paging.makePaging();

		return pageHtml;
	}

	public String insertMovie(List<MultipartFile> files, MovieDto movie, HttpSession session, RedirectAttributes rttr) {
		String view = null, msg = null;
		//업로드 하는 파일의 이름을 먼저 꺼냄
		String upFileName = files.get(0).getOriginalFilename();
		try {
			//파일 업로드 먼저
			if (!upFileName.equals("")) fileUpload(files, session, movie);//파일 업로드

			//DB에 영화정보 저장
			//dto<->Entity 변환
			Movie mEntity = mapper.map(movie, Movie.class);
			//save 하지 전에 mEntity 에는 mcode가 없음
			mRepo.save(mEntity);//save 메소드로 저장

			//태그로 쓸 단어를 OPEN API 로 처리
			tagProcess(mEntity);//영화 정보->태그 생성

			view = "redirect:/";
			msg = "등록 성공";
		} catch (Exception e) {
			e.printStackTrace();
			view = "redirect:writeForm";
			msg = "등록 실패";
		}
		return view;
	}

	private void tagProcess(Movie mEntity) throws Exception {
		String tagStr = mEntity.getMname() + ", "
				+ mEntity.getMdirector() + ", "
				+ mEntity.getMactor() + ", "
				+ mEntity.getMsynopsis() + ", "
				+ mEntity.getMgenre();

		//검색할 문장에서 한글, 영어, 숫자를 제외한 문자 제거
		tagStr = tagStr.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9,]", " ");//^=not 뒤에 붙은 것들 제외
		//원문:bbbaaacb->bbb cb(replace)
		//replaceAll도 replace와 동일. 단, 타겟 문장에 정규식을 사용할 수 있음
		//ex)"[^ㄱ-ㅎㅏ-ㅣ가-힣]" 한글 제외 나머지 변경 (공백 없음)

		HashTagProccess htp = new HashTagProccess();
		List<TagDto> tList = htp.addTagWord(tagStr, mEntity.getMcode());

		int cnt = 0;//태그 개수 제한용
		for (TagDto tag : tList) {
			Tag tEntity = mapper.map(tag, Tag.class);
			tRepo.save(tEntity);
			cnt++;
			if (cnt >= 10) break;//10개 이하만 DB에 저장
		}
	}

	private void fileUpload(List<MultipartFile> files, HttpSession session, MovieDto movie) throws IOException {
		log.info("fileUpload()");
		String sysname = null, oriname = null;
		String realPath = session.getServletContext().getRealPath("/");
		realPath += "upload/";
		File folder = new File(realPath);

		if (folder.isDirectory() == false) folder.mkdir();

		//파일 저장
		MultipartFile mf = files.get(0);
		oriname = mf.getOriginalFilename();
		sysname = System.currentTimeMillis() + oriname.substring(oriname.lastIndexOf("."));
		File file = new File(realPath + sysname);
		mf.transferTo(file);

		//파일명을 movieDto에 추가
		movie.setMsysname(sysname);
		movie.setMoriname(oriname);
	}

	public ModelAndView getMovie(Long mcode) {
		log.info("getMovie()");
		mv = new ModelAndView();

		//영화 정보
		Movie mEntity = mRepo.findById(mcode).get();
		MovieDto movie = mapper.map(mEntity, MovieDto.class);
		mv.addObject("movie", movie);

		//태크 정보
		List<String> tList = tRepo.findByTmcode(mcode);

		//해시 태그용 링크 html로 변환
		String tag = makeHashtag(tList);
		mv.addObject("tag", tag);

		return mv;
	}

	private String makeHashtag(List<String> tList) {
		log.info("makeHashtag()");
		String tag = null;
		StringBuffer sb = new StringBuffer();

		for (String str : tList) {
			sb.append("<a href=''>#" + str + "</a>");
		}//<a href='검색용 url'>#제목</a>
		tag = sb.toString();
		return tag;
	}

	public String updateMovie(List<MultipartFile> files, MovieDto movie, HttpSession session, RedirectAttributes rttr) {
		log.info("updateMovie()");
		String view = null, msg = null;

		String upFile = files.get(0).getOriginalFilename();//파일 변경을 위한 정보
		String poster = movie.getMsysname();//원래 저장되어 있는 포스터 이미지의 파일명

		try {
			if (!upFile.equals("")) fileUpload(files, session, movie);//upFile이 비어있지 않을 때
			//기존 파일(포스터) 삭제
			if (poster != null && !upFile.equals(""))
				fileDelete(poster, session);//기존 파일이 있고, 새 파일이 들어왔을 때 파일 삭제

			//영화 정보 수정 처리
			Movie mEntity = mapper.map(movie, Movie.class);
			mRepo.save(mEntity);
			view = "redirect:detail?mcode=" + movie.getMcode();
			msg = "수정 성공";
		} catch (Exception e) {
			e.printStackTrace();
			view = "redirect:updateForm?mcode=" + movie.getMcode();
			msg = "수정 실패";
		}

		rttr.addFlashAttribute("msg", msg);
		return view;
	}

	private void fileDelete(String poster, HttpSession session) throws Exception {
		log.info("fileDelete()");
		String realPath = session.getServletContext().getRealPath("/");
		realPath += "upload/" + poster;
		File file = new File(realPath);
		if (file.exists()) file.delete();
	}

	public String deleteMovie(Long mcode, String msysname, HttpSession session, RedirectAttributes rttr) {
		log.info("deleteMovie()");
		String view = null, msg = null;

		try {
			//파일 삭제
			if (msysname != null) fileDelete(msysname, session);
			tRepo.deleteByTmcode(mcode);//해시 태그 삭제
			mRepo.deleteById(mcode);//영화 정보 삭제
			view = "redirect:/";
			msg = "삭제 성공";
		} catch (Exception e) {//예외 처리
			e.printStackTrace();
			view = "redirect:detail?mcode" + mcode;
			msg = "삭제 실패";
		}

		rttr.addFlashAttribute("msg", msg);
		return view;
	}
}