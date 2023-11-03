package jpabook.jpashop.web;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    /**
     * 주문 생성 폼 응답 기능
     */
    @GetMapping(value = "/order")
    public String createForm(Model model){

        List<Member> members = memberService.findAllMembers();
        List<Item> items = itemService.findAllItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "/order/orderForm";
    }

    /**
     * 주문 생성 (요청데이터를 주문에 담아 DB에 저장 기능)
     */
    @PostMapping(value = "/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){
        // orderForm.html 에서 <form action>으로 데이터를 전송하였다.
        // 이는 쿼리파라미터 형식으로 데이터를 전송하므로, @RequestParam으로 읽어오고 있다.
        orderService.makeOrder(memberId, itemId, count);
        return "redirect:/orders";
    }

    /**
     * 주문 목록 검색
     * - 일반적인 주문 목록이 아닌 검색 기능을 통해 동적으로 결과를 가져오는 주문 목록 이다.
     */
    @GetMapping(value = "/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch,
                            Model model){
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    /**
     * 주문 취소
     * - 주문 취소시, 주문 상태는 CANCEL, 상품의 재고수가 다시 채워지도록 하는 기능
     */
    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){

        orderService.cancelOrder(orderId);

        return "redirect:/orders";
    }
}
