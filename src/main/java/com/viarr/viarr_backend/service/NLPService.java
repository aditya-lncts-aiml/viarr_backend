package com.viarr.viarr_backend.service;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NLPService {

    private final StanfordCoreNLP pipeline;

    private static final Set<String> knownSkills = new HashSet<>(Arrays.asList(
            "java", "python", "spring", "spring boot", "rest apis", "postgreSQL",
            "html", "css", "django", "opencv", "facenet", "streamlit",
            "mobilenetv2", "cifar-10", "machine learning", "data structures",
            "dbms", "ai", "computer vision", "docker", "kubernetes", "react", "angular", "dsa",
            "datastructures and algorithms", "datastructure", "data structure and algorithms",
            "ds&a", "database management system", "aws", "docker", "kubernetes", "sql", "mysql",
            "mongodb", "postgresql", "h2", "c++", "cpp", "c", ".net", "vb.net", "mern", "angular",
            "javascript", "typescript", "data analytics", "data analysis", "excel", "power bi", "tableau",
            "nlp","tailwind css", "spring security", "jsp", "git", "github",
            "oopm", "os", "problem solving", "communication", "leadership", "collaborative",
            "exception handling", "data management"


    ));


    private static final Pattern certificationPattern = Pattern.compile(
            "(?i)(certified|certificate|certification)\\s+(in|of)?\\s*([A-Za-z0-9 \\-+]+)"
    );

    private static final Pattern degreePattern = Pattern.compile(
            "(?i)(bachelor|master|b\\.tech|m\\.tech|phd|bsc|msc)[^.,\\n]*"
    );
    private static final Pattern experiencePattern = Pattern.compile(
            "(?i)(\\d+\\+?\\s*(years?|yrs?)\\s*(of)?\\s*(experience|exp)?[^.,\\n]*)"
    );

    private static final Pattern accomplishmentPattern = Pattern.compile(
            "(?i)(built|developed|created|designed|implemented|led)[^.,\\n]*"
    );

    private static final Pattern internshipPattern = Pattern.compile(
            "(?i)(intern(ship)?|interned|interning)[^.,\\n]*"
    );

    public NLPService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.useSUTime", "false");
        this.pipeline = new StanfordCoreNLP(props);
        log.info("Stanford CoreNLP pipeline initialized.");
    }

    public List<String> extractSkills(String text) {
        Set<String> skills = new HashSet<>();
        String lowerText = text.toLowerCase();
        String cleanedText = lowerText.replaceAll("[\\n\\r]", " "); // remove newlines for clean matching

        for (String skill : knownSkills) {
            // Add word boundaries to avoid partial matches like "c" in "css"
            String regex = "\\b" + Pattern.quote(skill.toLowerCase()) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(cleanedText);

            if (matcher.find()) {
                skills.add(skill.toLowerCase());
            }
        }

        return new ArrayList<>(skills);
    }

    public List<String> extractExperience(String text) {
        Matcher matcher = experiencePattern.matcher(text);
        List<String> experiences = new ArrayList<>();
        while (matcher.find()) {
            experiences.add(matcher.group().trim());
        }
        return experiences;
    }



    public List<String> extractEducation(String text) {
        Matcher matcher = degreePattern.matcher(text);
        List<String> educationList = new ArrayList<>();
        while (matcher.find()) {
            educationList.add(matcher.group().trim());
        }
        return educationList;
    }
    public List<String> extractInternships(String text) {
        Matcher matcher = internshipPattern.matcher(text);
        List<String> internships = new ArrayList<>();
        while (matcher.find()) {
            internships.add(matcher.group().trim());
        }
        return internships;
    }


    public List<String> extractCertifications(String text) {
        Matcher matcher = certificationPattern.matcher(text);
        List<String> certifications = new ArrayList<>();
        while (matcher.find()) {
            String cert = matcher.group().trim();
            if (!certifications.contains(cert)) {
                certifications.add(cert);
            }
        }
        return certifications;
    }

    public List<String> extractAccomplishments(String text) {
        Matcher matcher = accomplishmentPattern.matcher(text);
        List<String> accomplishments = new ArrayList<>();
        while (matcher.find()) {
            accomplishments.add(matcher.group().trim());
        }
        return accomplishments;
    }
    public List<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        keywords.addAll(extractSkills(text));
        keywords.addAll(extractEducation(text));
        keywords.addAll(extractExperience(text));
        keywords.addAll(extractCertifications(text));
        keywords.addAll(extractAccomplishments(text));
        keywords.addAll(extractInternships(text));
        return new ArrayList<>(keywords);
    }
}
