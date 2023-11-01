package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {
    @Id @GeneratedValue
    private Long deliveryId;
    @OneToOne(mappedBy = "delivery",fetch = FetchType.LAZY) // 양방향 연관관계
    private Order order;

    @Embedded
    private Address address;
    @Enumerated(value = EnumType.STRING)
    private DeliveryStatus deliveryStatus; //ENUM [READY(준비), COMP(배송)]
}
