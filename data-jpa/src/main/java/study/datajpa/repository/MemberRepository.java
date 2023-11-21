package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

// SpringDataJPA
public interface MemberRepository extends JpaRepository<Member, Long> , MemberRepositoryCustom, JpaSpecificationExecutor{ //
    // 엔티티의 프로퍼티 명에 맞게 작성
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username); // setParameter 대신 @Param
    // NamedQuery가 있으면 먼저 처리하고, 없으면 메소드 명명규칙에 따라 구현체를 가져온다.

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age); // setParameter 대신 @Param

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto(); // new operation, 생성자 만들듯이 해주면 된다.

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findMembersByUsername(String username); // 컬렉션 반환
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionByUsername(String username); // Optional

    Page<Member> findByAge(int age, Pageable pageable); // import 주의
    // 카운트 쿼리분리를 통한 성능 최적화 ( 카운트에서는 join을 안함)
    // 애초에 카운트 쿼리는 outer join이 필요없음
    @Query(value = "select m from Member m left join m.team t"
            , countQuery = "select count(m.username) from Member m")
    Page<Member> find2ByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true) // JPA의 excuteUpdate 작업을 해당 애노테이션이 처리해준다.
    @Query("update Member m set m.age = m.age +1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    // JPQL 에 엔티티 넣기
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

//    @EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);
    <T> List<T> findProjectionsDtoByUsername(@Param("username") String username, Class<T> type);

    // 해당 내용 그대로 DB에 쿼리를 날린다.
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    // Projection 에 맞게 매칭
    @Query(value = "select m.member_id as id, m.username, t.name as teamName " +
    "from member m left join team t",
    countQuery = "select count(*) from member",
    nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
