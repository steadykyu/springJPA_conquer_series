package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController // Rest API return 형식
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    // 샘플용 회원 만들기
    @PostConstruct
    public void init(){
        for(int i=0; i < 100; i++){
            memberRepository.save(new Member("user" + i, i, null));
        }
    }

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member){
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<Member> list(@PageableDefault(size=5) Pageable pageable){
        // 서비스 or Repository 계층에서 만든 Page 정보
        Page<Member> page = memberRepository.findAll(pageable);
        // DTO로 변환하기
        page.map(MemberDto::new);
        // page.map(member -> new MemberDto(member.getId, member.getUserName, null));
        // page.map(member -> new MemberDto(member));
        return page;
    }

    @GetMapping("/members/test")
    public Page<Member> pagableTest(@PageableDefault(size = 3) Pageable pageable){
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }

}
