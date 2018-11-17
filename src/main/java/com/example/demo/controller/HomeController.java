package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.Frequency;
import com.example.demo.entity.Ngram;
import com.example.demo.entity.User;
import com.example.demo.service.ArticleService;
import com.example.demo.service.FrequencyService;
import com.example.demo.service.NgramService;
import com.example.demo.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.tartarus.snowball.ext.PorterStemmer;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Katrina on 9/27/2018.
 */
@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    @Autowired
    NgramService ngramService;

    @Autowired
    FrequencyService freService;

    @RequestMapping("/registration")
    public String gotoRegistration(){
        return "register";
    }
    @RequestMapping("/goLogin")
    public String gotoLogin(){
        return "login";
    }
    @RequestMapping("/goTest")
    public String gotoTest(){
        return "test";
    }
    @RequestMapping("/goLogout")
    public String goLogout(){

        return "index";
    }

    @PostMapping("/register")
    public String register(HttpServletRequest request){
        User user = new User();
        user.setUsername(request.getParameter("username"));
        user.setEmail(request.getParameter("email"));
        user.setPassword(request.getParameter("password"));
        userService.saveUser(user);

        return "login";
    }
    @RequestMapping("/goIndex")
    public String goIndex(HttpServletRequest request, HttpSession session, Model model) {
        User user= new User();
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User sampleUser = userService.findUserByUsername(username, password);
        if (sampleUser != null) {
            session.setAttribute("user",sampleUser);
            model.addAttribute("username", username);
            String email=user.getEmail();
            model.addAttribute("email", email);

            int numArticles=articleService.getAll().size();
            model.addAttribute("numArticles", numArticles);

            return "index";}
        else {
            return "login";
        }
    }
    @PostMapping("/postText")
    public String text(HttpServletRequest request) throws IOException {
        Article article = new Article();
        String title=request.getParameter("title");
        String content=request.getParameter("content");
        String agency=request.getParameter("agency");
        String url=request.getParameter("url");
        article.setTitle(title);
        article.setTitle(url);
        article.setAgency(agency);
        article.setContent(content);
        articleService.save(article);
        cleanContent(content);
        return "index";
    }

    @PostMapping("/postScrape")
    public String scrape(HttpServletRequest request) throws IOException {
        Article article = new Article();
        String url=request.getParameter("url");
        String agency=request.getParameter("agency");
        Pattern mb = Pattern.compile("mb");
        Pattern abs = Pattern.compile("abs-cbn");
        Pattern manila= Pattern.compile("manilatimes");
        Pattern inquirer = Pattern.compile("inquirer");
        Pattern gma = Pattern.compile("gmanetwork");
        Pattern sun = Pattern.compile("sunstar");
        Matcher m=mb.matcher( url );
        Matcher a=abs.matcher( url );
        Matcher t=manila.matcher( url );
        Matcher i=inquirer.matcher( url );
        Matcher g=gma.matcher( url );
        Matcher s=sun.matcher( url );

        Article sampleUrl = articleService.findByUrl(url);

        if (sampleUrl != null) {
            return"error";
        }
        else {

            if (a.find()) {
                System.out.println("THIS IS ABS CBN");
                Document document = Jsoup.connect(url).get();
                String title = document.title();
                String text = document.select("div.article-content").text();
                System.out.println("title:" + title);
                System.out.println("article" + text);
                article.setTitle(title);
                article.setAgency(agency);
                article.setUrl(url);
                article.setContent(text);
                articleService.save(article);
                cleanContent(text);
            }  else if (t.find()) {
                System.out.println("THIS IS MANILATIMES");
                Document document = Jsoup.connect(url).get();
                String title = document.title();
                System.out.println("title:" + title);

                String text = document.select("div.article-wrap").text();

                System.out.println("article" + text);
                article.setTitle(title);
                article.setAgency(agency);
                article.setUrl(url);
                article.setContent(text);
                articleService.save(article);
                cleanContent(text);
            }
            else if (m.find()) {
                System.out.println("THIS IS MANILA BULLETIN");
                Document document = Jsoup.connect(url).get();
                String title = document.title();
                System.out.println("title:" + title);

                String text = document.select("article.uk-article").text();

                System.out.println("article" + text);
                article.setTitle(title);
                article.setAgency(agency);
                article.setUrl(url);
                article.setContent(text);
                articleService.save(article);
                cleanContent(text);
            }

        }
        return "index";
    }
    @PostMapping("/postFile")
     public String postFile(){
        return "index";
    }

    @GetMapping(value="/getArticles")
    public String getArticles(Model map){
        List<Article> articlelist = articleService.getAll();
        map.addAttribute("articlelist",articlelist);
        return "articles";
    }

    @GetMapping(value="/getFreq")
    public String getFreq(HttpServletRequest request) {
        int number= freService.getAll().size();
        int artId=Integer.parseInt(request.getParameter("artId"));
        List<String> allWords = new ArrayList<String>();
        List<Integer> allFreq = new ArrayList<Integer>();
//        Frequency sampleFreq = freService.findByArtId(artId);

        for (int c=0; c< number; c++){
            Frequency sampleFreq = freService.findByArtId(artId);
            String thisWord= sampleFreq.getWord();
            int thisFreq=sampleFreq.getFrequency();
            allWords.add(thisWord);
            allFreq.add(thisFreq);
        }

        return "docu";
    }
    @GetMapping(value="/getExam")
    public String getExam(Model map){
//        Ngram ngram =findbyAll();
        List <Article> arts = articleService.getAll();
        List <Ngram> ngram = ngramService.getAll();
        List <Frequency> freq = freService.getAll();
        List <Frequency> temp = new ArrayList<>();
//        for (int j=0; j<freq.size(); j++) {
//            for (int i = 0; i < freq.size(); i++) {
//                temp = freService.findByFreqId(freq.get(i).getNgramId());
//                System.out.println(temp);
//
//            }
//        }
////                Frequency sampleFreq= freService.findByNgramId(temp);
//                System.out.println(freq.get(j).getFrequency());
//            }

//        List<Ngram> ngramlist = ngramService.getAll();
//        Ngram ngram = ngramService.getAll();
//        Frequency freq = freService.getAll();
//        List <Frequency> fre1 = freService.findByNgramIdandFreqId(ngram.getNgramId(), freq.getFreqId());
////        Collections.sort(ngramlist);
        map.addAttribute("freq",freq);

        map.addAttribute("arts",arts);
//        map.addAttribute("freq",freq);
//        int col = wordlist.size();


        return "docu";
    }


    @PostMapping("/cleanContent")
    public String cleanContent(String content) throws IOException {

        System.out.println("done scrape");
        File file = new File("C:\\Users\\Katrina\\Desktop\\stopwords.txt");
        Set<String> stopWords = new LinkedHashSet<String>();
        List<String> ngrams = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String regex = "[A-Z]+";
        Pattern r = Pattern.compile(regex);
        int wc=0, tempWC=0;
        String changeWord;

        String[] words =content.replaceAll("[^a-zA-Z ]", "").split("\\s+");

        for(String line;(line = br.readLine()) != null;)
            stopWords.add(line.trim());
        br.close();

        ArrayList<String> wordsList = new ArrayList<String>();
        ArrayList<String> stemList = new ArrayList<String>();
        for (String word : words) {

            wordsList.add(word);
        }

//        System.out.println("After for loop:  " + wordsList);




//        for (int i = 0; i < wordsList.size(); i++) {
//
//
////                Matcher m = r.matcher(wordsList.get(i));
////                if (m.find( )){
////                    wordsList.remove(i);
////                }
//
//                for ( int ii = 0; ii < wordsList.get(i).length(); ii++ ) {
//                    char ch = wordsList.get(i).charAt(ii);
//                    // check each rule in turn, with code like this:
//                    if (Character.isUpperCase(ch) == true)
//                        wordsList.remove(ii);
//                    System.out.println("removed capital");
//                }
//                 for(String j:stopWords) {
//                     if (j.contains(wordsList.get(i))) {
//                        wordsList.remove(i);
//                        System.out.println("removed");
//
//                    }
//            }
//
//
//        }

        for (int i = 0; i < wordsList.size(); i++) {
            for(String j:stopWords) {

                Matcher m = r.matcher(wordsList.get(i));
                if (m.find( )){
                    wordsList.remove(i);
                }
                else if (j.contains(wordsList.get(i))) {
                    wordsList.remove(i);
                    System.out.println("removed");

                }
            }


        }
        for (String a:wordsList){
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(a);
            stemmer.stem();
            String steem=stemmer.getCurrent();
            stemList.add(steem);
            System.out.println("stemmer: "+ steem);
        }


        System.out.println("DONE STEMMING");

        Article sampleContent = articleService.findByContent(content);
        int articleid = sampleContent.getArticleId();
        for (String bag:stemList){

            Ngram sampleWord = ngramService.findByWords(bag);
            if (sampleWord != null) {
//                int id = sampleWord.getArticleId();
//                wc=sampleWord.getWordCount();
            }
            else {
                Ngram ngram = new Ngram();
//                ngram.setArticleId(articleid);
                ngram.setWords(bag);
                ngram.setWordCount(wc);
                ngramService.save(ngram);

            }

        }

        System.out.println("DONE SAVING STEM WORDS");
        Set<String> unique = new HashSet<String>(stemList);

        for (String key : unique) {
            Ngram sampleWords = ngramService.findByWords(key);

            int wordsid = sampleWords.getNgramId();
//            int artid = sampleWords.getArticleId();

            if (sampleWords != null) {
                tempWC = sampleWords.getWordCount();
                System.out.println("temp count:" + tempWC);
                wc = tempWC + Collections.frequency(stemList, key);
                sampleWords.setWordCount(wc);
                ngramService.save(sampleWords);
                Frequency fre = new Frequency();
                fre.setFrequency(Collections.frequency(stemList, key));
                fre.setNgramId(wordsid);
                fre.setArtId(articleid);
                fre.setWord(key);
                freService.save(fre);

            } else {
                Frequency fre1 = new Frequency();
                fre1.setFrequency(Collections.frequency(stemList, key));
                fre1.setNgramId(wordsid);
                fre1.setArtId(articleid);
                fre1.setWord(key);
                freService.save(fre1);
            }
        }
        System.out.println("DONE NGRAM");
        return "index";
    }

//    public static List<String> ngrams(int n, String[] str) {
//        List<String> ngrams = new ArrayList<String>();
////        String[] words = str.split(" ");
//        for (int i = 0; i < words.length - n + 1; i++)
//            ngrams.add(concat(words, i, i+n));
//
//        return ngrams;
//    }
//    public static String concat(String[] words, int start, int end) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = start; i < end; i++)
//            sb.append((i > start ? " " : "") + words[i]);
//        return sb.toString();
//    }


}
