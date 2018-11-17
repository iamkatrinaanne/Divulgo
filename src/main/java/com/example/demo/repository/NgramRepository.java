package com.example.demo.repository;

import com.example.demo.entity.Ngram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by Katrina on 10/14/2018.
 */
public interface NgramRepository extends JpaRepository<Ngram, Integer> {
    //
//    @Query("SELECT word FROM Ngram")
//    List<Ngram> getAllWord();

    //    @Query(value = "SELECT words FROM Ngram")
//    List<Ngram> findAllWords(String words);
//
    Ngram findByWords(String words);
    List<Ngram> findAll();
    //    List<Ngram> findByArticleId(int id);
//    Ngram ngram findByNgramId(int id);
    Ngram findByNgramId(int id);
    Ngram findByNgramIdAndWords(int id, String w);
//    Ngram findAll();
}
