package jpabook.jpashop.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.DistributionRate;
import jpabook.jpashop.domain.stock.SapStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component  //bean없이 가능 Autowired와 비슷
@RequiredArgsConstructor
public class InitDb {
    //조회용 샘플 데이터 입력
    private final InitService initService;

    @PostConstruct  //app loading 시점에 이거 호출하고싶어서!
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;

        public void dbInit1() {
            Account account = createAccount("E1001","GM_ONLINE_자사몰_국내");
            Account account2 = createAccount("E1002","GM_ONLINE_KAKAO");
            em.persist(account);
            em.persist(account2);

            DistributionRate distributionRate = createDistributionRate(account,60);
            DistributionRate distributionRate2 = createDistributionRate(account2,40);
            em.persist(distributionRate);
            em.persist(distributionRate2);

            Member member = createMember("userA", "서울", "1", "1111");
            em.persist(member);
            Book book1 = createBook("lang-01", 10000, 100,"11000405");
            em.persist(book1);
            Book book2 = createBook("mantu-01", 20000, 100,"11000123");
            em.persist(book2);
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);
            Order order = Order.createOrder(account,member, createDelivery(member), orderItem1, orderItem2);
            em.persist(order);
            SapStock sapStock = createSapStock(book1,100);//lang-01
            SapStock sapStock2 = createSapStock(book2,100);//mantu-01
            em.persist(sapStock);
            em.persist(sapStock2);
        }

        public void dbInit2() {
//            Member member = createMember("userB", "진주", "2", "2222"); em.persist(member);
//            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
//            em.persist(book1);
//            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
//            em.persist(book2);
//            Delivery delivery = createDelivery(member);
//            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
//            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
//            Order order = Order.createOrder(member, delivery, orderItem1,orderItem2);
//            em.persist(order);
        }
        private Member createMember(String name, String city, String street,
                                    String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        private Book createBook(String name, int price, int stockQuantity,String sapCode) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
//            book.setStockQuantity(stockQuantity);
            book.setProductCode(sapCode);
            return book;
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
        private SapStock createSapStock(Item item, int stock){
            SapStock sapStock = new SapStock();
            sapStock.setItem(item);
            sapStock.setStock(stock);
            sapStock.setDistributionFlag(0);
            return sapStock;
        }
        private Account createAccount(String sapcode, String name){
            Account account = new Account();
            account.setAccountCode(sapcode);
            account.setName(name);
            return account;
        }
        private DistributionRate createDistributionRate(Account account, int rate){
            DistributionRate distributionRate = new DistributionRate();
            distributionRate.setAccount(account);
            distributionRate.setRate(rate);
            return distributionRate;
        }

    }
}

