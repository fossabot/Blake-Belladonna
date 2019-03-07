package info.skyblond.Blake.Belladonna;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Random;
import java.util.zip.CRC32;

public class Messages {
    private String title = "", content = "";
    private Timestamp sendTime = new Timestamp(System.currentTimeMillis());
    //0 for burn after reading or time up, default
    //1 preserve forever, disabled when admin password is empty

    public boolean isExpired(){
        if(PropertiesUtils.getProperties().getMessageExpiredTime() == 0)
            return false;
        if(System.currentTimeMillis() - this.sendTime.getTime() >= PropertiesUtils.getProperties().getMessageExpiredTime()*1000)
            return true;
        return false;
    }

    public String getTitle() {
        return new String(Base64.getDecoder().decode(title.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public void setTitle(String title) {
        this.title = Base64.getEncoder().encodeToString(title.trim().getBytes(StandardCharsets.UTF_8));
    }

    public String getContent() {
        return new String(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public void setContent(String content) {
        this.content = Base64.getEncoder().encodeToString(content.trim().getBytes(StandardCharsets.UTF_8));
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public String storeToFile(){
        String json = Share.gson.toJson(this);
        String name;
        try {
            if (Files.notExists(PropertiesUtils.getProperties().getDataDirectory()))
                Files.createDirectories(PropertiesUtils.getProperties().getDataDirectory());

            CRC32 crc32 = new CRC32();
            crc32.update(json.getBytes(StandardCharsets.UTF_8));
            name = Long.toHexString(crc32.getValue()) + Long.toHexString(new Random().nextLong());

            if(Files.exists(Paths.get(PropertiesUtils.getProperties().getDataDirectory() + "/" + name.toUpperCase()))){
                Share.logger.error("File already exists: " + name.toUpperCase());
                return null;
            }

            Writer writer = Files.newBufferedWriter(Paths.get(
                    PropertiesUtils.getProperties().getDataDirectory() + "/" + name.toUpperCase()));
            writer.write(json);
            writer.close();

        }catch (IOException e){
            e.printStackTrace();
            Share.logger.error("Failed to store message: " + json);
            return null;
        }
        return name;
    }

    public String storeToMysql(){
        //TODO
        return null;
    }

    @Override
    public String toString() {
        return "Messages{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", sendTime=" + sendTime +
                '}';
    }
}
