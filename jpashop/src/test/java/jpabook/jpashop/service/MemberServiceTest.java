package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class MemberServiceTest {
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Test
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("규하");
        //when
        Long savedId = memberService.join(member);
        //then
        Assertions.assertThat(member).isEqualTo(memberService.findMember(savedId));
    }

    @Test
    public void 중복_회원_예외() throws Exception{
        //given
        Member member = new Member();
        member.setName("규하");

        Member member2 = new Member();
        member2.setName("규하"); // 중복된 이름
        //when
        memberService.join(member);
        //then
        assertThrows(IllegalStateException.class, () ->{
            memberService.join(member2);  // 예외가 발생해야함
        });
    }
}