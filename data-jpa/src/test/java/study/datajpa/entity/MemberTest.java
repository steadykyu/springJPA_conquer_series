package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;
import study.datajpa.repository.TeamRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    // 엔티티 연관관계 테스트
    @Test
    public void testEntity(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamA");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush(); // 영속성 컨텍스트에 내용대로 강제로 query를 날린다.
        em.clear();

        // 확인
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for(Member member : members){
            System.out.println("member =" + member); //By ToString Annotation
            System.out.println("-> member.team = "+ member.getTeam());
        }

    }
    @Test
    public void JpaEventBaseEntity() throws Exception{
        //given
        Member member = new Member("member1");
        memberRepository.save(member); // @PrePersist

        Thread.sleep(100);
        member.setUsername("member2");

        em.flush();
        em.clear();
        //when
        Member findMember = memberRepository.findById(member.getId()).get();
        //then
//        System.out.println("findMember.createdDate = " + findMember.getCreatedDate());
////        System.out.println("findMember.updatedDate = " + findMember.getUpdatedDate());
//        System.out.println("findMember.updatedDate = " + findMember.getLastModifiedDate());
//        System.out.println("findMember.createdBy = "+ findMember.getCreatedBy());
//        System.out.println("findMember.updatedBy = "+ findMember.getLastModifiedBy());
    }

    @Test
    public void JpaEventBaseEntity2() throws Exception{
        //given
        Team team = new Team("teamA");
        teamRepository.save(team); // @PrePersist

        Thread.sleep(100);
        team.setName("team2");

        em.flush();
        em.clear();
        //when
        Team findTeam = teamRepository.findById(team.getId()).get();
        //then
        System.out.println("findMember.createdDate = " + findTeam.getCreatedDate());
//        System.out.println("findMember.updatedDate = " + findMember.getUpdatedDate());
        System.out.println("findMember.updatedDate = " + findTeam.getLastModifiedDate());
    }
}