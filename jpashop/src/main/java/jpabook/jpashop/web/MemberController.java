package jpabook.jpashop.web;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원 등록 폼 화면 응답 기능
     */
    @GetMapping("/members/new")
    public String createForm(@ModelAttribute("memberForm") MemberForm memberForm){
        return "members/createMemberForm";
    }

    /**
     * 회원 데이터를 DB에 저장하는 기능
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result){ // 유효성 검사 적용
        if(result.hasErrors()){
            return "members/createMemberForm";
        }
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        
        memberService.join(member); // 엔티티를 직접 넘김 (신규이므로 비영속 엔티티)

        return "redirect:/"; // 홈 화면으로 리다이렉트
    }

    /**
     * 회원 목록 화면 응답 기능
     */
    @GetMapping("/members")
    public String membersList(Model model){
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members",members);
        return "members/memberList";
    }
}
