package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 주문생성() throws Exception{
        //given
        Member member = createSampleMember();
        Item book = createBook("JPABook", 10000, 10); // 책의 가격과 재고
        int orderCount = 4;

        //when
        Long orderId = orderService.makeOrder(member.getMemberId(), book.getItemId(), orderCount);// 회원id, 상품 id, 주문수량
        //then
        Order findOrder = orderRepository.findOne(orderId);
        Assertions.assertEquals(OrderStatus.ORDER, findOrder.getStatus(), "상품 주문시 상태는 Order");
        Assertions.assertEquals(1, findOrder.getOrderItems().size(), "상품 종류의 수가 정확해야한다.");
        Assertions.assertEquals(10000*4,findOrder.getTotalPrice(), "주문 가격은 가격 * 수량이다.");
        Assertions.assertEquals(6, book.getStockQuantity(), "주문한 수량만큼 상품 재고가 줄어야한다.");
    }

    private Member createSampleMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setStockQuantity(stockQuantity);
        book.setPrice(price);
        em.persist(book);
        return book;
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        //given
        Member member = createSampleMember();
        Item book = createBook("JPABook", 10000, 10); // 책의 가격과 재고
        int orderCount = 11; // 재고 수량보다 많음

        //when //then
        Assertions.assertThrows(NotEnoughStockException.class, () -> {
            orderService.makeOrder(member.getMemberId(),book.getItemId(),orderCount);
        });
    }

    @Test
    public void 주문취소() throws Exception{
        //given
        Member member = createSampleMember();
        Item book = createBook("JPABook", 10000, 10); // 책의 가격과 재고
        int orderCount = 4; // 주문 수량

        Long orderId = orderService.makeOrder(member.getMemberId(), book.getItemId(), orderCount);
        //when
        orderService.cancelOrder(orderId);
        //then
        Order findOrder = orderRepository.findOne(orderId);
        Assertions.assertEquals(OrderStatus.CANCEL, findOrder.getStatus(), "주문 상태가 CANCEL이 되어야한다.");
        Assertions.assertEquals(10, book.getStockQuantity(), "주문 취소시, 주문했던 수만큼 재고가 증가해야한다.");
    }
}
