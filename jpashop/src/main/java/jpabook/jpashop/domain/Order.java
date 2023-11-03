package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id @GeneratedValue
    private Long orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //Todo : why cascade?
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) //Todo : why cascade? - 안쓰니 에러뜸
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; // 배송 정보

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(value = EnumType.STRING) // String Option으로 해주기
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    /**
     * ===연관관계 편의 메서드들===
     * 올바른 양방향 관계 유지를 위한 연관관계 편의 메서드이다.
     */
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /**
     * 주문 생성 메서드
     * - 논리적으로 주문이란 개념은 회원이 상품을 골라 배송지를 입력함으로써 생성돤다,
     * - 그러므로 상품, 회원, 배송이 이미존재하고 있으며 주문생성에 필요하다.
     *      - 주문은 Item이 아닌 OrderItem과 연관관계를 가지므로, OrderItem이 온다. (필드)
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
//            order.getOrderItems().add(orderItem);
        }
        // 초기값 초기화
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    /**
     * 비즈니스 로직
     * - 주문 취소
     *  - 이미 배송 완료된 상품은 취소가 불가능하다.
     */
    public void cancel(){
        if(delivery.getDeliveryStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL); // 주문 상태 ( ORDER -> CANCEL)

        for(OrderItem orderItem : orderItems){
            orderItem.cancel(); // Order와 연관있는 OrderItem의 상태 변화 -> 연결된 Item의 재고수 증가
        }
    }
    /**
     * 조회 로직
     * 전체 주문 가격 조회
     */
    public int getTotalPrice(){
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice(); // orderItem은 각 주문속에 존재하는 상품들의 가격과 개수를 나타낸다.
            // ex) a 주문 빼빼로 1500 3
            // ex) a 주문 레쓰비 1000 5
            // ex) b 주문 빼빼로 1500 2
        }
        return totalPrice;
    }
}
