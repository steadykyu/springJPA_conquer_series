package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // import 주의
@RequiredArgsConstructor // Autowired - 생성자 주입
public class MemberService {
    private final MemberRepository memberRepository; // Autowired - 생성자 주입
    /**
     * 회원 가입
     * - 회원 수를 변경하므로, readOnly = false 인 @Transactional을 메서드에 설치!
     */
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getMemberId();
    }

    /**
     * 중복 회원을 처리
     * - 여기서는 이름을 기준으로 중복처리한다.
     */
    public void validateDuplicateMember(Member member){
        List<Member> findMembers =
                memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findAllMembers(){
        return memberRepository.findAll();
    }

    /**
     * 회원 개별 조회
     */
    public Member findMember(Long memberId){
        return memberRepository.findOne(memberId);
    }
}
