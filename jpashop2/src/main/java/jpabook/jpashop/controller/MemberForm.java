package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

// 화면을 위한 Form 데이터(엔티티를 직접쓰지 않기 위해 사용)
@Getter @Setter
public class MemberForm {
    @NotEmpty
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
