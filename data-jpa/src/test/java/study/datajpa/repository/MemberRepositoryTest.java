package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    // Spring Data JPA 가 제공하는 인터페이스의 구현체를 injection 받자.
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(false) // transaction의 롤백을 막는다.
    public void testMember() throws Exception{
        //given
        Member member = new Member("memberA");
        //when
        Member saveMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(saveMember.getId()).get();// Optional을 return 해온다.

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장
    }

    @Test
    public void 프록시확인() throws Exception{
        System.out.println(memberRepository);
        System.out.println(memberRepository.getClass());
    }

    @Test
    @Rollback(false) // transaction의 롤백을 막는다.
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> findMembers = memberRepository.findAll();
        assertThat(findMembers.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

        // 수정 검증
        // 변경 감지에 의해 MemberJpaRepositoryTest 트랜잭션이 끝날때 update 쿼리가 날라간다.
        // 그러므로 여기서 테스트 할 수는 없지만, H2 DB를 보면 값이 수정된 것을 볼 수 있다.
        Member originMember = new Member("origin");
        memberRepository.save(originMember);
        originMember.setUsername("new");
    }

    @Test
    public void findByUsernameAndAgeGreater() throws Exception{
        //given
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("AAA", 20, null);
        //when
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void testNamedQuery(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("BBB", 20, null);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testQuery(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("BBB", 20, null);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void findUsernameList(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("BBB", 20, null);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s:  usernameList) {
            System.out.println("s = "+ s);
        }
        assertThat(usernameList.get(0)).isEqualTo("AAA");
        assertThat(usernameList.get(1)).isEqualTo("BBB");
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10, team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for(MemberDto dto : memberDto){
            System.out.println("dto = "+ dto);
        }
    }

    @Test
    public void findByNames(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
        Member m2 = new Member("BBB", 20, null);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member m: result) {
            System.out.println("member = "+ m);
        }
    }

    @Test
    public void returnType(){
        Member m1 = new Member("AAA", 10, null); // null 은 Team
//        Member m2 = new Member("AAA", 20, null); // AAA인 경우 IncorrectResultSizeDataAccessException
        memberRepository.save(m1);
//        memberRepository.save(m2);

        List<Member> members = memberRepository.findMembersByUsername("AAA");
        List<Member> wrongMembers = memberRepository.findMembersByUsername("kkdfaskdfdskfk");

        for (Member m: members) {
            System.out.println("member = "+ m); // member = Member(id=1, username=AAA, age=10)
        }
        System.out.println("wrongMembers = " + wrongMembers.size()); // 0 -> NULL이 아닌 empty 컬렉션임에 주의해야함

        Member member = memberRepository.findMemberByUsername("AAA");
        Member wrongMember = memberRepository.findMemberByUsername("kkdfaskdfdskfk");
        System.out.println(member); // Member(id=1, username=AAA, age=10)
        System.out.println(wrongMember); // null
        // NoResultException 이 떠야하지만, null로 나오도록 try catch문 되어 있음.
//
        Optional<Member> option = memberRepository.findOptionByUsername("AAA");
        Optional<Member> wrongOption = memberRepository.findOptionByUsername("kkdfaskdfdskfk");
        System.out.println(option.get()); // Member(id=1, username=AAA, age=10)
        System.out.println(wrongOption); // Optional.empty
    }

    @Test
    public void paging(){
        //given
        memberRepository.save(new Member("member1",10, null));
        memberRepository.save(new Member("member2",10, null));
        memberRepository.save(new Member("member3",10, null));
        memberRepository.save(new Member("member4",10, null));
        memberRepository.save(new Member("member5",10, null));
        // 페이지 정보
            // PageRequest -> pageable의 구현체
            // 첫번째 매개변수(page) 0인 경우, 실제 query에 offset 문법이 생략된다.
            // 첫번째 매개변수(page) 0인 경우, offset=3 이 포함된다.
                //  0번 페이지의 컨텐츠 3개로 인해 시작점이 3이 된다
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3,
                Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // API 반환을 위한 Page DTO로 변환하기
            // Page는 API 그대로 반환 가능함.
            // 단 Entity를 그대로 반환하면 안됀다. 그러므로 DTO로 변환해서 반환하자.
        Page<MemberDto> toMap = page.map(member ->
            new MemberDto(member.getId(), member.getUsername(), null));

        //then
            // 페이지 관련 기능들을 메소드를 통해 가져올 수 있다.
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();//

        for (Member member: content) {
            System.out.println("member = "+ member);
        }
        System.out.println(totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지 number
        assertThat(page.getTotalPages()).isEqualTo(2); // 0 page - 3개 , 1page - 2개
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지인가?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }
    //List<Member> list2 = memberRepository.findByAge(age, pageRequest);
    // List의 경우, Page객체의 부가정보가 없는 그저 페이징된(offset~limit) 쿼리 결과를 받게 된다.

    @Test
    public void paging2() {
        //given
        memberRepository.save(new Member("member1", 10, null));
        memberRepository.save(new Member("member2", 10, null));
        memberRepository.save(new Member("member3", 10, null));
        memberRepository.save(new Member("member4", 10, null));
        memberRepository.save(new Member("member5", 10, null));
        // 페이지 정보
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3,
                Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.find2ByAge(age, pageRequest);
    }

    @Test
    public void paging_slice(){

        //given
        memberRepository.save(new Member("member1",10, null));
        memberRepository.save(new Member("member2",10, null));
        memberRepository.save(new Member("member3",10, null));
        memberRepository.save(new Member("member4",10, null));
        memberRepository.save(new Member("member5",10, null));
        // 페이지 정보
        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3,
                Sort.by(Sort.Direction.DESC, "username"));

        // when
        // page 0페이지의, 3개size를 가져오라고 하지만, Slice의 경우 한개를 더 가져온다.
        // 쿼리확인시 limit4 인걸확인 가능
        Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

        //then
        // 페이지 관련 기능들을 메소드를 통해 가져올 수 있다.
        List<Member> content = slice.getContent();

        for (Member member: content) {
            System.out.println("member = "+ member);
        }

        assertThat(content.size()).isEqualTo(3);
        assertThat(slice.getNumber()).isEqualTo(0); // 현재 페이지 number
        assertThat(slice.isFirst()).isTrue(); // 첫번째 페이지인가?
        assertThat(slice.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() throws Exception{
        //given
        memberRepository.save(new Member("member1", 10,null));
        memberRepository.save(new Member("member2", 19,null));
        memberRepository.save(new Member("member3", 20,null));
        memberRepository.save(new Member("member4", 21,null));
        memberRepository.save(new Member("member5", 40,null));
        //when
        int resultCount = memberRepository.bulkAgePlus(20); // 벌크 연산후, 엔티티를 사용하는 트랜잭션일 경우 문제발생

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0); // 40 -> bulk연산이 되지 않았다.
        System.out.println("member5 = " + member5);
        // bulk 연산은 영속성 컨텍스트를 거치지 않고 쿼리를 날린다.

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1", 10,teamA));
        memberRepository.save(new Member("member2", 19,teamB));
        memberRepository.save(new Member("member1", 30,teamB));

        em.flush();
        em.clear();
        //when
//        List<Member> members = memberRepository.findAll();
//        List<Member> members = memberRepository.findFetchJoin();
//                List<Member> members = memberRepository.findMemberEntityGraph();
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");
        // fetch join -> join 에 더해 연관관계의 모든 필드들을 다 가져와준다.
        for(Member member : members){
            System.out.println("member = " +member.getUsername() +", member.age = "+ member.getAge());
            System.out.println("member.team = " +member.getTeam().getClass());
            System.out.println("member.team.name = " +member.getTeam().getName());
        }
        //then
    }

    @Test
    public void queryHint() {
        Member member1 = new Member("member1", 10, null);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findMemberByUsername("member1");
//        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush(); // // JPA는 원본과 스냅샷용 두개를 만들고 있다. 이를 이용해 변경감지를 통해 update 쿼리를 날린다.
        
    }
    
    @Test
    public void lock() throws Exception{
        //given
        Member member1 = new Member("member1", 10, null);
        memberRepository.save(member1);
        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findLockByUsername("member1");
        //then
    }
    // 책의 트랜잭션과 Lock 참고

    @Test
    public void callCustom() throws Exception{
        //given
        List<Member> result = memberRepository.findMemberCustom(); // spring에서 동작하도록 만들어줌

    }

    
    @Test
    public void specBasic() throws Exception{
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

        //when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);
        //then
        assertThat(result.size()).isEqualTo(1);
    }
    // 스프링 데이터 JPA가 JPQL 을 알아서 만들어준다.

    @Test
    public void queryByExample() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

        //when
        // Probe
            // 엔티티 객체로 조건을 만든다.
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team); // 연관관계 세팅

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age"); // 기본값이 들어갈수 있으므로 무시한다.

        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("m1"); // 결국 결과가 하나이므로 이름 확인해보자.
    }

    @Test
    public void projections() throws Exception{
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();

        //when
//        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");
        List<NestedClosedProjections> result = memberRepository.findProjectionsDtoByUsername("m1", NestedClosedProjections.class);


        for(NestedClosedProjections usernameOnly : result){
            System.out.println("usernameOnly = " + usernameOnly);

            String username = usernameOnly.getUsername();
            System.out.println("username = " + username);
            String teamName = usernameOnly.getTeam().getName();
            System.out.println("teamName = "+ teamName);
        }
        //then
    }
    @Test
    public void nativeQuery() throws Exception{
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();
        //when

//        Member result = memberRepository.findByNativeQuery("m1"); // 직접 그대로 쿼리가 날아감
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for(MemberProjection memberProjection : content){
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
        }
        //then

    }
}