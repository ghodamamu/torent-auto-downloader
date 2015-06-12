package com.ghodamamu;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.search.FlagTerm;

/**
 * Created by anand on 11/6/15.
 */
public class Main extends TimerTask{
    public static DB db = new DB();

    @Override
    public void run() {
        try {
            Properties properties = System.getProperties();
            properties.put("mail.imap.ssl.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties,null);
            Store store = emailSession.getStore("imaps");
            store.connect("imap.gmail.com", "", "myPassword");
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            int var = emailFolder.getMessageCount();

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            for (int i = messages.length-1; i > 0; i--) {
                Message message = messages[i];
                if("Activate Game of Thrones".equals(message.getSubject())){
                    System.out.println("EUREKA !!!");
                    downloadGOTLatestEpisode();
                }
                break;
            }

            //close the store and folder objects
            emailFolder.close(false);
            store.close();


        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void downloadGOTLatestEpisode() throws IOException, SQLException {
        db.runSql2("TRUNCATE Record;");
        processPage("http://kat.cr/usearch/game%20of%20thrones/");
    }

    public static void processPage(String URL) throws SQLException, IOException{
        //check if the given URL is already in database
        String sql = "select * from Record where URL = '"+URL+"'";
        ResultSet rs = db.runSql(sql);
        if(rs.next()){

        }else{
            //store the URL to database to avoid parsing again
            sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";
            PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, URL);
            stmt.execute();

            //get useful information
            Document doc = Jsoup.connect("http://kat.cr/usearch/Game%20of%20Thrones%20S05E09/").get();

            if(doc.text().contains("Game of Thrones S05E09")){
                System.out.println(URL);
            }

            //get all links and recursively call the processPage method
            Elements questions = doc.select("a[href]");
            for(Element link: questions){
                if(link.attr("href").contains("game-of-thrones-s05e09")){
                    //processPage(link.attr("abs:href"));
                    Document document = Jsoup.connect("http://kat.cr" + link.attr("href")).get();
                    String noOfSeeders = document.select("strong[itemprop=seeders]").text();
                    String noOfLeechers = document.select("strong[itemprop=leechers]").text();
                    //if(Long.valueOf(noOfSeeders).compareTo(Long.valueOf(noOfLeechers)) > 1){
                    openWebpage(URI.create(document.select("a[title=Magnet link]").attr("href")));
                    break;
                    //}
                }
            }
        }
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
