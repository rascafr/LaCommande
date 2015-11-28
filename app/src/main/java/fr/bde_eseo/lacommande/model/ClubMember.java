package fr.bde_eseo.lacommande.model;

/**
 * Created by Rascafr on 24/10/2015.
 */
public class ClubMember {

    private String name, login, password;
    private int level;

    public ClubMember(String name, String login, String password, int level) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.level = level;
    }

    public boolean isAdmin() {
        return login.equalsIgnoreCase("admin");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
