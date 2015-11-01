package fr.bde_eseo.lacommande;

/**
 * Created by Rascafr on 24/10/2015.
 */
public class DataStore {

    private static DataStore instance;

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null)
            instance = new DataStore();
        return instance;
    }

    /**
     * Club item with informations
     */
    private ClubMember clubMember;

    public ClubMember getClubMember() {
        return clubMember;
    }

    public void setClubMember(ClubMember clubMember) {
        this.clubMember = clubMember;
    }
}
