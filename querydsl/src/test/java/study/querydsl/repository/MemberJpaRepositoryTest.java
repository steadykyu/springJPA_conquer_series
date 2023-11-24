package study.querydsl.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() throws Exception{
        //given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        //when //then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> findMembers = memberJpaRepository.findAll();
        assertThat(findMembers).containsExactly(member); // 회원 한개만

        List<Member> result = memberJpaRepository.findByUsername(member.getUsername());
        assertThat(result).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest() throws Exception{
        //given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);
        //when //then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> findMembers = memberJpaRepository.findAll_Querydsl();
        assertThat(findMembers).containsExactly(member); // 회원 한개만

        List<Member> result = memberJpaRepository.findByUsername_Querydsl(member.getUsername());
        assertThat(result).containsExactly(member);
    }

    @Test
    public void searchTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10 ,teamA);
        Member member2 = new Member("member2", 20 ,teamA);

        Member member3 = new Member("member3", 30 ,teamB);
        Member member4 = new Member("member4", 40 ,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //when
        MemberSearchCond condition = new MemberSearchCond();
        condition.setAgeGoe(25);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        // Member이름 조건은 생략

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        assertThat(result).extracting("username").containsExactly( "member3", "member4");
    }


    // isEmpty()
    @Test
    public void searchTest_Where() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10 ,teamA);
        Member member2 = new Member("member2", 20 ,teamA);

        Member member3 = new Member("member3", 30 ,teamB);
        Member member4 = new Member("member4", 40 ,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //when
        MemberSearchCond condition = new MemberSearchCond();
        condition.setAgeGoe(25);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        // Member이름 조건은 생략

        List<MemberTeamDto> result = memberJpaRepository.searchByWhere(condition);

        //then
        assertThat(result).extracting("username").containsExactly("member3", "member4");

    }
}