package jpabook.jpashop.web;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    /**
     * 상품 등록 폼 응답 기능
     */
    @GetMapping("/items/new")
    public String createForm(@ModelAttribute("form") BookForm bookForm){
        return "items/createItemForm";
    }

    /**
     * 상품 등록 (DB 저장) 기능
     */
    @PostMapping("/items/new")
    public String create(BookForm form){ // ModelAttribute 생략!

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        
        itemService.saveItem(book);
        return "redirect:/items"; // PRG 패턴
    }

    /**
     * 상품 목록
     */
    @GetMapping(value = "/items")
    public String itemList(Model model){
        List<Item> items = itemService.findAllItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * 상품 수정 폼 화면 응답
     */
    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form){
        Book item = (Book) itemService.findItem(itemId);

        form.setId(item.getItemId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        return "/items/updateItemForm";
    }

    /**
     * 상품 수정 (상품 값 수정 후 DB에 저장) 기능
     */
    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form){
// 수정과 같이 엔티티의 값을 수정하여 관리를 받아야하는 상황에서는 컨트롤러에서 엔티티를 생성하는 것이 좋지 않다.
// -> 준영속 엔티티가 되어 영속성 컨텍스트의 관리를 받지 못함
        // 이와달리 값을 수정하지않는 저장기능등에서는 중간과정에서 영속성 컨텍스트의 관리를 받는것이 중요하지 않고,
//         결과적으로는 DB에 저장하여 엔티티를 영속화처리하므로 엔티티를 생성해도 상관 없다.
//        Book book = new Book();
//        book.setItemId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

//        itemService.saveItem(book);

        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items"; // PRG 패턴
    }
    // 만약 수정 폼에서 Null을 넣게되면 어떻게 될까?
    // String의 경우 쿼리파라미터에 의해 name이 ""이 되버린다.
    // primitive 타입에는 어차피 null을 못넣어서, 에러발생
}
