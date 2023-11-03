package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    //@PersistenceContext
    private final EntityManager em; // 생성자 주입으로 Aurowired 가능

    public void save(Member member){
        em.persist(member);
    }

    public Member findOne(Long memberId){
        Member findMember = em.find(Member.class, memberId);
        return findMember;
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /**
     * findByName()
     * 이름 으로 회원 찾기
     */
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name= :name")
                .setParameter("name", name) // 쿼리 파라미터 :name 에 객체를 넣어준다.
                .getResultList();

    }
}
