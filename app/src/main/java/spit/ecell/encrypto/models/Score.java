package spit.ecell.encrypto.models;

/**
 * Created by Samriddha on 22-03-2018.
 */

public class Score {
    private String username;
    private double score;

    public Score(String username, double score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
