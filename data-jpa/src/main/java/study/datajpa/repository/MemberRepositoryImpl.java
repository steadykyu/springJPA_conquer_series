package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
@RequiredArgsConstructor // 구현체 이름을 인터페이스명+Impl 로 맞춰주어야 Spring Date JPA가 맞추어서 알아서 처리해줌
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    // DB 커넥션
    // Mybatis 등등.. Injecttion 받아모면 된다.
    // QueryDsl
    // JPA 직접 사용 or JDBC 템플릿을 위한 EntityManager
        // RA로 인해 Injection

    // -> 주로 스프링 데이터 JPA로 처리할 수 없는 복잡한 동적쿼리를 짤떄 선택함
    private final EntityManager em;


    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
