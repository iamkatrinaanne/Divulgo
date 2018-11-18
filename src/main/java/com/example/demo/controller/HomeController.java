package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.*;
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

    @Autowired
    TfidfService tfidfService;

    @Autowired
    TestService testService;

    @RequestMapping("/registration")
    public String gotoRegistration(){
        return "register";
    }
    @RequestMapping("/goLogin")
    public String gotoLogin(){
        return "login";
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
    @RequestMapping("/login")
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
            return "index";}
        else {
            return "login";
        }
    }

    @GetMapping("/getFreq")
    public String getFreq(){
        
        return "docu";
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

                String text = document.select("div.post-content-right").text();

                System.out.println("article" + text);
                article.setTitle(title);
                article.setAgency(agency);
                article.setUrl(url);
                article.setContent(text);
                articleService.save(article);
//                ngram(text);
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
//
//
//    @GetMapping(value="/getNgrams")
//    public String getNgrams(Model map){
//        List<Ngram> ngramlist = ngramService.getAllWords();
////        Collections.sort(ngramlist);
//        map.addAttribute("ngramlist",ngramlist);
//        return "ngrams";
//    }
//
//    @GetMapping(value="/getDocu")
//    public String getDocument(Model map){
//        List<Ngram> ngramlist = ngramService.getAll();
//        List<Frequency> frelist = freService.getAll();
//        List<String> ngrams = new ArrayList<String>();
//        Collections.sort(ngramlist);
//        map.addAttribute("frelist",frelist);
//
//        return "ngrams";
//    }

    @GetMapping(value="/getExam")
    public String getExam(Model map){
//        Ngram ngram =findbyAll();
        List <Article> arts = articleService.getAll();
        List <Ngram> ngram = ngramService.getAll();
        List <Frequency> freq = freService.getAll();
        List <Frequency> temp = new ArrayList<>();
        for (int j=0; j<freq.size(); j++){
            for (int i=0; i<freq.size(); i++) {
                temp = freService.findByNgramId(freq.get(i).getNgramId());
                System.out.println(temp);

            }
//                Frequency sampleFreq= freService.findByNgramId(temp);
                System.out.println(freq.get(j).getFrequency());
            }

//        List<Ngram> ngramlist = ngramService.getAll();
//        Ngram ngram = ngramService.getAll();
//        Frequency freq = freService.getAll();
//        List <Frequency> fre1 = freService.findByNgramIdandFreqId(ngram.getNgramId(), freq.getFreqId());
////        Collections.sort(ngramlist);
        map.addAttribute("freq",freq);
//        map.addAttribute("arts",freq);
//        map.addAttribute("freq",freq);
//        int col = wordlist.size();


        return "docu";
    }


    @PostMapping("/cleanContent")
    public String cleanContent(String content) throws IOException {

        System.out.println("done scrape");
        File file = new File("C:\\Users\\Katrina\\Desktop\\articles\\stopwords.txt");
        Set<String> stopWords = new LinkedHashSet<String>();
        List<String> ngrams = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String regex = "\\d+";
        int wc=0, tempWC=0;

        String[] words =content.replaceAll("[.,!?\\-()]", "").split("\\s+");

        for(String line;(line = br.readLine()) != null;)
            stopWords.add(line.trim());
        br.close();

        ArrayList<String> wordsList = new ArrayList<String>();
        ArrayList<String> stemList = new ArrayList<String>();
        for (String word : words) {

            wordsList.add(word);
        }
        System.out.println("After for loop:  " + wordsList);
//
        for (int i = 0; i < wordsList.size(); i++) {
            for(String j:stopWords) {
//                if (wordsList.get(i).matches(regex)) {
//                    wordsList.remove(i);
//                }
                if (Character.isUpperCase(wordsList.get(i).charAt(0))) {
                    wordsList.remove(i);
                }
                else if (wordsList.get(i).matches(".*[0-9].*")) {
                    wordsList.remove(i);
                } else if (j.contains(wordsList.get(i))) {
                    wordsList.remove(i);
                }
            }

        }
//        for (String str : wordsList) {
//
//            System.out.print(str + " ");
//        }
//        System.out.println("DONE REMOVING STOP WORDS");
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

    @PostMapping("/postStem")
    public String postStem(String [] stemList){

        return "index";
    }
    //TFIDF
    @RequestMapping("/submitTest")
    public String goSubmit(HttpServletRequest request) throws IOException {

        String content=request.getParameter("textarea-input");
        String agency=request.getParameter("agency");

//        article.setTitle(title);
//        article.setTitle(url);
//        article.setAgency(agency);
//        article.setContent(content);
//        articleService.save(article);
//        cleanContent(content);
        //stemming
        String[] words =content.replaceAll("[^a-zA-Z ]", "").split("\\s+");

        ArrayList<String> stemList = new ArrayList<>();

        for (String a:words){
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(a);
            stemmer.stem();
            String steem=stemmer.getCurrent();
            stemList.add(steem);
            System.out.println("stemmer: "+ steem);
        }

        System.out.println("-----STEM WORDS");
        for(String s: stemList) {
            System.out.print(s+"->");
        }

        List<Tfidf> tfidf6 = tfidfService.findAll();
        Double love = 0.0;
        Double lra = 0.0;
        Double lto = 0.0;
        Double sss= 0.0;
        HashMap<String, Double> result = new HashMap<>();
        HashMap<String, Double> entry = new HashMap<>();
        for(int i = 0; i<tfidf6.size(); i++){
            Tfidf tfidf = tfidfService.findByTfidfId(tfidf6.get(i).getTfidfId());

            if(stemList.contains(tfidf.getWord())){
                if(tfidf.getAgency().equals("LTO")){
                    lto = lto + tfidf.getTfidfVal();
                    System.out.println(tfidf.getWord()+"<------>"+tfidf.getAgency());
                    System.out.println("LTO value computation"+lto);

                }
                if(tfidf.getAgency().equals("LRA")){
                    lra = lra + tfidf.getTfidfVal();
                    System.out.println(tfidf.getWord()+"<------>"+tfidf.getAgency());
                    System.out.println("LRA value computation"+lra);
                }
                if(tfidf.getAgency().equals("PAG-IBIG")){
                    love = love + tfidf.getTfidfVal();
                    System.out.println(tfidf.getWord()+"<------>"+tfidf.getAgency());
                    System.out.println("PAG-IBIG value computation"+love);
                }
                if(tfidf.getAgency().equals("SSS")){
                    sss = sss+tfidf.getTfidfVal();
                    System.out.println(tfidf.getWord()+"<------>"+tfidf.getAgency());
                    System.out.println("SSS value computation"+sss);
                }

            }
            entry.put("LTO",lto);
            entry.put("LRA",lra);
            entry.put("PAG-IBIG",love);
            entry.put("SSS",sss);
        }
        result = maxVal(entry);

        System.out.println("-----------RESULT-------------");
        for(Map.Entry<String, Double> e: result.entrySet()){
            Test test = new Test();
            Article article = new Article();
            if(e.getKey().equals(agency)){
                test.setAgency(e.getKey());
                test.setContent(content);
                testService.save(test);
                System.out.println("RESULT IS CORRECT");
                System.out.println("SCORE: "+e.getValue());
                System.out.println("AGENCY RESULT: "+e.getKey());
                System.out.println("DECLARED AGENCY: "+agency);

                //Addition to dataset
//                article.setAgency(agency);
//                article.setContent(content);
//                articleService.save(article);
//                cleanContent(content);
            }
            else{
                test.setAgency(agency);
                test.setContent(content);
                testService.save(test);
                System.out.println("RESULT IS INCORRECT");
                System.out.println("SCORE: "+e.getValue());
                System.out.println("AGENCY RESULT: "+e.getKey());
                System.out.println("DECLARED AGENCY: "+agency);

                //Addition to dataset
//                article.setAgency(agency);
//                article.setContent(content);
//                articleService.save(article);
//                cleanContent(content);
            }
        }



        return "test";
    }

    public HashMap<String, Double> maxVal(HashMap<String, Double> values){
        HashMap<String, Double> max = new HashMap<>();
        Double maxval = 0.0;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if(entry.getValue()>maxval){
                maxval = entry.getValue();
            }
        }

        for (Map.Entry<String, Double> entry : values.entrySet())
            if(entry.getValue()==maxval)
                max.put(entry.getKey(),entry.getValue());
        return max;
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
