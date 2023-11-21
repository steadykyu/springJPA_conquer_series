package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
@SpringBootTest
@Transactional // import 주의 -> 자동 롤백 JPA는 트랜잭션 하에서 동작하므로 필수
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;
    @Autowired
    EntityManager em;

    @Test // import 주의!
    @Rollback(false) // transaction의 롤백을 막는다.
    public void testMember() throws Exception{
        //given
        Member member = new Member("memberA");
        //when
        Member saveMember = memberJpaRepository.save(member);
        //then
        Member findMember = memberJpaRepository.find(saveMember.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장
    }

    @Test
    @Rollback(false) // transaction의 롤백을 막는다.
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> findMembers = memberJpaRepository.findAll();
        assertThat(findMembers.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);

        // 수정 검증
            // 변경 감지에 의해 MemberJpaRepositoryTest 트랜잭션이 끝날때 update 쿼리가 날라간다.
            // 그러므로 여기서 테스트 할 수는 없지만, H2 DB를 보면 값이 수정된 것을 볼 수 있다.
        Member originMember = new Member("origin");
        memberJpaRepository.save(originMember);
        originMember.setUsername("new");
    }

    @Test
    public void findByUsernameAndAgeGreater() throws Exception{
        //given
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("AAA", 20, null);
        //when
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void testNamedQuery(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("BBB", 20, null);
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        List<Member> result = memberJpaRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void paging(){
        //given
        memberJpaRepository.save(new Member("member1",10, null));
        memberJpaRepository.save(new Member("member2",10, null));
        memberJpaRepository.save(new Member("member3",10, null));
        memberJpaRepository.save(new Member("member4",10, null));
        memberJpaRepository.save(new Member("member5",10, null));
            // 페이지 정보
        int age = 10;
        int offset = 1; // 0번 row가 있다고 가정시, 1번 row 부터
        int limit = 3; // 3개 (1,2,3 row) 에 관한 정보들

        // when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        // 페이지 계산 공식 적용 방식은 따로 검색
            // totalPage = totalCount / size
            // ex) page 1 -> offset: 1, limit:10
            // ex) page 2 -> offset:10, limit:20

        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
        System.out.println(members.get(0)); // Member(id=4, username=member4, age=10)
                                             // 내림차순에의해 5 대신 4
    }
    
    @Test
    public void bulkUpdate() throws Exception{
        //given
        memberJpaRepository.save(new Member("member1", 10,null));
        memberJpaRepository.save(new Member("member2", 19,null));
        memberJpaRepository.save(new Member("member3", 20,null));
        memberJpaRepository.save(new Member("member4", 21,null));
        memberJpaRepository.save(new Member("member5", 40,null));
        //when
        int resultCount = memberJpaRepository.bulkAgePlus(20);
        em.flush();
        em.clear(); // 영속성 컨텍스트의 엔티티들을  완전히 비워버린다.

        List<Member> result = memberJpaRepository.findByUsername("member5");
        Member member5 = result.get(0); // 40 -> bulk연산이 되지 않았다.
        System.out.println("member5 = " + member5);
        // bulk 연산은 영속성 컨텍스트를 거치지 않고 쿼리를 날린다.
        //then
        assertThat(resultCount).isEqualTo(3);
    }
}