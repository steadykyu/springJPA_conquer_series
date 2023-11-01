package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter // Value Type은 처음 생성한 후, 값 변경이 불가능 하도록 만들어야한다!
public class Address {
    private String city;
    private String street;
    private String zipcode;

    protected Address(){ // JPA SPEC 을 보면 protected의 기본생성자를 허용하므로, protected로 제약한다.

    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
