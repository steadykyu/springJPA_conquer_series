package jpabook.jpashop.domain.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;

import jpabook.jpashop.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;
@ExtendWith(SpringExtension.class) // 단위 테스트간에 공통적으로 사용할 기능을 구현하여 @ExtendWith를 통하여 적용할 수 있는 기능을 제공합니다.
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
//    @Rollback(value = false) // insert문이 나감
    public void 회원가입() throws Exception{
        //given
        Member member = new Member();
        member.setName("kim2");

        //when
        Long savedId = memberService.join(member);

        //then
//        em.flush(); // -> 이 방식으로 DB에 쿼리를 날릴 수도 있음.
        assertEquals(member, memberRepository.findOne(savedId));
    }
    @Test
    public void duplicate_member_exception() throws Exception{
        //given : 예외가 발생하도록 같음 이름
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");
        //when
        memberService.join(member1);
//        memberService.join(member2);
//        try {
//            memberService.join(member2);
//        } catch (IllegalStateException e){
//            return;
//        }
        //then : 위쪽에서 예외가 발생해야만 한다고 알리기위해 사용되는 메서드 (무조건 테스트를 불통과 시킴)
        // junit5 기준
        assertThrows(IllegalStateException.class, () ->
                memberService.join(member2)
        );
    }

}