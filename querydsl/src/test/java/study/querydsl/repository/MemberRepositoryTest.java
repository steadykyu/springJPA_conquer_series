package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void basicTest() throws Exception{
        //given
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        //when //then
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> findMembers = memberRepository.findAll();
        assertThat(findMembers).containsExactly(member); // 회원 한개만

        List<Member> result = memberRepository.findByUsername(member.getUsername());
        assertThat(result).containsExactly(member);
    }
    @PersistenceContext
    EntityManager em;
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

        List<MemberTeamDto> result = memberRepository.search(condition); // MemberRepositoryCustom.search() 사용가능!!

        //then
        assertThat(result).extracting("username").containsExactly("member3", "member4");
        System.out.println(result.get(0).getTeamId());
    }

    @Test
    public void searchPageSimple() throws Exception{
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
            // 페이징을 위해 동적쿼리 조건이 없다고 가정
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest); // MemberRepositoryCustom.search() 사용가능!!

        //then
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchPageComplex() throws Exception{
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

        PageRequest pageRequest = PageRequest.of(0, 3); // PageSize 는 3이다.

        Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, pageRequest); // MemberRepositoryCustom.search() 사용가능!!

        //then
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }

    @Test
    public void searchPageCountQuery() throws Exception{
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
        condition.setUsername("member1"); // 한개만 조회되도록 설정
        PageRequest pageRequest = PageRequest.of(0, 3); // PageSize 는 3이다.

        Page<MemberTeamDto> result = memberRepository.searchPageCountQuery(condition, pageRequest); // MemberRepositoryCustom.search() 사용가능!!

        //then
        assertThat(result.getTotalElements()).isEqualTo(1); // 컨텐츠는 1개
        assertThat(result.getSize()).isEqualTo(3); // 페이지 크기 3 (페이지 에 들어올수 있는 컨텐츠의 크기)
        assertThat(result.getContent())
                .extracting("username")
                .containsExactly("member1");
    }

    @Test
    public void queryDslPredicateExecutor(){
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

        // Predicate를 매개변수로 가지는 메서드를 불러온다.
        QMember member = QMember.member;
        Iterable<Member> findMembers = memberRepository.findAll(
                member.age.between(10, 40).and(member.username.eq("member1"))
        );

        for (Member findMember : findMembers) {
            System.out.println(findMember);
        }
    }
}