package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberTestRepository;


@RestController
@RequiredArgsConstructor
public class MemberTestController {

    private final MemberTestRepository memberTestRepository;

    @GetMapping("/test/members")
    public Page<MemberTeamDto> searchMemberPage(MemberSearchCond condition, Pageable pageable){
        Page<MemberTeamDto> findDtos = memberTestRepository.applyPagination3(condition, pageable);
        return findDtos;
    }
}
