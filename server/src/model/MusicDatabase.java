package model;

import com.google.gson.Gson;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MusicDatabase {
    private static MusicDatabase musicDatabase = null;
    private final String FILE_NAME = "music.json";
    private final String MUSICCLASS_REGEX = "(\\,?\\[?\\s+)(?=\\{\\s+\"release\")";
    private final int PAGE_SIZE = 20;

    private MusicDatabase(){}

    public static MusicDatabase GetInstance() {
        if(musicDatabase==null)
            musicDatabase = new MusicDatabase();
        return musicDatabase;
    }

    public MusicClass getSongByID(String songID) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
        Scanner scanner = new Scanner(fileInputStream).useDelimiter(MUSICCLASS_REGEX);
        while(scanner.hasNext()) {
            String token = scanner.next();
            MusicClass musicClass = new Gson().fromJson(token, MusicClass.class);
            if(musicClass.getSongID().equals(songID)) return musicClass;
        }
        return null;
    }

    public List<MusicClass> getSongs(int index) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
        Scanner scanner = new Scanner(fileInputStream).useDelimiter(MUSICCLASS_REGEX);
        List<MusicClass> ret = new ArrayList<>();
        for(int i = 0; i < PAGE_SIZE*index; i++) {
            if(scanner.hasNext())
                scanner.next();
            else
                return null;
        }
        for(int i = 0; i < PAGE_SIZE; i++) {
            if(scanner.hasNext()) {
                String token = scanner.next();
                MusicClass musicClass = new Gson().fromJson(token, MusicClass.class);
                ret.add(musicClass);
            } else break;
        }
        return ret;
    }

    public List<MusicClass> getSongsSearch(int index, String query) throws FileNotFoundException {
        System.out.println("Searching index " + index);
        FileInputStream fileInputStream = new FileInputStream(FILE_NAME);
        Scanner scanner = new Scanner(fileInputStream).useDelimiter(MUSICCLASS_REGEX);

        List<MusicClass> ret = new ArrayList<>();
        query = query.toLowerCase();

        int c = 0;
        while(scanner.hasNext()){
            String token = scanner.next();
            if(token.endsWith("]")){
                token = token.substring(0, token.length()-1);
            }
            try {
                MusicClass musicClass = new Gson().fromJson(token, MusicClass.class);
                if (musicClass.getSongTitle().toLowerCase().contains(query)) {
                    ret.add(musicClass);
                }
                if (musicClass.getArtistName().toLowerCase().contains(query)) {
                    ret.add(musicClass);
                }
                System.out.println(c);
                c++;

            }catch (Exception e){
                System.out.println(token);
            }
            }
        System.out.println("we made it ");

        return ret.subList(index*PAGE_SIZE, (index*PAGE_SIZE)+PAGE_SIZE);
    }

}
