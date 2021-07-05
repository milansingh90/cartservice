package com.demo.spring.demo.repository;


import com.demo.spring.demo.beans.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<Item,Long> {

    //@Query("SELECT c FROM Item c where c.itemcode = :code")
    Optional<Item> findItemByItemCode(@Param("code") String code);
}
