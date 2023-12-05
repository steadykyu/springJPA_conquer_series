package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import study.querydsl.dto.MemberDto;

import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;
    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() throws Exception{
        //member1을 찾아라
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        // 쿼리 오류시 어플리케이션이 로딩된 후, 해당 쿼리가 동작하는 런타임시점에 오류가 발견된다.
    }

    @Test
    public void startQuerydsl() throws Exception{
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member2"))
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member2");
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))) // chain 식으로 작성
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), (member.age.eq(10))) // and 대신 쉼표 가능
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() throws Exception{
//        // List - 모든 멤버 객체
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 단 건
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();
        // 처음 한건 조회
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();
        System.out.println(fetchFirst); // Member(id=3, username=member1, age=10)

//        페이징에서 사용 - total 정보를 위해 count 쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        System.out.println(results.getTotal()); // 4
//        JPA 쿼리: id만 가지고 count 쿼리를 진행함.
        List<Member> content = results.getResults();
        System.out.println(content); // 모든 멤버 객체(각 1,2,3,4 Member객체)

//         count 쿼리로 변경
//             JPA 쿼리: select * -> count(member0_.member_id)
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() throws Exception{
        // 정렬용 예제 추가
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0); // 이름 비교시 오름 차순
        Member member6 = result.get(1); // 이름 비교시
        Member memberNull = result.get(2); // nullLast

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isEqualTo(null);
    }

    @Test
    public void paging() throws Exception{
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
        // JPQL 에는 안보이나 JPA 가 보내는 실제 쿼리에는 offset과 limit이 들어간 모습을 볼 수 있다.
    }

    @Test
    public void paging2() throws Exception{
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();


        assertThat(queryResults.getTotal()).isEqualTo(4);  // 총 컨텐츠의 수
        assertThat(queryResults.getLimit()).isEqualTo(2);  // 페이징 크기
        assertThat(queryResults.getOffset()).isEqualTo(1); // 시작점
        assertThat(queryResults.getResults().size()).isEqualTo(2); // 페이징 된 컨텐츠의 수

    }

    @Test
    public void aggregation() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        // Tuple - querydsl이 제공

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     * 팀의 이름과 각 팀 회원 들의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
//                .having(team.name.eq("teamA")) // Having 절도 가능
                .fetch();
        // 팀을 기준으로 groupBy 로 인해 두개의 Tuple이 생성된다.
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10+20)/2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30+40)/2
    
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception{
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

    }

    /**
     * 세타 조인
     * - 연관관계가 없는 필드로 하는 조인
     * - 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception{
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

//        List<Tuple> result = queryFactory
//                .select(member, team)
//                .from(member, team)
//                .where(member.username.eq(team.name))
//                .fetch();

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

//        for (Tuple tuple: result){
//            System.out.println(tuple);
//        }
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

        // 모든 member와 team을 가져와서 조인한 후 where문을 하는 경우
        // DB 마다 세타 조인 쿼리 초적화가 다르다.
    }

    /**
     * 예 ) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join Team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() throws Exception{
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
//                .join(member.team, team)
//                .where(team.name.eq("teamA"))
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }

        // member는 다 가져왔고, team은 teamA인 부분만 조회하여 조인한다.
    }

    /**
     * 2. 연관관계가 없는 엔티티의 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) //주의: 엔티티가 하나만 들어간다. -> Join에 FK 값이 없다. 이름 On절의 이름을 기준으로한다.
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit // emf 전용 DI
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo() throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        // member 와 Team은 Lazy 관계이다.(지연로딩) 그러므로 Team 관련 쿼리는 출력되지않는다.

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse(); // 아직까지는 영속화 되지 않은 빈 값이 들어있다.
        System.out.println(findMember.getTeam()); // team 조회 쿼리가 새로 출력된다. ---> 1+N 문제 발생 주의
    }

    @Test
    public void fetchJoin() throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isTrue(); // 영속화된 Team이 들어있어 True이다.
        System.out.println(findMember.getTeam()); // -> 이전에 한번에 Team 엔티티들을 영속화하여 추가 쿼리가 발생 X
    }

    /**
     * 나이가 가장 많은 회원
     */
    @Test
    public void subQuery() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        // 서브 쿼리의 결과로 max인 40이 나온다.
                        JPAExpressions.select(memberSub.age.max()) // max()
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(40);

    }
    // JPAEXpression은 Static Import로 편히 사용 가능
    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        // 평균인 25 이상
                        JPAExpressions.select(memberSub.age.avg()) // avg()
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(30, 40);

    }

    /**
     *
     */
    @Test
    public void subQueryIn() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in( // in 절 활용
                        // 10살 초과인 20, 30 ,40 의 회원이 row로 온다.
                        JPAExpressions.select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(20, 30, 40);

    }

    /**
     * select 절에 in절
     */
    @Test
    public void selectSubQuery() throws Exception{
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void basicCase() throws Exception{
        //given
        List<String> result = queryFactory
                .select(member.age
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = "+ s);
        }

    }

    @Test
    public void complexCase() throws Exception{
        //given
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0살~20살")
                        .when(member.age.between(21, 30)).then("21살~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        //when
        for (String s : result){
            System.out.println("s = "+ s);
        }
    }
    // 가급적 DB에서 이런 문제를 처리하지 않는게 좋다..
    // raw데이터를 필터링, 그룹핑, 최소한의 계산정도만 하는게 좋다.

    @Test
    public void orderByCase() throws Exception{
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void constant() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : result){
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() throws Exception{
        // {username}_{age}
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) // 숫자, enum -> 문자로 변환
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s: result) {
            System.out.println("s = "+ s);
        }
    }

    @Test
    public void simpleProjection() throws Exception{
        //given
        List<String> result = queryFactory
                .select(member.username) // 엔티티 하나도 SimpleProjection으로 본다.-> 타입 가져와준다.
                .from(member)
                .fetch();
        //when
        for (String s: result) {
            System.out.println("s = "+ s);
        }
    }

    @Test
    public void tupleProjection() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(member.username, member.age) // 지정 대상이 두개 이상
                .from(member)
                .fetch();
        //when
        for(Tuple tuple: result){
            String username = tuple.get(member.username);
            int age = tuple.get(member.age);
            System.out.println("username = "+ username);
            System.out.println("age = " + age);
        }
        // Tuple은 Repo 계층에서만 사용하자. 서비스나 API계층까지 가는것은 좋지 않다.
        // 그래야만 만약 QueryDsl 대신 다른 Repo 기술을 사용하더라도 갈아끼우기만 하면 된다.

    }

    @Test
    public void findDtoByJPQL() throws Exception{
        //given
        // JPQL은 new 연산과 함께 dto의 경로를 다적어주어야한다.
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();
        //when
        for (MemberDto dto: result) {
            System.out.println("memberDto = "+ dto);
        }
    }

    @Test
    public void findDtoBySetter() throws Exception{
        //given
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, // bean -> 프로퍼티 방식으로 접근해서 값을 넣는다.
                        member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoBySetter2() throws Exception{
        //given
        List<UserDto> result = queryFactory
                .select(Projections.bean(UserDto.class, // bean -> 프로퍼티 방식으로 접근해서 값을 넣는다.
                        member.username.as("name"), member.age))
                .from(member)
                .fetch();
        //when
        for (UserDto memberDto : result){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByField() throws Exception{
        //given
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, // fields -> 필드값을 그대로 가져와 주입한다.
                        member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() throws Exception{
        //given
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, // constructor -> 생성자방식으로 값을 주입
                        member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findUserDtoByField() throws Exception{
        QMember memberSub = new QMember("memberSub");
        // DTO와 엔티티의 프로퍼티 명이 다른 경우는 어떻게 될까?
        //given
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), // username을 name의 별칭으로 수정 -> DTO 와 매칭 가능
                        Expressions.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age") // 이름이 없는 subQuery에 별칭 주기 -> 이후 DTO와 매칭
                        ))
                .from(member)
                .fetch();
        //when
        for (UserDto UserDto : result){
            System.out.println(UserDto);
        }
    }

    @Test
    public void findUserDtoByConstructor() throws Exception{
        //given
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (UserDto userDto : result){
            System.out.println(userDto);
        }
    }

//    @Test
//    public void findDtoByQueryProjection() throws Exception{
//        List<MemberDto> result = queryFactory
//                .select(new QMemberDto(member.username, member.age)) // 생성자 방식으로 써주면 된다.
//                .from(member)
//                .fetch();
//
//        for (MemberDto memberDto : result) {
//            System.out.println("memberDto = "+ memberDto);
//        }
//    }

    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception{
        //given
        String usernameParam = "member1";
        Integer ageParam = 10;

        // searchMember1
        // 둘다 null -> where에 아무조건 X
        // ageParam = null -> where username = xxx
        // username = null -> where age = xx
        // 둘다 값 존재 -> where username = xxx and age = xx
        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);     
        //then

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
//        BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond)); 필수 조건일 경우
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() throws Exception{
        String usernameParam = "member1"; // null
        Integer ageParam = 10; // null

        // searchMember1
        // 둘다 null -> where에 아무조건 X
        // ageParam = null -> where username = xxx
        // username = null -> where age = xx
        // 둘다 값 존재 -> where username = xxx and age = xx
        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameCond), ageEq(ageCond)) // where에 null이 들어오면 그냥 무시된다.
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression usernameEq(String usernameCond) {
        if(usernameCond == null){
            return null;
        }
        return member.username.eq(usernameCond);
    }

    private BooleanBuilder allEq(String usernameCond, Integer ageCond){
        BooleanBuilder builder = new BooleanBuilder();
        return builder.and(usernameEq(usernameCond)).and(ageEq(ageCond));
        // BooleanExpression을 이용하면 조건들을 합쳐서 해줄 수 있다는 장점이 있다.
        // 다른 쿼리에서도 재활용할 수 있음.
    }

    @Test
    @Commit
    public void bulkUpdate() throws Exception{

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute(); // -> 영향을 받은 row를 count를 출력
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        for (Member member : result) {
            System.out.println(member);
        }
    }

    @Test
    public void bulkAdd() throws Exception{
        //given
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1)) // add(-1) 가능
//                .set(member.age, member.age.multiply(2))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        for (Member member : result) {
            System.out.println(member);
        }
    }

    @Test
    public void bulkDelete() throws Exception{
        //given
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
        //then
        assertThat(count).isEqualTo(3); // 20 30 40

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        for (Member member : result) {
            System.out.println(member);
        } // member1만 남음
    }

    @Test
    public void sqlFunction() throws Exception{
        //given
        List<String> result = queryFactory
                .select(Expressions.stringTemplate("function('replace',{0},{1},{2})",
                        member.username, "member", "M")) // "member" -> "M"
                .from(member)
                .fetch();
        // H2Dialect.class - registerFunction 참고
        //when
        for (String s : result) {
            System.out.println("s = "+ s);
        }
        //then
    }

    @Test
    public void sqlFunction2() throws Exception{
        //given
            // 대문자로 바꾸기
        List<String> result = queryFactory
//                .select(Expressions.stringTemplate("function('ucase',{0})", member.username))
                .select(member.username.upper())
                .from(member)
                .fetch();
        //when
        for (String s : result) {
            System.out.println("s = "+ s);
        }

        //ansi 표준 함수들은 querydsl이 상당부분 내장하고 있다. 따라서 다음과 같이 처리해도 결과는 같다.
    }
}
