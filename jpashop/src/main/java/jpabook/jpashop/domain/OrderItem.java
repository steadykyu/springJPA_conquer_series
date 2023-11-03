package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class OrderItem {
    @Id @GeneratedValue
    private Long orderItemId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 주문 상품

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    /**
     * 주문 상품 생성
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    /**
     * 비즈니스 로직
     * - 주문 취소 시, OrderItem에서도 주문 취소에 알맞은 로직을 수행해야한다.
     */
    public void cancel(){
        this.getItem().addStock(count);
    }

    /**
     * 조회 로직
     * - 주문 상품 전체 가격 조회( A 주문 , B 상품 의 전체 가격)
     * - 물론 동일 A 주문 , B 상품 의 전체 가격이 다른 row에 존재 가능
     */
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
