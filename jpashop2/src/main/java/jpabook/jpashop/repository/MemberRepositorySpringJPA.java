package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 엔티티와 PK를 넣는다.
public interface MemberRepositorySpringJPA extends JpaRepository<Member, Long> {
    List<Member> findByName(String name);
    // 끝 -> 메서드 명명규칙을 통해 알맞은 쿼리를 생성해준다.
    // select m from Member m where m.name = :name
}
