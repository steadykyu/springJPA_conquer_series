package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Test
    public void order_Item() throws Exception{
        //given
        Member member = createMember("규하", new Address("서울", "강가", "123-123"));

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //then
        Order findOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, findOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1,findOrder.getOrderItems().size(),"주문한 상품 종류 수가 정확해야한다.");
        assertEquals(10000*orderCount, findOrder.getTotalPrice(), "주문 가격은 가격 * 수량 이다." );
        assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고수량이 줄어야한다.");
    }

    @Test
    public void order_cancel() throws Exception{
        // 주문 취소
        //given
        Member member = createMember("규하", new Address("서울", "강가", "123-123"));
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL 이다.");
        assertEquals(10, book.getStockQuantity(), "주문 취소된 상품은 그만큼 재고가 증가한다.");

    }

    @Test
    public void overOrder() throws Exception{
        // 상품 주문_재고수량 초과 해서 주문 받을때
        //given
        Member member = createMember("규하", new Address("서울", "강가", "123-123"));
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;
        //when

        //then

        assertThrows(NotEnoughStockException.class, () ->
                orderService.order(member.getId(), book.getId(), orderCount)
        );
    }

    private Book createBook(String name, int price, int stockquantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockquantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name, Address address) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(address);
        em.persist(member);
        return member;
    }
}