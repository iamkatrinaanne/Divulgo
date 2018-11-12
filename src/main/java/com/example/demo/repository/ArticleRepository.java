package com.example.demo.repository;

import com.example.demo.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Katrina on 10/7/2018.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {

    Article findByUrl(String url);
    Article findByContent(String content);

}

