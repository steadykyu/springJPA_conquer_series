package study.querydsl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile(value = "local")
@Component
@RequiredArgsConstructor
public class initMember {
    private final InitMemberService initMemberService;

    @PostConstruct // DI 되자마자 초기화 콜백으로 해당 메서드가 자동 실행
    public void init(){
        initMemberService.init();
    }
    @Component
    static class InitMemberService {
        @PersistenceContext
        EntityManager em;
        
        @Transactional // 엔티티가 실행될 수 있도록 트랙잭션 필요.
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);
            // Member 생성
            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }
    }
}
