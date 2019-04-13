package com.example.guessmyfacts;

import java.util.HashMap;
import java.util.Map;

public class User {
    String email;
    String profile_pic;

    HashMap<Question, String> answers = new HashMap<Question, String>();

    public enum Question {
        AGE("age", "Age", true),
        COLOR("color", "Color"),
        HOBBY("hobby", "Hobby");

        private String key, prompt;
        private boolean isNumeric = false;

        public String getStatsKey() {
            return this.key;
        }
        public String getUserKey() {
            return this.key.toUpperCase();
        }

        public String getPrompt() {
            return this.prompt;
        }

        public boolean isNumeric() {
            return this.isNumeric;
        }

        Question(String key, String prompt) {
            this.key = key;
            this.prompt = prompt;
        }

        Question(String key, String prompt, boolean isNumeric) {
            this.key = key;
            this.prompt = prompt;
            this.isNumeric = isNumeric;
        }
    }

    public User(String email, Map<Question, String> answers, String profile_pic){
        this.email = email;
        this.answers = (HashMap<Question, String>) answers;
        this.profile_pic = profile_pic;
    }

    public String getAnswer(Question question) {
        return answers.get(question);
    }
}
